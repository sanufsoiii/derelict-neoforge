package dev.mim1q.derelict.vendor.gimm1q.mixin.client.model.feature;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.mim1q.derelict.vendor.gimm1q.accessor.client.EntityModelVertexConsumerOverrideAccessor;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@ApiStatus.Internal
@Mixin(RenderLayer.class)
public abstract class FeatureRendererMixin {
    @Inject(
        method = "renderColoredCutoutModel",
        at = @At("RETURN")
    )
    private static <T extends LivingEntity> void derelictGimm1q$renderModelOverlay(
        EntityModel<T> model,
        ResourceLocation texture,
        PoseStack matrices,
        MultiBufferSource vertexConsumers,
        int light,
        T entity,
        int i,
        CallbackInfo ci
    ) {
        if (model == null) return;
        final var consumer = ((EntityModelVertexConsumerOverrideAccessor) model).derelictGimm1q$getConsumerOverride(vertexConsumers);
        if (consumer == null) return;

        model.renderToBuffer(matrices, consumer, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
    }
}
