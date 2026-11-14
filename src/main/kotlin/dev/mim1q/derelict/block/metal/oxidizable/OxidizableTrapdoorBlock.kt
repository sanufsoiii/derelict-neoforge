package dev.mim1q.derelict.block.metal.oxidizable

import net.minecraft.world.level.block.state.properties.BlockSetType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.WeatheringCopper
import net.minecraft.world.level.block.TrapDoorBlock
import net.minecraft.server.level.ServerLevel
import net.minecraft.core.BlockPos
import net.minecraft.util.RandomSource

class OxidizableTrapdoorBlock(
    private val level: WeatheringCopper.WeatherState,
    settings: Properties,
    type: BlockSetType
) : TrapDoorBlock(type, settings), WeatheringCopper {
    override fun getAge(): WeatheringCopper.WeatherState = level

    override fun isRandomlyTicking(state: BlockState): Boolean = true

    @Suppress("OVERRIDE_DEPRECATION")
    override fun randomTick(state: BlockState, world: ServerLevel, pos: BlockPos, random: RandomSource) {
        changeOverTime(state, world, pos, random)
    }
}