package dev.mim1q.derelict.tag

import net.minecraft.world.level.block.Block
import net.minecraft.core.registries.Registries
import net.minecraft.tags.TagKey
import net.minecraft.resources.ResourceLocation

object ModBlockTags {
    val COBWEBS = of("cobwebs")
    val FULL_COBWEBS = of("full_cobwebs")

    private fun of(id: String): TagKey<Block> = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("derelict", id))
}