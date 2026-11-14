package dev.mim1q.derelict.vendor.gimm1q.client.item.handheld;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.Map;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public final class HandheldItemModelRegistryImpl implements HandheldItemModelRegistry {
    public static final HandheldItemModelRegistry INSTANCE = new HandheldItemModelRegistryImpl();
    public static final Map<Item, Tuple<ResourceLocation, ResourceLocation>> MODELS = new HashMap<>();

    @Override
    public void register(Item item, ResourceLocation modelId, ResourceLocation handheldId) {
        var prefixedModelId = modelId.withPrefix("item/");
        var prefixedHandheldId = handheldId.withPrefix("item/");
        ModelLoadingPlugin.register(context -> context.addModels(
            prefixedModelId,
            prefixedHandheldId
        ));
        MODELS.put(item, new Tuple<>(prefixedModelId, prefixedHandheldId));
    }
}
