package dev.mim1q.derelict.block

import com.mojang.serialization.MapCodec
import dev.mim1q.derelict.init.ModBlocksAndItems
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.MultifaceBlock
import net.minecraft.world.level.block.MultifaceSpreader
import net.minecraft.world.level.block.SimpleWaterloggedBlock
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.material.FluidState
import net.minecraft.world.level.material.Fluids
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.BooleanProperty
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.phys.Vec3
import net.minecraft.util.RandomSource
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelAccessor

@Suppress("OVERRIDE_DEPRECATION", "DEPRECATION")
sealed class EmbersBlock(settings: Properties, private val particleVelocity: Double = 0.01) :
    MultifaceBlock(settings.noCollission().randomTicks()), SimpleWaterloggedBlock {
    override fun getSpreader(): MultifaceSpreader = null!!

    // Waterloggable
    companion object {
        val WATERLOGGED: BooleanProperty = BlockStateProperties.WATERLOGGED
    }

    init {
        registerDefaultState(defaultBlockState().setValue(WATERLOGGED, false))
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        super.createBlockStateDefinition(builder)
        builder.add(WATERLOGGED)
    }

    override fun getStateForPlacement(ctx: BlockPlaceContext) =
        super.getStateForPlacement(ctx)?.setValue(WATERLOGGED, ctx.level.getFluidState(ctx.clickedPos).type === Fluids.WATER)

    override fun getFluidState(state: BlockState): FluidState {
        return if (state.getValue(WATERLOGGED)) Fluids.WATER.getSource(false) else super.getFluidState(state)
    }

    override fun updateShape(
        state: BlockState,
        direction: Direction,
        neighborState: BlockState,
        world: LevelAccessor,
        pos: BlockPos,
        neighborPos: BlockPos
    ): BlockState {
        world.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world))
        return super.updateShape(state, direction, neighborState, world, pos, neighborPos)
    }

    // Particle Spawning
    private fun getBaseParticleOffset(state: BlockState, random: RandomSource): Vec3 {
        val directions = availableFaces(state)
        val direction = directions.toList()[random.nextInt(directions.size)]
        val planeVector = Vec3(1.0, 1.0, 1.0)
            .subtract(Vec3.atLowerCornerOf(direction.normal.multiply(direction.axisDirection.getStep())))
        val randomOffset = planeVector
            .multiply(random.nextDouble(), random.nextDouble(), random.nextDouble())
            .subtract(planeVector.scale(0.4))
        return Vec3.atLowerCornerOf(direction.normal).scale(0.35).add(randomOffset)
    }

    override fun animateTick(state: BlockState, world: Level, pos: BlockPos, random: RandomSource) {
        val particle = getParticle(random) ?: return
        val particlePos = getBaseParticleOffset(state, random).add(Vec3.atCenterOf(pos))
        world.addParticle(
            particle,
            particlePos.x, particlePos.y, particlePos.z,
            0.0, particleVelocity, 0.0
        )
    }

    abstract fun getParticle(random: RandomSource): ParticleOptions?

    class Smoldering(settings: Properties) : EmbersBlock(settings) {
        override fun codec(): MapCodec<out MultifaceBlock> = MapCodec.unit(ModBlocksAndItems.SMOLDERING_EMBERS)

        override fun getParticle(random: RandomSource): ParticleOptions? {
            if (random.nextFloat() < 0.8f) return null
            return if (random.nextFloat() < 0.4f) ParticleTypes.FLAME else ParticleTypes.SMOKE
        }

        override fun placeLiquid(
            world: LevelAccessor,
            pos: BlockPos,
            state: BlockState,
            fluid: FluidState
        ): Boolean {
            val canFillWithFluid = super.placeLiquid(world, pos, state, fluid)
            if (canFillWithFluid) {
                world.setBlock(
                    pos,
                    ModBlocksAndItems.SMOKING_EMBERS.withPropertiesOf(state).setValue(WATERLOGGED, true),
                    3
                )
            }
            return canFillWithFluid
        }

        override fun getStateForPlacement(ctx: BlockPlaceContext): BlockState? {
            val state = super.getStateForPlacement(ctx) ?: return null
            if (state.getValue(WATERLOGGED)) {
                return ModBlocksAndItems.SMOKING_EMBERS.withPropertiesOf(state).setValue(WATERLOGGED, true)
            }
            return state
        }
    }

    class Smoking(settings: Properties) : EmbersBlock(settings, 0.07) {
        override fun codec(): MapCodec<out MultifaceBlock> = MapCodec.unit(ModBlocksAndItems.SMOKING_EMBERS)

        override fun getParticle(random: RandomSource): ParticleOptions = ParticleTypes.CAMPFIRE_COSY_SMOKE
    }
}