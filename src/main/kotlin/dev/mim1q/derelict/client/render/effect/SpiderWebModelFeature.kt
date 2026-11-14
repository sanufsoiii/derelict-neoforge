package dev.mim1q.derelict.client.render.effect

import dev.mim1q.derelict.Derelict
import dev.mim1q.derelict.init.ModBlocksAndItems
import dev.mim1q.derelict.init.ModStatusEffects
import dev.mim1q.derelict.init.component.ModCardinalComponents.getClientSyncedEffectAmplifier
import dev.mim1q.derelict.init.component.ModCardinalComponents.hasClientSyncedEffect
import dev.mim1q.derelict.util.render.entry
import dev.mim1q.derelict.vendor.gimm1q.client.render.overlay.ModelOverlayFeatureRenderer
import dev.mim1q.derelict.vendor.gimm1q.client.render.overlay.ModelOverlayVertexConsumer
import dev.mim1q.derelict.vendor.gimm1q.interpolation.Easing.lerp
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.LivingEntityRenderer
import net.minecraft.client.model.EntityModel
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.world.entity.LivingEntity
import com.mojang.math.Axis

val SPIDER_WEB_TEXTURE_SPARSE = Derelict.id("textures/entity/effect/spider_web_sparse.png")
val SPIDER_WEB_TEXTURE_MEDIUM = Derelict.id("textures/entity/effect/spider_web.png")
val SPIDER_WEB_TEXTURE_DENSE = Derelict.id("textures/entity/effect/spider_web_dense.png")

class SpiderWebModelFeature(
    context: LivingEntityRenderer<LivingEntity, EntityModel<LivingEntity>>,
) : ModelOverlayFeatureRenderer<LivingEntity, EntityModel<LivingEntity>>(
    context,
    { it.hasClientSyncedEffect(ModStatusEffects.COBWEBBED) },
    vertexConsumerPicker@{ entity, vertexConsumers ->
        val texture = when (entity.getClientSyncedEffectAmplifier(ModStatusEffects.COBWEBBED)) {
            2 -> SPIDER_WEB_TEXTURE_DENSE
            1 -> SPIDER_WEB_TEXTURE_MEDIUM
            else -> SPIDER_WEB_TEXTURE_SPARSE
        }
        return@vertexConsumerPicker ModelOverlayVertexConsumer
            .of(vertexConsumers.getBuffer(RenderType.entityCutout(texture)))
            .textureSize(64)
            .offset(1.101f)
            .skipPlanes()
    },
    true
) {
    override fun render(
        matrices: PoseStack,
        vertexConsumers: MultiBufferSource,
        light: Int,
        entity: LivingEntity,
        limbAngle: Float,
        limbDistance: Float,
        tickDelta: Float,
        animationProgress: Float,
        headYaw: Float,
        headPitch: Float
    ) {
        super.render(matrices, vertexConsumers, light, entity, limbAngle, limbDistance, tickDelta, animationProgress, headYaw, headPitch)

        if (!entity.hasClientSyncedEffect(ModStatusEffects.COBWEBBED)) return

        val blockRenderer = Minecraft.getInstance().blockRenderer
        val cobweb = ModBlocksAndItems.CORNER_COBWEB.defaultBlockState()

        matrices.entry {
            mulPose(Axis.YP.rotationDegrees(180f - lerp(entity.yBodyRotO, entity.yBodyRot, tickDelta) + entity.id * 777))
            if (entity.bbHeight >= 0.7 && entity.bbWidth <= 4.0) repeat(3) {
                mulPose(Axis.YP.rotationDegrees(120f))
                matrices.entry {
                    translate(-0.5, 0.6, -0.45 * (entity.bbWidth + 2))
                    blockRenderer.renderBatched(
                        cobweb, entity.blockPosition(), entity.level(), matrices, vertexConsumers.getBuffer(RenderType.cutout()), true,
                        entity.level().random
                    )
                }
            }
        }
    }
}