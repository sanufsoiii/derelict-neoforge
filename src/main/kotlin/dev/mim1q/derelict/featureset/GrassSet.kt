package dev.mim1q.derelict.featureset

import net.minecraft.world.level.block.state.BlockBehaviour.Properties
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.TallGrassBlock
import net.minecraft.world.level.block.DoublePlantBlock
import net.minecraft.world.item.Item
import net.minecraft.resources.ResourceLocation

class GrassSet(
    id: ResourceLocation,
    defaultItemSettings: Item.Properties
) : FeatureSet(id, defaultItemSettings) {
    val grassBlock = registerBlockWithItem("${name}_grass_block", Block(Properties.ofFullCopy(Blocks.GRASS_BLOCK)))
    val grass = registerBlockWithItem("short_${name}_grass", TallGrassBlock(Properties.ofFullCopy(Blocks.SHORT_GRASS)))
    val tallGrass =
        registerBlockWithItem("tall_${name}_grass", DoublePlantBlock(Properties.ofFullCopy(Blocks.SHORT_GRASS)))

    override fun register(): FeatureSet = this
}