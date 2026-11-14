package dev.mim1q.derelict.tag

import dev.mim1q.derelict.Derelict
import net.minecraft.world.entity.EntityType
import net.minecraft.core.registries.Registries
import net.minecraft.tags.TagKey

object ModEntityTags {
    val SPAWNS_SPIDERLINGS_ON_DEATH = of("spawns_spiderlings_on_death")

    private fun of(id: String): TagKey<EntityType<*>> = TagKey.create(Registries.ENTITY_TYPE, Derelict.id(id))
}