package dev.mim1q.derelict.block.cobweb

import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.item.context.BlockPlaceContext
import net.minecraft.world.level.block.state.StateDefinition
import net.minecraft.world.level.block.state.properties.EnumProperty
import net.minecraft.world.level.block.state.properties.IntegerProperty
import net.minecraft.world.level.block.Mirror
import net.minecraft.world.level.block.Rotation
import net.minecraft.util.StringRepresentable
import net.minecraft.core.Direction
import net.minecraft.util.Mth

class FancyCornerCobwebBlock(settings: Properties) : Block(settings) {
    override fun createBlockStateDefinition(builder: StateDefinition.Builder<Block, BlockState>) {
        super.createBlockStateDefinition(builder)
        builder.add(ROTATION, TYPE)
    }

    override fun getStateForPlacement(ctx: BlockPlaceContext): BlockState {
        if (ctx.clickedFace.axis.isHorizontal) {
            return defaultBlockState()
                .setValue(ROTATION, getRotation(ctx, 45F))
                .setValue(TYPE, Type.HORIZONTAL)
        }
        return defaultBlockState()
            .setValue(ROTATION, getRotation(ctx))
            .setValue(TYPE, if (ctx.clickedFace == Direction.UP) Type.BOTTOM else Type.TOP)
    }

    private fun getRotation(ctx: BlockPlaceContext, offset: Float = 22.5F): Int {
        val yaw = Mth.positiveModulo(ctx.player?.yHeadRot?.plus(offset) ?: 0.0F, 360.0F)
        return (yaw / 45F).toInt() % 8
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun mirror(state: BlockState, mirror: Mirror): BlockState =
        state.setValue(ROTATION, (state.getValue(ROTATION) + mirror.ordinal * 4) % 8)

    @Suppress("OVERRIDE_DEPRECATION")
    override fun rotate(state: BlockState, rotation: Rotation): BlockState =
        state.setValue(ROTATION, (state.getValue(ROTATION) + rotation.ordinal * 2) % 8)


    companion object {
        val ROTATION: IntegerProperty = IntegerProperty.create("rotation", 0, 7)
        val TYPE: EnumProperty<Type> = EnumProperty.create("type", Type::class.java)
    }

    enum class Type(private val id: String) : StringRepresentable {
        TOP("top"),
        BOTTOM("bottom"),
        HORIZONTAL("horizontal");

        override fun getSerializedName() = id
    }
}