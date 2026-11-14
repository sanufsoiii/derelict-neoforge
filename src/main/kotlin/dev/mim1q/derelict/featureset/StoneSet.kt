package dev.mim1q.derelict.featureset

import net.fabricmc.fabric.api.`object`.builder.v1.block.type.BlockSetTypeBuilder
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.ButtonBlock
import net.minecraft.world.level.block.PressurePlateBlock
import net.minecraft.world.level.block.SlabBlock
import net.minecraft.world.level.block.StairBlock
import net.minecraft.world.level.block.WallBlock
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.properties.BlockSetType
import net.minecraft.resources.ResourceLocation

class StoneSet(
    id: ResourceLocation,
    defaultBlockSettings: BlockBehaviour.Properties = BlockBehaviour.Properties.ofFullCopy(Blocks.STONE)
) : FeatureSet(id, defaultBlockSettings = defaultBlockSettings) {
    val blockSetType = BlockSetTypeBuilder.copyOf(BlockSetType.STONE).build(id)

    val block = Block(defaultBlockSettings)
    val stairs = StairBlock(block.defaultBlockState(), defaultBlockSettings)
    val slab = SlabBlock(defaultBlockSettings)
    val wall = WallBlock(defaultBlockSettings)
    val pressurePlate = PressurePlateBlock(blockSetType, defaultBlockSettings)
    val button = ButtonBlock(blockSetType, 5, defaultBlockSettings)

    override fun register(): StoneSet = this.apply {
        registerBlockWithItem(name, block)
        registerBlockWithItem("${name}_stairs", stairs)
        registerBlockWithItem("${name}_slab", slab)
        registerBlockWithItem("${name}_wall", wall)
        registerBlockWithItem("${name}_pressure_plate", pressurePlate)
        registerBlockWithItem("${name}_button", button)
    }
}