package dev.mim1q.derelict.client.render.entity.projectile

import dev.mim1q.derelict.entity.projectile.SpiderSilkBolaProjectile
import dev.mim1q.derelict.util.render.entry
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.world.item.ItemDisplayContext
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.world.item.Items
import net.minecraft.resources.ResourceLocation
import com.mojang.math.Axis

class SpiderSilkBolaRenderer(context: EntityRendererProvider.Context) : EntityRenderer<SpiderSilkBolaProjectile>(
    context
) {
    private val itemRenderer = context.itemRenderer
    private val stack = Items.COBWEB.defaultInstance

    override fun render(
        entity: SpiderSilkBolaProjectile,
        yaw: Float,
        tickDelta: Float,
        matrices: PoseStack,
        vertexConsumers: MultiBufferSource,
        light: Int
    ) {
        if ((entity.tickCount + tickDelta) < 1.5f) return

        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light)

        matrices.entry {
            translate(0.0, 0.5, 0.0)
            mulPose(Axis.YP.rotationDegrees(180f + entity.yRot))
            mulPose(Axis.XP.rotationDegrees(entity.xRot))

            itemRenderer.renderStatic(
                null,
                stack,
                ItemDisplayContext.NONE,
                false,
                matrices,
                vertexConsumers,
                entity.level(),
                light,
                OverlayTexture.NO_OVERLAY,
                0
            )
        }
    }

    override fun getTextureLocation(entity: SpiderSilkBolaProjectile) = ResourceLocation.parse("textures/item/cobweb.png")
}