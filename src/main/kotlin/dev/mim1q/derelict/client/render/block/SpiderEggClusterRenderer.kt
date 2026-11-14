package dev.mim1q.derelict.client.render.block

import dev.mim1q.derelict.Derelict
import dev.mim1q.derelict.block.cobweb.SpiderEggBlock
import dev.mim1q.derelict.block.cobweb.SpiderEggBlock.SpiderEggBlockEntity
import dev.mim1q.derelict.util.render.render
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import net.minecraft.util.RandomSource
import kotlin.math.sin

class SpiderEggClusterRenderer(ctx: BlockEntityRendererProvider.Context) : BlockEntityRenderer<SpiderEggBlockEntity> {
    companion object {
        val MODEL_IDS = arrayOf(
            Derelict.id("block/spider_egg/tiny_spider_egg"),
            Derelict.id("block/spider_egg/small_spider_egg"),
            Derelict.id("block/spider_egg/medium_spider_egg"),
            Derelict.id("block/spider_egg/large_spider_egg"),
            Derelict.id("block/spider_egg/huge_spider_egg"),
        )
    }

    private val models = MODEL_IDS.mapNotNull {
        ctx.blockRenderDispatcher.blockModelShaper.modelManager.getModel(it)
    }

    override fun render(
        entity: SpiderEggBlockEntity,
        tickDelta: Float,
        matrices: PoseStack,
        vertexConsumers: MultiBufferSource,
        light: Int,
        overlay: Int
    ) {
        val block = entity.blockState.block
        if (block is SpiderEggBlock) {
            matrices.translate(0.5, 0.0, 0.5)
            matrices.mulPose(Axis.YP.rotationDegrees((entity.blockPos.hashCode() % 4) * 90f))
            matrices.translate(-0.5, 0.0, -0.5)
            val random = entity.getLevel()?.random ?: return
            val addedTime = (entity.blockPos.hashCode() % 100) * 777
            val time = (entity.getLevel()?.gameTime ?: return) + tickDelta + addedTime
            if (block.big) {
                val offset = entity.blockState.getOffset(entity.getLevel(), entity.blockPos)
                matrices.translate(offset.x, offset.y, offset.z)
                renderBig(time, random, matrices, vertexConsumers, light)
            } else {
                renderCluster(time, random, matrices, vertexConsumers, light)
            }
        }
    }

    private fun renderBig(time: Float, random: RandomSource, matrices: PoseStack, consumers: MultiBufferSource, light: Int) {
        renderEgg(random, matrices, consumers, light, 0.5f, 0f, 0.5f, 4, time)
    }

    private fun renderCluster(time: Float, random: RandomSource, matrices: PoseStack, consumers: MultiBufferSource, light: Int) {
        renderEgg(random, matrices, consumers, light, 0.9f, 0f, 0.5f, 0, time)
        renderEgg(random, matrices, consumers, light, 0.1f, 0f, 0.1f, 1, time + 10)
        renderEgg(random, matrices, consumers, light, 0.5f, 0f, 0.8f, 2, time + 20)
        renderEgg(random, matrices, consumers, light, 0.7f, 0f, 0.3f, 3, time + 30)
    }

    private fun renderEgg(
        random: RandomSource,
        matrices: PoseStack,
        consumers: MultiBufferSource,
        light: Int,
        x: Float, y: Float, z: Float,
        index: Int,
        time: Float
    ) {
        matrices.pushPose()
        matrices.translate(x, y, z)
        val scale = 0.9f + 0.15f * sin(time * 0.15f)
        matrices.scale(scale, scale, scale)
        models[index].render(random, light, matrices, consumers.getBuffer(RenderType.solid()))
        matrices.popPose()
    }
}