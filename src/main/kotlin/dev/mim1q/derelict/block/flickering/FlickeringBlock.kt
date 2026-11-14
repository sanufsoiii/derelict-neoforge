package dev.mim1q.derelict.block.flickering

import dev.mim1q.derelict.Derelict
import dev.mim1q.derelict.config.DerelictConfigModel.FlickeringLightsSetting
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.properties.EnumProperty
import net.minecraft.util.StringRepresentable
import net.minecraft.core.BlockPos
import net.minecraft.util.RandomSource
import net.minecraft.world.level.Level

interface FlickeringBlock {
    fun scheduleFlicker(world: Level, state: BlockState, pos: BlockPos, random: RandomSource, block: Block) {
        if (world.isClientSide) return
        if (Derelict.CONFIG.flickeringLights() == FlickeringLightsSetting.FAST) {
            world.setBlockAndUpdate(pos, state.setValue(LIGHT_STATE, LightState.FLICKERING))
            return
        }
        val (lightState, delay) = when (Derelict.CONFIG.flickeringLights()) {
            FlickeringLightsSetting.FANCY -> nextFancyFlicker(random, state.getValue(LIGHT_STATE))
            else -> if (state.getValue(LIGHT_STATE) == LightState.HALF_ON) nextFabulousFlicker(random) else LightState.HALF_ON to 1 + random.nextInt(
                1
            )
        }

        world.setBlockAndUpdate(pos, state.setValue(LIGHT_STATE, lightState))
        world.scheduleTick(pos, block, delay)
    }

    fun nextFancyFlicker(random: RandomSource, currentState: LightState): Pair<LightState, Int> {
        return when (currentState) {
            LightState.OFF -> LightState.ON to if (random.nextFloat() < 0.75) (random.nextInt(3) + 1) else (random.nextInt(
                100
            ) + 20)

            else -> LightState.OFF to random.nextInt(3) + 1
        }
    }

    fun nextFabulousFlicker(random: RandomSource): Pair<LightState, Int> {
        if (random.nextFloat() < 0.5) {
            if (random.nextFloat() < 0.5) {
                return LightState.ON to random.nextInt(4) + 2
            }
            return LightState.ON to random.nextInt(100) + 20
        }
        return LightState.OFF to random.nextInt(6) + 1
    }

    companion object {
        fun getLuminance(state: BlockState) = when (state.getValue(LIGHT_STATE)) {
            LightState.HALF_ON -> 5
            LightState.ON -> 10
            LightState.FLICKERING -> 7
            else -> 0
        }

        val LIGHT_STATE: EnumProperty<LightState> = EnumProperty.create("light_state", LightState::class.java)

        enum class LightState(private val id: String) : StringRepresentable {
            FLICKERING("flickering"),
            OFF("off"),
            HALF_ON("half_on"),
            ON("on");

            override fun getSerializedName() = id
        }
    }
}