package dev.mim1q.derelict.block.metal.oxidizable

import dev.mim1q.derelict.block.metal.GrateBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.WeatheringCopper
import net.minecraft.world.level.block.WeatheringCopper.WeatherState
import net.minecraft.server.level.ServerLevel
import net.minecraft.core.BlockPos
import net.minecraft.util.RandomSource

class OxidizableGrateBlock(
    private val level: WeatherState,
    settings: Properties
) : GrateBlock(settings), WeatheringCopper {
    override fun getAge(): WeatherState = level

    override fun isRandomlyTicking(state: BlockState): Boolean =
        WeatheringCopper.getNext(state.block).isPresent

    @Suppress("OVERRIDE_DEPRECATION")
    override fun randomTick(state: BlockState, world: ServerLevel, pos: BlockPos, random: RandomSource) =
        changeOverTime(state, world, pos, random)
}