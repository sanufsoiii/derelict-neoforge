package dev.mim1q.derelict.vendor.gimm1q.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

public class Gimm1qCodecUtil {
    public static <T> Codec<T> createIdentifierToRegistryCodec(Registry<T> registry) {
        return ResourceLocation.CODEC.flatXmap(
            id -> {
                try {
                    var value = registry.get(id);
                    if (value == null) {
                        return DataResult.error(() -> "Identifier " + id + " not found in " + registry.key().location());
                    }
                    return DataResult.success(value);
                } catch (Exception ignored) {
                    return DataResult.error(() -> "Error while getting " + id + " from " + registry.key().location());
                }
            },
            value -> {
                var id = registry.getKey(value);
                return id == null
                    ? DataResult.error(() -> "Identifier " + value + " not found in " + registry.key().location())
                    : DataResult.success(id);
            }
        );
    }
}
