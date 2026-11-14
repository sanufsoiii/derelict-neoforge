package dev.mim1q.derelict.block

import dev.mim1q.derelict.util.ShapeUtil.rotate
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.WeatheringCopper
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.DirectionProperty
import net.minecraft.world.level.block.state.properties.IntegerProperty
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.world.InteractionResult
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction.*
import net.minecraft.util.Mth
import net.minecraft.util.RandomSource
import net.minecraft.world.phys.shapes.VoxelShape
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level

sealed class RotatableCoverBlock(
    settings: Properties
) : Block(settings.noOcclusion()) {
    protected abstract fun getRotationProperty(): IntegerProperty
    protected abstract fun getRotation(ctx: BlockPlaceContext): Int

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        super.createBlockStateDefinition(builder)
        builder.add(ROTATION_8, FACING)
    }

    override fun getStateForPlacement(ctx: BlockPlaceContext): BlockState {
        val rotation = if (ctx.clickedFace.axis.isVertical) getRotation(ctx) else 0
        return defaultBlockState().setValue(FACING, ctx.clickedFace).setValue(getRotationProperty(), rotation)
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getShape(
        state: BlockState,
        world: BlockGetter,
        pos: BlockPos,
        context: CollisionContext
    ) = when (val facing = state.getValue(FACING)) {
        UP -> SHAPE_BOTTOM
        DOWN -> SHAPE_TOP
        else -> {
            SHAPE.rotate(facing)
        }
    }

    override fun useWithoutItem(
        state: BlockState,
        world: Level,
        pos: BlockPos,
        player: Player,
        hit: BlockHitResult,
    ): InteractionResult {
        val rotation = state.getValue(getRotationProperty())
        val difference = if (player.isShiftKeyDown) -1 else 1
        val newRotation = Mth.positiveModulo(rotation + difference, getRotationProperty().possibleValues.size)
        world.setBlockAndUpdate(pos, state.setValue(getRotationProperty(), newRotation))
        return InteractionResult.SUCCESS
    }

    override fun propagatesSkylightDown(state: BlockState, world: BlockGetter, pos: BlockPos) = true

    open class Normal(settings: Properties) : RotatableCoverBlock(settings) {
        override fun getRotationProperty(): IntegerProperty = ROTATION_8
        override fun getRotation(ctx: BlockPlaceContext): Int {
            val up = ctx.clickedFace == UP
            val yaw = Mth.positiveModulo(ctx.player?.yHeadRot?.plus(if (up) 11.25F else -11.25F) ?: 0.0F, 360.0F)
            val rotation = (yaw / 22.5F).toInt() % 8
            return if (ctx.clickedFace == UP) rotation else 7 - rotation
        }

        class OxidizableNormal(
            private val level: WeatheringCopper.WeatherState,
            settings: Properties
        ) : Normal(settings), WeatheringCopper {
            override fun getAge() = level

            @Suppress("OVERRIDE_DEPRECATION")
            override fun randomTick(state: BlockState, world: ServerLevel, pos: BlockPos, random: RandomSource) {
                changeOverTime(state, world, pos, random)
            }

            override fun isRandomlyTicking(state: BlockState): Boolean {
                return WeatheringCopper.getNext(state.block).isPresent
            }
        }
    }

    class Crossed(settings: Properties) : RotatableCoverBlock(settings) {
        override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
            builder.add(ROTATION_3, FACING)
        }

        override fun getRotationProperty(): IntegerProperty = ROTATION_3
        override fun getRotation(ctx: BlockPlaceContext): Int =
            if (ctx.horizontalDirection.axis == Axis.X) 2 else 0
    }

    open class SquarePatch(settings: Properties) : RotatableCoverBlock(settings) {
        override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
            builder.add(ROTATION_4, FACING)
        }

        override fun getRotationProperty(): IntegerProperty = ROTATION_4
        override fun getRotation(ctx: BlockPlaceContext): Int {
            val up = ctx.clickedFace == UP
            val yaw = Mth.positiveModulo(ctx.player?.yHeadRot?.plus(if (up) 11.25F else -11.25F) ?: 0.0F, 360.0F)
            val rotation = (yaw / 22.5F).toInt() % 4
            return if (ctx.clickedFace == UP) rotation else 3 - rotation
        }

        class OxidizableSquarePatch(
            private val level: WeatheringCopper.WeatherState,
            settings: Properties
        ) : SquarePatch(settings), WeatheringCopper {
            override fun getAge() = level

            @Suppress("OVERRIDE_DEPRECATION")
            override fun randomTick(state: BlockState, world: ServerLevel, pos: BlockPos, random: RandomSource) {
                changeOverTime(state, world, pos, random)
            }

            override fun isRandomlyTicking(state: BlockState): Boolean {
                return WeatheringCopper.getNext(state.block).isPresent
            }
        }
    }

    companion object {
        val SHAPE: VoxelShape = box(1.0, 1.0, 14.0, 15.0, 15.0, 16.0)
        val SHAPE_BOTTOM: VoxelShape = box(1.0, 0.0, 1.0, 15.0, 2.0, 15.0)
        val SHAPE_TOP: VoxelShape = box(1.0, 14.0, 1.0, 15.0, 16.0, 15.0)

        val FACING: DirectionProperty = BlockStateProperties.FACING
        val ROTATION_8: IntegerProperty = IntegerProperty.create("rotation", 0, 7)
        val ROTATION_3: IntegerProperty = IntegerProperty.create("rotation", 0, 2)
        val ROTATION_4: IntegerProperty = IntegerProperty.create("rotation", 0, 3)
    }
}