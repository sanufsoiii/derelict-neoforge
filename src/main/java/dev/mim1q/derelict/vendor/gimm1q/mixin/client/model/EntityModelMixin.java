package dev.mim1q.derelict.vendor.gimm1q.mixin.client.model;

import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.mim1q.derelict.vendor.gimm1q.accessor.client.EntityModelVertexConsumerOverrideAccessor;
import dev.mim1q.derelict.vendor.gimm1q.client.render.overlay.ModelOverlayVertexConsumer;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BiFunction;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

@Mixin(EntityModel.class)
public class EntityModelMixin<T extends Entity> implements EntityModelVertexConsumerOverrideAccessor {
    @Unique
    @Nullable
    private BiFunction<LivingEntity, MultiBufferSource, ModelOverlayVertexConsumer> derelictGimm1q$consumerOverride = null;

    @Unique
    @Nullable
    private LivingEntity derelictGimm1q$lastModelEntity = null;

    @Override
    public void derelictGimm1q$setConsumerOverride(
        BiFunction<LivingEntity, MultiBufferSource, ModelOverlayVertexConsumer> vertexConsumerPicker,
        LivingEntity derelictGimm1q$lastModelEntity
    ) {
        this.derelictGimm1q$consumerOverride = vertexConsumerPicker;
        this.derelictGimm1q$lastModelEntity = derelictGimm1q$lastModelEntity;
    }

    @Override
    @Nullable
    public VertexConsumer derelictGimm1q$getConsumerOverride(MultiBufferSource vertexConsumerProvider) {
        if (derelictGimm1q$consumerOverride == null || derelictGimm1q$lastModelEntity == null) return null;
        return derelictGimm1q$consumerOverride.apply(derelictGimm1q$lastModelEntity, vertexConsumerProvider);
    }

    @Inject(
        method = "copyPropertiesTo",
        at = @At("HEAD")
    )
    private void derelictGimm1q$copyStateTo(EntityModel<T> copy, CallbackInfo ci) {
        ((EntityModelVertexConsumerOverrideAccessor) copy).derelictGimm1q$setConsumerOverride(derelictGimm1q$consumerOverride, derelictGimm1q$lastModelEntity);
    }
}
