package dev.mim1q.derelict.entity.spider.control

import dev.mim1q.derelict.util.degreesDifference
import dev.mim1q.derelict.util.extensions.degrees
import net.minecraft.world.entity.ai.control.BodyRotationControl
import net.minecraft.world.entity.Mob
import net.minecraft.util.Mth.rotateIfNecessary
import net.minecraft.util.Mth.rotLerp
import net.minecraft.world.phys.Vec3
import kotlin.math.atan2

class ArachneBodyControl(
    private val entity: Mob,
    private val factor: Float = 0.1f
) : BodyRotationControl(entity) {
    override fun clientTick() {
        val movementVector = Vec3(entity.x - entity.xo, 0.0, entity.z - entity.zo)
        var targetBodyYaw = atan2(movementVector.z, movementVector.x).toFloat().degrees() - 90F

        val difference = degreesDifference(entity.yBodyRot, targetBodyYaw)
        if (difference > 40) {
            targetBodyYaw = rotLerp(factor, targetBodyYaw, entity.yBodyRot)
        }

        if (movementVector.horizontalDistanceSqr() > 0.0001) {
            entity.setYBodyRot(rotLerp(factor, entity.yBodyRot, targetBodyYaw))
            entity.yHeadRot = rotateIfNecessary(entity.yHeadRot, entity.yBodyRot, entity.maxHeadYRot.toFloat())
        } else {
            entity.setYBodyRot(rotLerp(factor, entity.yBodyRot, entity.yHeadRot))
        }
    }
}