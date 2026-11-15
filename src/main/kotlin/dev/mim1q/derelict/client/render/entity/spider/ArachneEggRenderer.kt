package dev.mim1q.derelict.client.render.entity.spider

import dev.mim1q.derelict.Derelict
import dev.mim1q.derelict.entity.spider.ArachneEggEntity
import dev.mim1q.derelict.util.render.entry
import dev.mim1q.derelict.util.render.render
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.resources.ResourceLocation
import net.minecraft.client.resources.model.ModelResourceLocation
import kotlin.math.sin

class ArachneEggRenderer(ctx: EntityRendererProvider.Context) : EntityRenderer<ArachneEggEntity>(ctx) {
    private val blockRenderManager = ctx.blockRenderDispatcher

    override fun getTextureLocation(entity: ArachneEggEntity): ResourceLocation = Derelict.id("none")

    override fun render(
        entity: ArachneEggEntity,
        yaw: Float,
        tickDelta: Float,
        matrices: PoseStack,
        vertexConsumers: MultiBufferSource,
        light: Int
    ) {
        super.render(entity, yaw, tickDelta, matrices, vertexConsumers, light)
        val world = entity.level()
        val model =
            blockRenderManager.blockModelShaper.modelManager.getModel(ModelResourceLocation.standalone(MODEL_IDS.getOrElse(entity.stage) { MODEL_IDS[0] })) ?: return

        val time = entity.getAnimationTime(tickDelta) * 0.15f

        matrices.entry {
            translate(0.5, 0.0, 0.5)
            val xzScale = 0.9f + 0.05f * sin(time)
            val yScale = 0.9f + 0.07f * sin(time - 1f)
            scale(xzScale, yScale, xzScale)
            translate(-0.5 - 0.5 / xzScale, 1.01, -0.5 - 0.5 / xzScale)


            model.render(
                world.random,
                light,
                matrices,
                vertexConsumers.getBuffer(RenderType.cutout())
            )
        }
    }

    companion object {
        val MODEL_IDS = arrayOf(
            Derelict.id("block/special/arachne_egg"),
            Derelict.id("block/special/arachne_egg_1"),
            Derelict.id("block/special/arachne_egg_2"),
            Derelict.id("block/special/arachne_egg_broken")
        )
    }
}
