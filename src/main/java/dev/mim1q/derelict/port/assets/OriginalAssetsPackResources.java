package dev.mim1q.derelict.port.assets;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.FilePackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * Read-only view of the original jar that exposes nothing but {@code assets/derelict/**} and
 * {@code assets/minecraft/**}. Server data (recipes, loot tables, tags, ...) is never exposed: the port ships its own.
 */
public class OriginalAssetsPackResources extends FilePackResources {
    public static final Set<String> NAMESPACES = Set.of("derelict", "minecraft");
    /** pack_format of 1.21.1 resource packs. */
    public static final int PACK_FORMAT = 34;

    private final Component description;

    public OriginalAssetsPackResources(PackLocationInfo info, SharedZipFileAccess zip, Component description) {
        super(info, zip, "");
        this.description = description;
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        if (type != PackType.CLIENT_RESOURCES) return Set.of();
        Set<String> result = new HashSet<>(super.getNamespaces(type));
        result.retainAll(NAMESPACES);
        return result;
    }

    @Override
    public @Nullable IoSupplier<InputStream> getResource(PackType type, ResourceLocation location) {
        if (type != PackType.CLIENT_RESOURCES || !NAMESPACES.contains(location.getNamespace())) return null;
        return super.getResource(type, location);
    }

    @Override
    public void listResources(PackType type, String namespace, String path, ResourceOutput output) {
        if (type != PackType.CLIENT_RESOURCES || !NAMESPACES.contains(namespace)) return;
        super.listResources(type, namespace, path, output);
    }

    /** Nothing from the jar root (fabric.mod.json, icon, ...) is exposed. */
    @Override
    public @Nullable IoSupplier<InputStream> getRootResource(String... elements) {
        return null;
    }

    /** The jar has no pack.mcmeta, so the pack metadata is provided here. */
    @SuppressWarnings("unchecked")
    @Override
    public <T> @Nullable T getMetadataSection(MetadataSectionSerializer<T> serializer) {
        if (serializer == PackMetadataSection.TYPE) {
            return (T) new PackMetadataSection(description, PACK_FORMAT);
        }
        return null;
    }
}
