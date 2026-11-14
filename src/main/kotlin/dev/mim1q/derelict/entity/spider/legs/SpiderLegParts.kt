package dev.mim1q.derelict.entity.spider.legs

import dev.mim1q.derelict.util.extensions.radians
import dev.mim1q.derelict.util.wrapRadians
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.util.Mth.PI

class SpiderLegParts(
    val joint: ModelPart,
    val upper: ModelPart,
    val lower: ModelPart,
    private val right: Boolean
) {
    fun applyIk(ik: SpiderLegIKSolver, yaw: Float, deltaTime: Float) {
        if (right) {
            joint.yRot = wrapRadians(ik.getYaw(deltaTime) - yaw.radians() + PI).coerceIn((-80f).radians(), 80f.radians())
            upper.zRot = ik.getUpperRoll(deltaTime)
            lower.zRot = ik.getLowerRoll(deltaTime)
        } else {
            joint.yRot = wrapRadians(ik.getYaw(deltaTime) - yaw.radians()).coerceIn((-80f).radians(), 80f.radians())
            upper.zRot = -ik.getUpperRoll(deltaTime)
            lower.zRot = -ik.getLowerRoll(deltaTime)
        }
    }

    companion object {
        fun create(root: ModelPart, index: Int, right: Boolean): SpiderLegParts {
            val side = if (right) "right" else "left"
            val joint = root.getChild("${side}_leg_joint$index")
            val upper = joint.getChild("${side}_leg$index")
            val lower = upper.getChild("${side}_leg_front$index")
            return SpiderLegParts(joint, upper, lower, right)
        }

        fun createArray(root: ModelPart): Array<SpiderLegParts> = arrayOf(
            create(root, 0, false),
            create(root, 1, false),
            create(root, 2, false),
            create(root, 3, false),
            create(root, 0, true),
            create(root, 1, true),
            create(root, 2, true),
            create(root, 3, true)
        )
    }
}