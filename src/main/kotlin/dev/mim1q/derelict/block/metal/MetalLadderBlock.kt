package dev.mim1q.derelict.block.metal

import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.LadderBlock
import net.minecraft.core.BlockPos
import net.minecraft.world.level.LevelReader

open class MetalLadderBlock(settings: Properties) : LadderBlock(settings) {
    @Suppress("OVERRIDE_DEPRECATION")
    override fun canSurvive(state: BlockState, world: LevelReader, pos: BlockPos) = true
}