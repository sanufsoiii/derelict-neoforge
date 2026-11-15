package dev.mim1q.derelict.vendor.gimm1q.accessor.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.mim1q.derelict.vendor.gimm1q.client.render.overlay.ModelOverlayVertexConsumer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.LivingEntity;

@ApiStatus.NonExtendable
@ApiStatus.Internal
public interface EntityModelVertexConsumerOverrideAccessor {
    void derelictGimm1q$setConsumerOverride(
        @Nullable BiFunction<LivingEntity, MultiBufferSource, ModelOverlayVertexConsumer> vertexConsumerPicker,
        @Nullable LivingEntity lastModelEntity
    );

    @Nullable
    VertexConsumer derelictGimm1q$getConsumerOverride(MultiBufferSource vertexConsumerProvider);
}
