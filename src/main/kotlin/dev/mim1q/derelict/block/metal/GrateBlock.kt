package dev.mim1q.derelict.block.metal

import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.TransparentBlock
import net.minecraft.core.Direction

open class GrateBlock(settings: BlockBehaviour.Properties) : TransparentBlock(settings) {
    @Suppress("OVERRIDE_DEPRECATION")
    override fun skipRendering(state: BlockState, stateFrom: BlockState, direction: Direction) =
        stateFrom.block is GrateBlock || super.skipRendering(state, stateFrom, direction)
}