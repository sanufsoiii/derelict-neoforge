package dev.mim1q.derelict.block.metal.oxidizable

import dev.mim1q.derelict.block.metal.BeamBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.WeatheringCopper
import net.minecraft.world.level.block.WeatheringCopper.WeatherState
import net.minecraft.server.level.ServerLevel
import net.minecraft.core.BlockPos
import net.minecraft.util.RandomSource

class OxidizableBeamBlock(
    private val oxidizationLevel: WeatherState,
    settings: Properties
) : BeamBlock(settings), WeatheringCopper {
    override fun getAge() = oxidizationLevel

    @Suppress("OVERRIDE_DEPRECATION")
    override fun randomTick(state: BlockState, world: ServerLevel, pos: BlockPos, random: RandomSource) {
        changeOverTime(state, world, pos, random)
    }

    override fun isRandomlyTicking(state: BlockState): Boolean {
        return WeatheringCopper.getNext(state.block).isPresent
    }
}