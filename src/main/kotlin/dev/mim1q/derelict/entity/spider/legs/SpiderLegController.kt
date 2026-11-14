package dev.mim1q.derelict.entity.spider.legs

import dev.mim1q.derelict.util.TWO_PI
import dev.mim1q.derelict.util.extensions.getLocallyOffsetPos
import net.minecraft.world.entity.LivingEntity
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.phys.Vec3
import kotlin.math.sin

class SpiderLegController(
    private val entity: LivingEntity,
    private val upperLimbLength: () -> Float,
    private val lowerLimbLength: () -> Float,
    vararg offsetsAndTargets: Pair<() -> Vec3, () -> Vec3>
) {
    init {
        assert(offsetsAndTargets.size == 8)
    }

    private val legs = offsetsAndTargets.mapIndexed { index, it ->
        SingleLegController(it.first, it.second, index >= 4)
    }.toTypedArray()

    fun tick() {
        legs.forEachIndexed { index, leg ->
            val offset = if ((index % 2 == 0) xor (index >= 4)) 6 else 0
            val isTimeToMove = entity.tickCount % 12 == (offset + (index % 2) * 2)

            leg.step(isTimeToMove || entity.tickCount == 1)

            // FOR DEBUGGING PURPOSES

//            val particlePos = entity.getLocallyOffsetPos(leg.offset)
//
//            entity.world.addParticle(
//                ParticleTypes.CRIT,
//                particlePos.x, particlePos.y, particlePos.z,
//                0.0, 0.0, 0.0
//            )
//            entity.world.addParticle(
//                ParticleTypes.ENCHANTED_HIT,
//                leg.currentTargetPos.x, leg.currentTargetPos.y, leg.currentTargetPos.z,
//                0.0, 0.0, 0.0
//            )
        }
    }

    fun getIk(index: Int) = legs[index].ikSolver

    private inner class SingleLegController(
        val offset: () -> Vec3,
        val target: () -> Vec3,
        val right: Boolean,
    ) {
        val ikSolver = SpiderLegIKSolver(upperLimbLength, lowerLimbLength)

        var prevTargetPos: Vec3 = entity.position()
        var currentTargetPos: Vec3 = entity.position()
        var nextTargetPos: Vec3? = entity.position()

        var targetChangeTicks: Int = 0

        val closerTarget: Vec3
            get() {
                val calculatedTarget = target()
                return calculatedTarget.horizontalDistance().let { length ->
                    Vec3(
                        (calculatedTarget.x / length) * (length - 0.2),
                        calculatedTarget.y,
                        (calculatedTarget.z / length) * (length - 0.1)
                    )
                }
            }

        fun step(shouldMove: Boolean) {
            if (shouldMove) {
                prevTargetPos = currentTargetPos
                val velocity = Vec3(entity.x - entity.xo, entity.y - entity.yo, entity.z - entity.zo).normalize()
                val posY = findLegY(entity.getLocallyOffsetPos(closerTarget).add(velocity))
                nextTargetPos = entity.getLocallyOffsetPos(target()).add(velocity).with(Direction.Axis.Y, posY)
            }

            val transitionTime = 6.0

            if (nextTargetPos != null) {
                if (currentTargetPos.distanceToSqr(nextTargetPos) > 0.1) {
                    currentTargetPos = prevTargetPos.lerp(nextTargetPos, targetChangeTicks / transitionTime).add(
                        0.0,
                        0.5 * (sin((targetChangeTicks - (transitionTime / 4.0)) * TWO_PI / transitionTime) + 1.0),
                        0.0
                    )
                }

                targetChangeTicks++
                if (targetChangeTicks > transitionTime) {
                    nextTargetPos = null
                }
            } else {
                targetChangeTicks = 0
            }

            ikSolver.convertToLocalAndSolve(entity, offset(), currentTargetPos, true, 5.0)
        }

        private fun findLegY(base: Vec3): Double {
            for (i in 1 downTo -3) {
                val pos = BlockPos.containing(base).above(i)
                val block = entity.level().getBlockState(pos)
                val shape = block.getCollisionShape(entity.level(), pos)
                if (!shape.isEmpty) {
                    return pos.y + shape.max(Direction.Axis.Y)
                }
            }
            return base.y
        }
    }
}