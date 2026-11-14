package dev.mim1q.derelict.vendor.gimm1q.client.render.overlay;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.mim1q.derelict.vendor.gimm1q.accessor.client.EntityModelVertexConsumerOverrideAccessor;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;

/**
 * A ready-to-use feature renderer that displays an overlay on top of the original entity model
 *
 * @param <E> Entity type
 * @param <M> Entity model type
 * @see ModelOverlayVertexConsumer
 */
public class ModelOverlayFeatureRenderer<E extends LivingEntity, M extends EntityModel<E>> extends RenderLayer<E, M> {
    private final BiFunction<E, MultiBufferSource, ModelOverlayVertexConsumer> vertexConsumerPicker;
    private final Predicate<E> predicate;
    private final boolean affectsFeatures;

    public ModelOverlayFeatureRenderer(
        RenderLayerParent<E, M> context,
        Predicate<E> predicate,
        BiFunction<E, MultiBufferSource, ModelOverlayVertexConsumer> vertexConsumerPicker,
        boolean affectsFeatures
    ) {
        super(context);
        this.predicate = predicate;
        this.vertexConsumerPicker = vertexConsumerPicker;
        this.affectsFeatures = affectsFeatures;
    }

    @Override
    public void render(
        PoseStack matrices,
        MultiBufferSource vertexConsumers,
        int light,
        E entity,
        float limbAngle,
        float limbDistance,
        float tickDelta,
        float animationProgress,
        float headYaw,
        float headPitch
    ) {
        if (!predicate.test(entity)) {
            if (affectsFeatures) {
                ((EntityModelVertexConsumerOverrideAccessor) getParentModel()).gimm1q$setConsumerOverride(null, null);
            }
            return;
        }

        var consumer = vertexConsumerPicker.apply(entity, vertexConsumers);

        if (affectsFeatures) {
            //noinspection unchecked
            ((EntityModelVertexConsumerOverrideAccessor) getParentModel()).gimm1q$setConsumerOverride(
                (BiFunction<LivingEntity, MultiBufferSource, ModelOverlayVertexConsumer>) vertexConsumerPicker,
                entity
            );
        }

        if (consumer == null) return;
        getParentModel().renderToBuffer(matrices, consumer, light, OverlayTexture.NO_OVERLAY, 0xFFFFFFFF);
    }

    public static <E extends LivingEntity, M extends EntityModel<E>>
    Function<RenderLayerParent<E, M>, RenderLayer<E, M>> of(
        Predicate<E> predicate,
        Function<MultiBufferSource, ModelOverlayVertexConsumer> vertexConsumerPicker,
        boolean affectsFeatures
    ) {
        return (ctx) -> new ModelOverlayFeatureRenderer<>(ctx, predicate, (e, v) -> vertexConsumerPicker.apply(v), affectsFeatures);
    }

    public static <E extends LivingEntity, M extends EntityModel<E>>
    Function<RenderLayerParent<E, M>, RenderLayer<E, M>> of(
        Predicate<E> predicate,
        BiFunction<E, MultiBufferSource, ModelOverlayVertexConsumer> vertexConsumerPicker,
        boolean affectsFeatures
    ) {
        return (ctx) -> new ModelOverlayFeatureRenderer<>(ctx, predicate, vertexConsumerPicker, affectsFeatures);
    }
}
