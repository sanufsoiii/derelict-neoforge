package dev.mim1q.derelict.entity.spider.legs

import dev.mim1q.derelict.util.extensions.getLocallyOffsetPos
import dev.mim1q.derelict.util.lerpAngleRadians
import dev.mim1q.derelict.vendor.gimm1q.interpolation.Easing
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.sin

class SpiderLegIKSolver(
    private val upperLengthGetter: () -> Float,
    private val lowerLengthGetter: () -> Float
) {
    private var firstTick = true

    private var yaw = 0f
    private var upperRoll = 0f
    private var lowerRoll = 0f

    private var lastYaw = 0f
    private var lastUpperRoll = 0f
    private var lastLowerRoll = 0f

    fun solve(
        target: Vec3,
        negativeLowerRoll: Boolean
    ) {
        val upperLength = upperLengthGetter()
        val lowerLength = lowerLengthGetter()

        lastUpperRoll = upperRoll
        lastLowerRoll = lowerRoll
        lastYaw = yaw

        yaw = lerpAngleRadians(yaw, atan2(target.z.toFloat(), target.x.toFloat()), 0.7f)

        val lowerRollCosine = ((target.lengthSqr().toFloat() - upperLength * upperLength - lowerLength * lowerLength) / (2f * upperLength * lowerLength)).coerceIn(-1f, 1f)
        var newLowerRoll = acos(lowerRollCosine)
        if (negativeLowerRoll) newLowerRoll = -newLowerRoll

        lowerRoll = newLowerRoll
        val lowerRollSine = sin(newLowerRoll)

        val adjacent = upperLength + lowerLength * lowerRollCosine
        val opposite = lowerLength * lowerRollSine

        val targetHorizontal = target.horizontalDistance().toFloat()
        val targetVertical = target.y.toFloat()

        val tangentY = targetVertical * adjacent - targetHorizontal * opposite
        val tangentX = targetHorizontal * adjacent + targetVertical * opposite

        upperRoll = atan2(tangentY, tangentX)

        if (firstTick) {
            firstTick = false
            lastYaw = yaw
            lastUpperRoll = upperRoll
            lastLowerRoll = lowerRoll
        }
    }

    fun convertToLocalAndSolve(
        entity: Entity,
        offset: Vec3,
        target: Vec3,
        negativeLowerRoll: Boolean,
        maxLength: Double = Double.MAX_VALUE
    ) {
        val localTarget = target.subtract(entity.getLocallyOffsetPos(offset))

        if (localTarget.horizontalDistanceSqr() > maxLength * maxLength) {
            val length = localTarget.horizontalDistance()
            val normalizedX = localTarget.x / length
            val normalizedZ = localTarget.z / length
            solve(Vec3(normalizedX * maxLength, localTarget.y, normalizedZ * maxLength), negativeLowerRoll)
            return
        }

        solve(localTarget, negativeLowerRoll)
    }

    fun getYaw(tickDelta: Float) = lerpAngleRadians(lastYaw, yaw, tickDelta)
    fun getUpperRoll(tickDelta: Float) = Easing.lerp(lastUpperRoll, upperRoll, tickDelta)
    fun getLowerRoll(tickDelta: Float) = Easing.lerp(lastLowerRoll, lowerRoll, tickDelta)
}