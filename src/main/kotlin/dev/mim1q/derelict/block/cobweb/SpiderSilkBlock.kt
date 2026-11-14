package dev.mim1q.derelict.block.cobweb

import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.entity.Entity
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.Level

class SpiderSilkBlock(settings: Properties) : Block(settings) {
    @Suppress("OVERRIDE_DEPRECATION")
    override fun entityInside(state: BlockState, world: Level, pos: BlockPos, entity: Entity) {
        entity.makeStuckInBlock(state, Vec3(0.75, 0.5, 0.75))
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun skipRendering(state: BlockState, stateFrom: BlockState, direction: Direction): Boolean =
        stateFrom.block == this
}