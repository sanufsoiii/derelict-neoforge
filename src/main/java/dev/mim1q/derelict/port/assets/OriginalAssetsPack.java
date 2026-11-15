package dev.mim1q.derelict.port.assets;

import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackCompatibility;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.world.flag.FeatureFlagSet;
import net.neoforged.neoforge.event.AddPackFindersEvent;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Registers the original jar from {@code resourcepacks/} as an always-on built-in client pack on top of everything.
 * Client resources only; the jar is looked up inside {@code loadPacks}, so a pack repository reload picks up a
 * jar that was dropped in while the game is running.
 */
public final class OriginalAssetsPack {
    public static final String PACK_ID = "derelict:original_assets";

    private OriginalAssetsPack() {
    }

    public static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES) return;
        event.addRepositorySource(consumer -> OriginalAssetsLocator.find().ifPresent(jar -> consumer.accept(create(jar))));
    }

    private static Pack create(Path jar) {
        PackLocationInfo info = new PackLocationInfo(
            PACK_ID,
            Component.translatable("derelict.port.assets.pack_name"),
            PackSource.BUILT_IN,
            Optional.empty()
        );
        Component description = Component.translatable("derelict.port.assets.pack_description", jar.getFileName().toString());
        Pack.ResourcesSupplier resources = new Pack.ResourcesSupplier() {
            @Override
            public PackResources openPrimary(PackLocationInfo location) {
                return new OriginalAssetsPackResources(location, new FilePackResources.SharedZipFileAccess(jar.toFile()), description);
            }

            @Override
            public PackResources openFull(PackLocationInfo location, Pack.Metadata metadata) {
                return openPrimary(location);
            }
        };
        Pack.Metadata metadata = new Pack.Metadata(description, PackCompatibility.COMPATIBLE, FeatureFlagSet.of(), List.of());
        return new Pack(info, resources, metadata, new PackSelectionConfig(true, Pack.Position.TOP, true));
    }
}
