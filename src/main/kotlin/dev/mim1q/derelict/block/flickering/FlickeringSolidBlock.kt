package dev.mim1q.derelict.block.flickering

import dev.mim1q.derelict.block.flickering.FlickeringBlock.Companion.LightState
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.core.BlockPos
import net.minecraft.util.RandomSource
import net.minecraft.world.level.Level

class FlickeringSolidBlock(
    settings: Properties
) : Block(settings.randomTicks().lightLevel(FlickeringBlock::getLuminance)),
    FlickeringBlock {
    init {
        registerDefaultState(defaultBlockState().setValue(FlickeringBlock.LIGHT_STATE, LightState.FLICKERING))
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(FlickeringBlock.LIGHT_STATE)
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun tick(state: BlockState, world: ServerLevel, pos: BlockPos, random: RandomSource) {
        scheduleFlicker(world, state, pos, random, this)
    }

    override fun setPlacedBy(world: Level, pos: BlockPos, state: BlockState, placer: LivingEntity?, itemStack: ItemStack) {
        scheduleFlicker(world, state, pos, world.random, this)
    }
}