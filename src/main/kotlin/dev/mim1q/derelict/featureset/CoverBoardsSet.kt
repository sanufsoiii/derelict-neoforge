package dev.mim1q.derelict.featureset

import dev.mim1q.derelict.block.RotatableCoverBlock
import dev.mim1q.derelict.init.noZFighting
import net.minecraft.world.level.block.state.BlockBehaviour.Properties
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.item.Item
import net.minecraft.resources.ResourceLocation

class CoverBoardsSet(
    id: ResourceLocation,
    defaultItemSettings: Item.Properties,
    defaultBlockSettings: Properties = Properties.ofFullCopy(Blocks.OAK_PLANKS).noZFighting()
) : FeatureSet(id, defaultItemSettings, defaultBlockSettings = defaultBlockSettings) {
    val single = registerBlockWithItem("${name}_cover_board", RotatableCoverBlock.Normal(defaultBlockSettings))
    val double = registerBlockWithItem("double_${name}_cover_boards", RotatableCoverBlock.Normal(defaultBlockSettings))
    val crossed =
        registerBlockWithItem("crossed_${name}_cover_boards", RotatableCoverBlock.Crossed(defaultBlockSettings))

    override fun register(): CoverBoardsSet {
        return this
    }
}