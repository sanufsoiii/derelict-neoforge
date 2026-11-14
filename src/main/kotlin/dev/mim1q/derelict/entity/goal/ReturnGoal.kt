package dev.mim1q.derelict.entity.goal

import net.minecraft.world.entity.ai.goal.Goal
import net.minecraft.world.entity.Mob
import net.minecraft.core.BlockPos

class ReturnGoal<T : Mob>(
    private val entity: T,
    private val predicate: () -> Boolean,
    private val posGetter: () -> BlockPos?
) : Goal() {
    init {
        flags.add(Flag.MOVE)
    }

    override fun tick() {
        super.tick()

        val pos = posGetter()
        if (pos != null) {
            entity.getNavigation().moveTo(pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5, 0.7)
        }
    }

    override fun canUse(): Boolean = posGetter() != null && predicate()
}