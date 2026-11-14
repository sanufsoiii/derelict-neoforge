package dev.mim1q.derelict.util.extensions

import net.minecraft.client.renderer.texture.OverlayTexture
import com.mojang.blaze3d.vertex.VertexConsumer
import com.mojang.blaze3d.vertex.PoseStack
import org.joml.Vector2f

fun VertexConsumer.produceVertex(
    entry: PoseStack.Pose,
    light: Int,
    x: Float,
    y: Float,
    z: Float,
    textureU: Float,
    textureV: Float,
    red: Int,
    green: Int,
    blue: Int,
    alpha: Int
) {
    addVertex(entry.pose(), x, y, z)
        .setColor(red, green, blue, alpha)
        .setUv(textureU, textureV)
        .setOverlay(OverlayTexture.NO_OVERLAY)
        .setLight(light)
        .setNormal(entry, 0F, -1F, 0F)
}

fun VertexConsumer.drawBillboard(
    matrices: PoseStack,
    light: Int,
    from: Vector2f,
    to: Vector2f,
    fromUv: Vector2f = Vector2f(0F, 0F),
    toUv: Vector2f = Vector2f(1F, 1F),
    red: Int = 255,
    green: Int = 255,
    blue: Int = 255,
    alpha: Int = 255,
    doubleSided: Boolean = true
) {
    val entry = matrices.last()

    produceVertex(entry, light, from.x, from.y, 0.0f, fromUv.x, fromUv.y, red, green, blue, alpha)
    produceVertex(entry, light, to.x, from.y, 0.0f, toUv.x, fromUv.y, red, green, blue, alpha)
    produceVertex(entry, light, to.x, to.y, 0.0f, toUv.x, toUv.y, red, green, blue, alpha)
    produceVertex(entry, light, from.x, to.y, 0.0f, fromUv.x, toUv.y, red, green, blue, alpha)
    if (doubleSided) {
        produceVertex(entry, light, to.x, from.y, 0.0f, toUv.x, fromUv.y, red, green, blue, alpha)
        produceVertex(entry, light, from.x, from.y, 0.0f, fromUv.x, fromUv.y, red, green, blue, alpha)
        produceVertex(entry, light, from.x, to.y, 0.0f, fromUv.x, toUv.y, red, green, blue, alpha)
        produceVertex(entry, light, to.x, to.y, 0.0f, toUv.x, toUv.y, red, green, blue, alpha)
    }
}