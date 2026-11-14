package dev.mim1q.derelict.featureset

import dev.mim1q.derelict.init.ModBlocksAndItems
import dev.mim1q.derelict.init.ModBlocksAndItems.ItemCategory
import dev.mim1q.derelict.util.extensions.copy
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockBehaviour.Properties
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.item.Item
import net.minecraft.resources.ResourceLocation

abstract class FeatureSet(
    id: ResourceLocation,
    protected val defaultItemSettings: Item.Properties = Item.Properties(),
    private val defaultBlockSettings: Properties = Properties.ofFullCopy(Blocks.STONE)
) {
    protected val name = id.path
    protected val namespace = id.namespace

    protected fun id(name: String) = ResourceLocation.fromNamespaceAndPath(namespace, name)
    open fun register(): FeatureSet = this

    protected fun <I : Item> registerItem(
        name: String,
        item: I,
        category: ItemCategory = ItemCategory.GENERAL
    ): I = ModBlocksAndItems.registerItem(name, item, category)

    protected fun <B : Block> registerBlock(name: String, block: B): B = ModBlocksAndItems.registerBlock(name, block)

    protected fun <B : Block> registerBlockWithItem(
        name: String,
        block: B,
        category: ItemCategory = ItemCategory.GENERAL
    ): B = ModBlocksAndItems.register(name, block, category)

    protected fun defaultBlockSettings(): BlockBehaviour.Properties = defaultBlockSettings.copy()
}