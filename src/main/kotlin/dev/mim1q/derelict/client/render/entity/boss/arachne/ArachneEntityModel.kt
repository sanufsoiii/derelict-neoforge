package dev.mim1q.derelict.client.render.entity.boss.arachne

import dev.mim1q.derelict.entity.boss.ArachneEntity
import dev.mim1q.derelict.entity.spider.legs.SpiderLegParts
import dev.mim1q.derelict.util.extensions.radians
import dev.mim1q.derelict.util.extensions.setPartialAnglesDegrees
import dev.mim1q.derelict.util.extensions.setPartialRoll
import dev.mim1q.derelict.vendor.gimm1q.interpolation.Easing
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.client.renderer.RenderType
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.model.EntityModel
import com.mojang.blaze3d.vertex.PoseStack
import kotlin.math.sin

class ArachneEntityModel(root: ModelPart) : EntityModel<ArachneEntity>(RenderType::entityCutout) {

    private val body = root.getChild("body")
    private val sternum = body.getChild("sternum")
    private val abdomen = sternum.getChild("abdomen")
    private val eggs = IntRange(0, 16).map { abdomen.getChild("eggs").getChild("egg$it") }
    private val head = body.getChild("head")
    private val leftFang = head.getChild("left_fang")
    private val rightFang = head.getChild("right_fang")

    val legs = SpiderLegParts.createArray(body)

    override fun renderToBuffer(
        matrices: PoseStack,
        vertices: VertexConsumer,
        light: Int,
        overlay: Int,
        color: Int
    ) {
        body.render(matrices, vertices, light, overlay, color)
    }

    override fun setupAnim(
        entity: ArachneEntity,
        limbAngle: Float,
        limbDistance: Float,
        animationProgress: Float,
        headYaw: Float,
        headPitch: Float
    ) {
        head.setRotation(headPitch.radians(), headYaw.radians(), 0F)

        val screaming = entity.legsRaisedAnimation.update(animationProgress)
        val shaking = entity.shakingAnimation.update(animationProgress)

        legs[0].upper.setPartialAnglesDegrees(0f, -70f, 0f, screaming)
        legs[0].lower.setPartialAnglesDegrees(0f, 50f, 0f, screaming)
        legs[0].joint.setPartialAnglesDegrees(0f, 0f, 60f, screaming)

        legs[4].upper.setPartialAnglesDegrees(0f, 70f, 0f, screaming)
        legs[4].lower.setPartialAnglesDegrees(0f, -50f, 0f, screaming)
        legs[4].joint.setPartialAnglesDegrees(0f, 0f, -60f, screaming)

        body.yRot += sin(x = animationProgress * 3f) * 1f.radians() * shaking
        abdomen.yRot += sin(animationProgress * 3f - 1f) * 5f.radians() * shaking
        head.yRot += sin(animationProgress * 5f - 1f) * 2f.radians() * shaking

        val leftLegStomp = entity.leftLegStomp.update(animationProgress)
        val rightLegStomp = entity.rightLegStomp.update(animationProgress)

        legs[0].upper.zRot += 60f.radians() * leftLegStomp
        legs[0].joint.yRot += 20f.radians() * leftLegStomp
        legs[4].upper.zRot -= 60f.radians() * rightLegStomp
        legs[4].joint.yRot -= 20f.radians() * rightLegStomp

        val fangAnimation = entity.fangsAnimation.update(animationProgress)
        leftFang.zRot += fangAnimation * 20f.radians()
        leftFang.xRot -= fangAnimation * 60f.radians()
        rightFang.zRot -= fangAnimation * 20f.radians()
        rightFang.xRot -= fangAnimation * 60f.radians()
        head.xRot -= fangAnimation * 15f.radians()


        if (entity.isDeadOrDying) {
            val death = entity.deathAnimation.update(animationProgress)
            val jumpParam = (entity.deathAnimation.progress * 3).coerceAtMost(1f)
            val jump = -4 * jumpParam * (jumpParam - 1)

            body.y -= 16 * jump
            body.z -= 16 * jumpParam
            body.xRot -= 180f.radians() * jumpParam
            legs.forEachIndexed { i, leg ->
                val mul = if (i < 4) 1 else -1
                leg.lower.setPartialRoll(mul * 130f.radians(), death)
                leg.upper.setPartialRoll(mul * 40f.radians(), death)
            }
        }
    }

    override fun prepareMobModel(entity: ArachneEntity, limbAngle: Float, limbDistance: Float, tickDelta: Float) {
        val animationProgress = entity.tickCount + tickDelta

        body.resetPose()
        abdomen.resetPose()
        leftFang.resetPose()
        rightFang.resetPose()

        eggs.forEachIndexed { index, egg ->
            val speed = 7F + sin(index * 100F) * 3F
            val scale = 1F + sin((animationProgress + index * speed) * 0.25F) * 0.1F
            egg.xScale = scale; egg.yScale = scale; egg.zScale = scale
        }

        legs.forEachIndexed { index, it ->
            it.applyIk(
                entity.legController.getIk(index),
                Easing.lerp(entity.yBodyRotO, entity.yBodyRot, tickDelta),
                tickDelta
            )
        }
    }
}