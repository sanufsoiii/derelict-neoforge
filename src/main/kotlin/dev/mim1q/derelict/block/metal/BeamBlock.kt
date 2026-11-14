package dev.mim1q.derelict.block.metal

import dev.mim1q.derelict.util.ShapeUtil.rotate
import dev.mim1q.derelict.util.StringWrapper
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.EnumProperty
import net.minecraft.util.StringRepresentable
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.Direction.Axis
import net.minecraft.world.phys.shapes.VoxelShape
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.level.BlockGetter

open class BeamBlock(settings: Properties) : Block(settings) {
    companion object {
        val SHAPE_VERTICAL: VoxelShape = Shapes.or(
            box(0.0, 0.0, 0.0, 16.0, 16.0, 2.0),
            box(5.0, 0.0, 0.0, 11.0, 16.0, 16.0),
            box(0.0, 0.0, 14.0, 16.0, 16.0, 16.0)
        )

        val SHAPE_HORIZONTAL: VoxelShape = Shapes.or(
            box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0),
            box(5.0, 2.0, 0.0, 11.0, 14.0, 16.0),
            box(0.0, 14.0, 0.0, 16.0, 16.0, 16.0)
        )

        val BEAM_AXIS: EnumProperty<BeamRotation> = EnumProperty.create("axis", BeamRotation::class.java)
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(BEAM_AXIS)
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getShape(
        state: BlockState,
        world: BlockGetter,
        pos: BlockPos,
        context: CollisionContext
    ): VoxelShape = state.getValue(BEAM_AXIS).shape

    override fun getStateForPlacement(ctx: BlockPlaceContext): BlockState = when (ctx.clickedFace.axis) {
        Axis.X -> defaultBlockState().setValue(BEAM_AXIS, BeamRotation.X)
        Axis.Z -> defaultBlockState().setValue(BEAM_AXIS, BeamRotation.Z)
        else -> when (ctx.horizontalDirection.axis) {
            Axis.X -> defaultBlockState().setValue(BEAM_AXIS, BeamRotation.Y_X)
            else -> defaultBlockState().setValue(BEAM_AXIS, BeamRotation.Y_Z)
        }
    }

    enum class BeamRotation(name: String, val shape: VoxelShape) : StringRepresentable by StringWrapper(name) {
        X("x", SHAPE_HORIZONTAL.rotate(Direction.EAST)),
        Y_X("y_x", SHAPE_VERTICAL.rotate(Direction.EAST)),
        Y_Z("y_z", SHAPE_VERTICAL),
        Z("z", SHAPE_HORIZONTAL),
    }
}