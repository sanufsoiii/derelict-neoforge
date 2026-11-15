package dev.mim1q.derelict.client.render.entity.projectile

import dev.mim1q.derelict.Derelict
import dev.mim1q.derelict.entity.projectile.SpiderEggProjectile
import dev.mim1q.derelict.util.render.entry
import dev.mim1q.derelict.util.render.render
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.resources.ResourceLocation
import net.minecraft.client.resources.model.ModelResourceLocation

class SpiderEggProjectileRenderer(ctx: EntityRendererProvider.Context) : EntityRenderer<SpiderEggProjectile>(ctx) {
    val model = ctx.modelManager.getModel(ModelResourceLocation.standalone(Derelict.id("block/spider_egg/huge_spider_egg")))

    override fun render(
        entity: SpiderEggProjectile,
        yaw: Float,
        tickDelta: Float,
        matrices: PoseStack,
        vertexConsumers: MultiBufferSource,
        light: Int
    ) {
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light)
        matrices.entry {
            model?.render(entity.level().random, light, matrices, vertexConsumers.getBuffer(RenderType.solid()))
        }
    }

    override fun getTextureLocation(entity: SpiderEggProjectile): ResourceLocation = Derelict.id("none")
}
