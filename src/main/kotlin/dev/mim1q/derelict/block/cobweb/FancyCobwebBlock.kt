package dev.mim1q.derelict.block.cobweb

import com.mojang.serialization.MapCodec
import dev.mim1q.derelict.init.ModBlocksAndItems
import dev.mim1q.derelict.tag.ModBlockTags
import net.minecraft.world.level.block.MultifaceBlock
import net.minecraft.world.level.block.MultifaceSpreader
import net.minecraft.world.level.block.SupportType
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.shapes.CollisionContext
import net.minecraft.world.entity.Entity
import net.minecraft.world.item.Item
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.network.chat.Component
import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.phys.Vec3
import net.minecraft.world.phys.shapes.VoxelShape
import net.minecraft.world.phys.shapes.Shapes
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelAccessor
import net.minecraft.world.level.LevelReader

open class FancyCobwebBlock(
    settings: Properties,
    private val hasTooltip: Boolean = false
) : MultifaceBlock(settings) {
    @Suppress("OVERRIDE_DEPRECATION")
    override fun canSurvive(state: BlockState, world: LevelReader, pos: BlockPos): Boolean {
        return DIRECTIONS.any { canConnect(world, pos, it) }
    }

    private fun canConnect(world: LevelReader, origin: BlockPos, direction: Direction): Boolean {
        val pos = origin.offset(direction.normal)
        val state = world.getBlockState(pos)
        return state.isFaceSturdy(
            world,
            pos,
            direction.opposite,
            SupportType.CENTER
        ) || state.`is`(ModBlockTags.COBWEBS)
    }

    override fun getStateForPlacement(ctx: BlockPlaceContext): BlockState {
        val pos = ctx.clickedPos
        val world = ctx.level
        var state = defaultBlockState()
        for (direction in Direction.entries) {
            state = state.setValue(getFaceProperty(direction), canConnect(world, pos, direction))
        }
        return state
    }

    override fun codec(): MapCodec<out MultifaceBlock> = MapCodec.unit(ModBlocksAndItems.FANCY_COBWEB)

    @Suppress("OVERRIDE_DEPRECATION")
    override fun updateShape(
        state: BlockState,
        direction: Direction,
        neighborState: BlockState,
        world: LevelAccessor,
        pos: BlockPos,
        neighborPos: BlockPos
    ): BlockState {
        return state.setValue(getFaceProperty(direction), canConnect(world, pos, direction))
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun canBeReplaced(state: BlockState, context: BlockPlaceContext): Boolean = false

    override fun appendHoverText(
        stack: ItemStack,
        context: Item.TooltipContext,
        tooltip: MutableList<Component>,
        options: TooltipFlag,
    ) {
        super.appendHoverText(stack, context, tooltip, options)
        val text = Component.translatable(DISABLED_TOOLTIP_KEY)
        if (text.string.isNotBlank() && hasTooltip) {
            tooltip.add(text.withStyle(ChatFormatting.DARK_RED))
        }
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun getShape(state: BlockState, world: BlockGetter, pos: BlockPos, context: CollisionContext): VoxelShape =
        Shapes.block()

    override fun getSpreader(): MultifaceSpreader = MultifaceSpreader(this)

    override fun getMaxHorizontalOffset(): Float = 1 / 16f

    override fun getMaxVerticalOffset(): Float = 1 / 16f

    @Suppress("OVERRIDE_DEPRECATION")
    override fun entityInside(state: BlockState, world: Level, pos: BlockPos, entity: Entity) {
        entity.makeStuckInBlock(state, Vec3(0.75, 0.5, 0.75))
    }

    companion object {
        private const val DISABLED_TOOLTIP_KEY = "block.derelict.fancy_cobweb.disabled_tooltip"
    }
}