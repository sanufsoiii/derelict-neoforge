package dev.mim1q.derelict.block.cobweb

import com.mojang.serialization.MapCodec
import dev.mim1q.derelict.init.ModBlocksAndItems
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.HorizontalDirectionalBlock
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.shapes.VoxelShape
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.level.BlockGetter

class SpiderMoltBlock(settings: Properties) : HorizontalDirectionalBlock(settings) {
    override fun codec(): MapCodec<out HorizontalDirectionalBlock> = MapCodec.unit(ModBlocksAndItems.SPIDER_MOLT)

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getCollisionShape(state: BlockState, world: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape =
        Shapes.empty()

    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        builder.add(FACING)
    }

    override fun getStateForPlacement(ctx: BlockPlaceContext): BlockState =
        defaultBlockState().setValue(FACING, ctx.horizontalDirection)
}