package dev.mim1q.derelict.entity.goal

import net.minecraft.world.entity.ai.goal.Goal

open class TickingGoal(
    private val length: Int = 100,
    private val onTick: (ticks: Int) -> Unit = {},
    private val onStart: () -> Unit = {},
    private val onStop: () -> Unit = {},
    private val startPredicate: () -> Boolean = { true },
    private val shouldContinuePredicate: (ticks: Int) -> Boolean = { true }
) : Goal() {
    private var ticks = 0

    override fun canUse(): Boolean = startPredicate()

    override fun start() {
        ticks = 0
        onStart()
    }

    override fun stop() {
        ticks = 0
        onStop()
    }

    override fun canContinueToUse(): Boolean = ticks < length && shouldContinuePredicate(ticks)
    override fun tick() {
        ticks++
        onTick(ticks)
    }

    override fun requiresUpdateEveryTick(): Boolean = true
}