package dev.mim1q.derelict.port.assets;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.mim1q.derelict.Derelict;
import net.neoforged.fml.loading.FMLPaths;

import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * "Don't show again" flag of the missing assets notice, stored in {@code config/derelict-port-assets.json}.
 * Deleting the file shows the notice again. Independent of the mod's own config.
 */
public final class AssetNoticeSettings {
    public static final String FILE_NAME = "derelict-port-assets.json";
    private static final String KEY = "dont_show_again";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private AssetNoticeSettings() {
    }

    public static Path path() {
        return FMLPaths.CONFIGDIR.get().resolve(FILE_NAME);
    }

    public static boolean dontShowAgain() {
        Path path = path();
        if (!Files.isRegularFile(path)) return false;
        try (Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            return root.has(KEY) && root.get(KEY).getAsBoolean();
        } catch (Exception e) {
            Derelict.LOGGER.warn("[derelict-port] Could not read {}: {}", path, e.toString());
            return false;
        }
    }

    public static void setDontShowAgain(boolean value) {
        Path path = path();
        try {
            Files.createDirectories(path.getParent());
            JsonObject root = new JsonObject();
            root.addProperty(KEY, value);
            try (Writer writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                GSON.toJson(root, writer);
            }
        } catch (Exception e) {
            Derelict.LOGGER.warn("[derelict-port] Could not write {}: {}", path, e.toString());
        }
    }
}
