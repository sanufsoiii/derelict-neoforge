package dev.mim1q.derelict.tag

import dev.mim1q.derelict.Derelict
import net.minecraft.world.item.Item
import net.minecraft.core.registries.Registries
import net.minecraft.tags.TagKey

object ModItemTags {
    val UNWAXED_METALS = of("unwaxed_metals")
    val WAXED_METALS = of("waxed_metals")
    val GENERAL_TAB = of("general_tab")
    val WAXED = of("waxed")

    private fun of(id: String): TagKey<Item> = TagKey.create(Registries.ITEM, Derelict.id(id))
}