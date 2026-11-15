package dev.mim1q.derelict.vendor.gimm1q.client.item.handheld;


import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

@ApiStatus.Internal

public final class HandheldItemModelRegistryImpl implements HandheldItemModelRegistry {
    public static final HandheldItemModelRegistry INSTANCE = new HandheldItemModelRegistryImpl();
    public static final Map<Item, Tuple<ModelResourceLocation, ModelResourceLocation>> MODELS = new HashMap<>();
    /** Registered as additional (standalone) models in {@code ModelEvent.RegisterAdditional}, see Gimm1qClient. */
    public static final Set<ModelResourceLocation> ADDITIONAL_MODELS = new LinkedHashSet<>();

    @Override
    public void register(Item item, ResourceLocation modelId, ResourceLocation handheldId) {
        var prefixedModelId = ModelResourceLocation.standalone(modelId.withPrefix("item/"));
        var prefixedHandheldId = ModelResourceLocation.standalone(handheldId.withPrefix("item/"));
        ADDITIONAL_MODELS.add(prefixedModelId);
        ADDITIONAL_MODELS.add(prefixedHandheldId);
        MODELS.put(item, new Tuple<>(prefixedModelId, prefixedHandheldId));
    }
}
