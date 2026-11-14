package dev.mim1q.derelict.block.cobweb

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
import net.minecraft.world.phys.shapes.VoxelShape
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.LevelAccessor

class SpiderSilkStrandBlock(settings: Properties) : Block(settings) {
    companion object {
        private val SHAPE = box(6.0, 0.0, 6.0, 10.0, 16.0, 10.0)
        private val TYPE = EnumProperty.create("type", Type::class.java)
    }

    init {
        registerDefaultState(defaultBlockState().setValue(TYPE, Type.MIDDLE))
    }

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(TYPE)
    }

    override fun getStateForPlacement(ctx: BlockPlaceContext): BlockState = getStateForPos(ctx.level, ctx.clickedPos)

    private fun getStateForPos(world: BlockGetter, pos: BlockPos) = when {
        isFaceFull(world.getBlockState(pos.above()).getShape(world, pos.above()), Direction.DOWN) ->
            defaultBlockState().setValue(TYPE, Type.TOP)

        isFaceFull(world.getBlockState(pos.below()).getShape(world, pos.below()), Direction.UP) ->
            defaultBlockState().setValue(TYPE, Type.BOTTOM)

        else -> defaultBlockState()
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun updateShape(
        state: BlockState,
        direction: Direction,
        neighborState: BlockState,
        world: LevelAccessor,
        pos: BlockPos,
        neighborPos: BlockPos
    ): BlockState = if (direction.axis.isVertical) getStateForPos(world, pos) else state


    @Suppress("OVERRIDE_DEPRECATION")
    override fun getShape(state: BlockState, world: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape = SHAPE

    enum class Type(name: String) : StringRepresentable by StringWrapper(name) {
        TOP("top"),
        MIDDLE("middle"),
        BOTTOM("bottom");
    }
}