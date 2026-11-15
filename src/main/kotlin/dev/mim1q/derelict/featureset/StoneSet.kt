package dev.mim1q.derelict.featureset

import net.minecraft.sounds.SoundEvents
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.ButtonBlock
import net.minecraft.world.level.block.PressurePlateBlock
import net.minecraft.world.level.block.SlabBlock
import net.minecraft.world.level.block.StairBlock
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.WallBlock
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.properties.BlockSetType
import net.minecraft.resources.ResourceLocation

class StoneSet(
    id: ResourceLocation,
    defaultBlockSettings: () -> BlockBehaviour.Properties = { BlockBehaviour.Properties.ofFullCopy(Blocks.STONE) }
) : FeatureSet(id, defaultBlockSettingsFactory = defaultBlockSettings) {
    // Fabric: BlockSetTypeBuilder.copyOf(BlockSetType.STONE).build(id) (no registration).
    val blockSetType = BlockSetType(
        id.toString(),
        true,
        true,
        false,
        BlockSetType.PressurePlateSensitivity.MOBS,
        SoundType.STONE,
        SoundEvents.IRON_DOOR_CLOSE,
        SoundEvents.IRON_DOOR_OPEN,
        SoundEvents.IRON_TRAPDOOR_CLOSE,
        SoundEvents.IRON_TRAPDOOR_OPEN,
        SoundEvents.STONE_PRESSURE_PLATE_CLICK_OFF,
        SoundEvents.STONE_PRESSURE_PLATE_CLICK_ON,
        SoundEvents.STONE_BUTTON_CLICK_OFF,
        SoundEvents.STONE_BUTTON_CLICK_ON
    )

    val block = Block(defaultBlockSettings())
    val stairs = StairBlock(block.defaultBlockState(), defaultBlockSettings())
    val slab = SlabBlock(defaultBlockSettings())
    val wall = WallBlock(defaultBlockSettings())
    val pressurePlate = PressurePlateBlock(blockSetType, defaultBlockSettings())
    val button = ButtonBlock(blockSetType, 5, defaultBlockSettings())

    override fun register(): StoneSet = this.apply {
        registerBlockWithItem(name, block)
        registerBlockWithItem("${name}_stairs", stairs)
        registerBlockWithItem("${name}_slab", slab)
        registerBlockWithItem("${name}_wall", wall)
        registerBlockWithItem("${name}_pressure_plate", pressurePlate)
        registerBlockWithItem("${name}_button", button)
    }
}
