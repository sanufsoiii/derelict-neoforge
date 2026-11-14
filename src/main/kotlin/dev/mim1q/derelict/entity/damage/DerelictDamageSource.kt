package dev.mim1q.derelict.entity.damage

import dev.mim1q.derelict.Derelict.id
import net.minecraft.world.entity.Entity
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.damagesource.DamageType
import net.minecraft.resources.ResourceKey
import net.minecraft.core.registries.Registries
import net.minecraft.world.level.Level

class DerelictDamageSource private constructor(val key: ResourceKey<DamageType>) {
    fun get(world: Level, entity: Entity?, indirectEntity: Entity?): DamageSource {
        return DamageSource(
            world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key),
            entity,
            indirectEntity
        )
    }

    fun get(world: Level, entity: Entity?): DamageSource {
        return DamageSource(world.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key), entity)
    }

    fun get(world: Level): DamageSource {
        return get(world, null)
    }

    companion object {
        val SPIDERLING_ALLY: DerelictDamageSource = create("spiderling_ally")

        private fun create(name: String): DerelictDamageSource {
            return DerelictDamageSource(ResourceKey.create(Registries.DAMAGE_TYPE, id(name)))
        }
    }
}