package dev.mim1q.derelict.port.assets;

import dev.mim1q.derelict.Derelict;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.zip.ZipFile;

/**
 * Finds the original Derelict jar that the player put into {@code <gameDir>/resourcepacks/}.
 * <p>
 * Only the top level of that folder is scanned (no recursion), only {@code .jar} and {@code .zip} files are
 * considered, and an archive matches when it contains {@link #MARKER}. The version is not checked.
 * The result is cached until {@link #rescan()} is called. Nothing here touches the network.
 */
public final class OriginalAssetsLocator {
    public static final String MARKER = "assets/derelict/textures/block/burned_leaves.png";

    private static volatile boolean scanned = false;
    private static volatile Path found = null;

    private OriginalAssetsLocator() {
    }

    public static Path resourcePacksDir() {
        return FMLPaths.GAMEDIR.get().resolve("resourcepacks");
    }

    /** The cached original jar, scanning {@code resourcepacks/} on first use. */
    public static synchronized Optional<Path> find() {
        if (!scanned) {
            found = scan();
            scanned = true;
        }
        return Optional.ofNullable(found);
    }

    public static boolean isPresent() {
        return find().isPresent();
    }

    /** Drops the cached result and scans again. */
    public static synchronized Optional<Path> rescan() {
        scanned = false;
        found = null;
        return find();
    }

    private static Path scan() {
        Path dir = resourcePacksDir();
        List<Path> matches = new ArrayList<>();
        if (Files.isDirectory(dir)) {
            try (Stream<Path> files = Files.list(dir)) {
                files.filter(Files::isRegularFile)
                    .filter(p -> {
                        String name = p.getFileName().toString().toLowerCase(Locale.ROOT);
                        return name.endsWith(".jar") || name.endsWith(".zip");
                    })
                    .filter(OriginalAssetsLocator::hasMarker)
                    .forEach(matches::add);
            } catch (IOException | RuntimeException e) {
                Derelict.LOGGER.warn("[derelict-port] Could not scan {} for the original jar: {}", dir, e.toString());
                matches.clear();
            }
        }

        if (matches.isEmpty()) {
            Derelict.LOGGER.info("[derelict-port] ORIGINAL_ASSETS_MISSING");
            return null;
        }

        matches.sort(Comparator.comparing(OriginalAssetsLocator::modifiedTime).reversed());
        Path chosen = matches.get(0);
        for (int i = 1; i < matches.size(); i++) {
            Derelict.LOGGER.info("[derelict-port] Ignoring older original jar {} (using {})",
                matches.get(i).getFileName(), chosen.getFileName());
        }
        Derelict.LOGGER.info("[derelict-port] ORIGINAL_ASSETS_FOUND {}", chosen.getFileName());
        return chosen;
    }

    private static boolean hasMarker(Path file) {
        try (ZipFile zip = new ZipFile(file.toFile())) {
            return zip.getEntry(MARKER) != null;
        } catch (IOException | RuntimeException e) {
            Derelict.LOGGER.warn("[derelict-port] Could not read {}: {}", file.getFileName(), e.toString());
            return false;
        }
    }

    private static FileTime modifiedTime(Path file) {
        try {
            return Files.getLastModifiedTime(file);
        } catch (IOException e) {
            return FileTime.fromMillis(0);
        }
    }
}
