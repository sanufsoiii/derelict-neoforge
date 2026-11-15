package dev.mim1q.derelict.featureset

import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.SignItem
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.ButtonBlock
import net.minecraft.world.level.block.DoorBlock
import net.minecraft.world.level.block.FenceBlock
import net.minecraft.world.level.block.FenceGateBlock
import net.minecraft.world.level.block.PressurePlateBlock
import net.minecraft.world.level.block.RotatedPillarBlock
import net.minecraft.world.level.block.SlabBlock
import net.minecraft.world.level.block.StairBlock
import net.minecraft.world.level.block.StandingSignBlock
import net.minecraft.world.level.block.TrapDoorBlock
import net.minecraft.world.level.block.WallSignBlock
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.properties.BlockSetType
import net.minecraft.world.level.block.state.properties.WoodType
import net.neoforged.neoforge.common.ItemAbilities
import net.neoforged.neoforge.event.level.BlockEvent

class WoodSet(
    id: ResourceLocation,
    defaultItemSettings: Item.Properties = Item.Properties(),
) : FeatureSet(id, defaultItemSettings, { BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_WOOD) }) {
    // Fabric: BlockSetTypeBuilder.copyOf(BlockSetType.OAK).register(id)
    val blockSetType = BlockSetType.register(BlockSetType(id.toString()))
    // Fabric: WoodTypeBuilder.copyOf(WoodType.OAK).register(id, blockSetType)
    val woodType = WoodType.register(WoodType(id.toString(), blockSetType))

    val planks = Block(defaultBlockSettings())
    val log = RotatedPillarBlock(defaultBlockSettings())
    val strippedLog = RotatedPillarBlock(defaultBlockSettings())
    val wood = RotatedPillarBlock(defaultBlockSettings())
    val strippedWood = RotatedPillarBlock(defaultBlockSettings())
    val stairs = StairBlock(planks.defaultBlockState(), defaultBlockSettings())
    val slab = SlabBlock(defaultBlockSettings())
    val door = DoorBlock(blockSetType, defaultBlockSettings().noOcclusion())
    val trapdoor = TrapDoorBlock(blockSetType, defaultBlockSettings().noOcclusion())
    val fence = FenceBlock(defaultBlockSettings())
    val fenceGate = FenceGateBlock(woodType, defaultBlockSettings())
    val button = ButtonBlock(blockSetType, 20, defaultBlockSettings())
    val pressurePlate = PressurePlateBlock(blockSetType, defaultBlockSettings())
    val sign = StandingSignBlock(woodType, defaultBlockSettings().noCollission())
    val wallSign = WallSignBlock(woodType, defaultBlockSettings().noCollission())

    override fun register(): WoodSet = this.apply {
        registerBlockWithItem("${name}_planks", planks)
        registerBlockWithItem("${name}_log", log)
        registerBlockWithItem("stripped_${name}_log", strippedLog)
        registerBlockWithItem("${name}_wood", wood)
        registerBlockWithItem("stripped_${name}_wood", strippedWood)
        registerBlockWithItem("${name}_stairs", stairs)
        registerBlockWithItem("${name}_slab", slab)
        registerBlockWithItem("${name}_door", door)
        registerBlockWithItem("${name}_trapdoor", trapdoor)
        registerBlockWithItem("${name}_fence", fence)
        registerBlockWithItem("${name}_fence_gate", fenceGate)
        registerBlockWithItem("${name}_button", button)
        registerBlockWithItem("${name}_pressure_plate", pressurePlate)
        registerBlock("${name}_sign", sign)
        registerBlock("${name}_wall_sign", wallSign)
        registerItem("${name}_sign", SignItem(defaultItemSettings.stacksTo(16), sign, wallSign))
        STRIPPABLES[log] = strippedLog
        STRIPPABLES[wood] = strippedWood
    }

    companion object {
        /** Fabric: StrippableBlockRegistry.register. On NeoForge stripping is a BlockToolModificationEvent. */
        private val STRIPPABLES = mutableMapOf<Block, Block>()

        fun onToolModification(event: BlockEvent.BlockToolModificationEvent) {
            if (event.itemAbility != ItemAbilities.AXE_STRIP) return
            val stripped = STRIPPABLES[event.state.block] ?: return
            // Also set while simulated: the simulation asks "would this work", the answer must match.
            event.finalState = stripped.withPropertiesOf(event.state)
        }
    }
}
