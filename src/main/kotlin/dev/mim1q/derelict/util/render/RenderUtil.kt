package dev.mim1q.derelict.util.render

import net.minecraft.client.renderer.texture.OverlayTexture
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.resources.model.BakedModel
import net.minecraft.client.renderer.block.model.BakedQuad
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.core.Direction
import net.minecraft.util.RandomSource

inline fun PoseStack.entry(setup: PoseStack.() -> Unit) {
    pushPose()
    setup()
    popPose()
}

fun BakedModel.render(
    random: RandomSource,
    light: Int,
    matrices: PoseStack,
    buffer: VertexConsumer
) {
    for (direction in Direction.entries) {
        val quads = this.getQuads(null, direction, random)
        renderBakedQuads(quads, matrices, buffer, light)
    }

    val noDirectionQuads = this.getQuads(null, null, random)
    renderBakedQuads(noDirectionQuads, matrices, buffer, light)
}

private fun renderBakedQuads(
    quads: List<BakedQuad>,
    matrices: PoseStack,
    buffer: VertexConsumer,
    light: Int
) {
    for (quad in quads) {
        buffer.putBulkData(
            matrices.last(),
            quad,
            floatArrayOf(1.0f, 1.0f, 1.0f, 1.0f),
            1f, 1f, 1f, 1f,
            intArrayOf(light, light, light, light),
            OverlayTexture.NO_OVERLAY,
            false
        )
    }
}