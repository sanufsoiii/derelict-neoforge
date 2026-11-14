package dev.mim1q.derelict.block.metal.oxidizable

import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.WeatheringCopper
import net.minecraft.world.level.block.IronBarsBlock
import net.minecraft.server.level.ServerLevel
import net.minecraft.core.BlockPos
import net.minecraft.util.RandomSource

class OxidizablePaneBlock(
    private val oxidizationLevel: WeatheringCopper.WeatherState,
    settings: Properties
) : IronBarsBlock(settings), WeatheringCopper {
    override fun getAge() = oxidizationLevel

    @Suppress("OVERRIDE_DEPRECATION")
    override fun randomTick(state: BlockState, world: ServerLevel, pos: BlockPos, random: RandomSource) {
        changeOverTime(state, world, pos, random)
    }

    override fun isRandomlyTicking(state: BlockState): Boolean {
        return WeatheringCopper.getNext(state.block).isPresent
    }
}