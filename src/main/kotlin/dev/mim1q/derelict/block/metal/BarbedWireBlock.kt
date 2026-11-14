package dev.mim1q.derelict.block.metal

import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.EnumProperty
import net.minecraft.world.level.block.state.properties.BlockStateProperties
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction.Axis
import net.minecraft.world.phys.Vec3
import net.minecraft.world.phys.shapes.VoxelShape
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level

open class BarbedWireBlock(settings: Properties) : Block(settings) {
    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(AXIS)
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getShape(
        state: BlockState,
        world: BlockGetter,
        pos: BlockPos,
        context: CollisionContext
    ): VoxelShape = if (state.getValue(AXIS) == Axis.X) SHAPE else SHAPE_ROTATED

    override fun getStateForPlacement(ctx: BlockPlaceContext): BlockState =
        defaultBlockState().setValue(AXIS, ctx.horizontalDirection.getClockWise().axis)

    @Suppress("OVERRIDE_DEPRECATION")
    override fun entityInside(state: BlockState, world: Level, pos: BlockPos, entity: Entity) {
        entity.makeStuckInBlock(state, Vec3(0.5, 0.5, 0.5))
        entity.hurt(world.damageSources().cactus(), 1.5F)
    }

    companion object {
        val SHAPE: VoxelShape = box(0.0, 0.0, 4.0, 16.0, 8.0, 12.0)
        val SHAPE_ROTATED: VoxelShape = box(4.0, 0.0, 0.0, 12.0, 8.0, 16.0)

        val AXIS: EnumProperty<Axis> = BlockStateProperties.HORIZONTAL_AXIS
    }
}