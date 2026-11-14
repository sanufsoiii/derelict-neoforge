package dev.mim1q.derelict.init.component

import dev.mim1q.derelict.Derelict
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.effect.MobEffect
import net.minecraft.nbt.CompoundTag
import net.minecraft.core.HolderLookup.Provider
import net.minecraft.core.Holder
import net.minecraft.resources.ResourceLocation
import org.ladysnake.cca.api.v3.component.Component
import org.ladysnake.cca.api.v3.component.ComponentKey
import org.ladysnake.cca.api.v3.component.ComponentRegistry
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer

object ModCardinalComponents : EntityComponentInitializer {
    val EFFECTS_COMPONENT: ComponentKey<DerelictEffectsComponent> =
        ComponentRegistry.getOrCreate(Derelict.id("effect_flags"), DerelictEffectsComponent::class.java)

    override fun registerEntityComponentFactories(registry: EntityComponentFactoryRegistry) {
        registry.registerFor(LivingEntity::class.java, EFFECTS_COMPONENT, ::DerelictEffectsComponent)
    }

    class DerelictEffectsComponent(private val provider: Any) : Component, AutoSyncedComponent {
        private val effects = mutableMapOf<ResourceLocation, Int>()


        override fun readFromNbt(tag: CompoundTag, registry: Provider) {
            effects.clear()
            tag.allKeys.forEach { key ->
                effects[ResourceLocation.parse(key)] = tag.getInt(key)
            }
        }

        override fun writeToNbt(tag: CompoundTag, registry: Provider) {
            effects.forEach { (key, value) ->
                tag.putInt(key.toString(), value)
            }
        }

        fun addEffect(effect: Holder<MobEffect>, amplifier: Int) {
            val id = effect.unwrapKey().get().location()
            effects[id] = amplifier
            EFFECTS_COMPONENT.sync(provider)
        }

        fun removeEffect(effect: Holder<MobEffect>) {
            val id = effect.unwrapKey().get().location()
            effects.remove(id)
            EFFECTS_COMPONENT.sync(provider)
        }

        fun getEffectAmplifier(effect: Holder<MobEffect>): Int? {
            val id = effect.unwrapKey().get().location()
            return effects[id]
        }

        fun removeAll() = effects.clear()
    }

    val LivingEntity.statusEffectComponent: DerelictEffectsComponent
        get() = EFFECTS_COMPONENT.get(this)

    fun LivingEntity.hasClientSyncedEffect(effect: Holder<MobEffect>): Boolean =
        statusEffectComponent.getEffectAmplifier(effect) != null

    fun LivingEntity.getClientSyncedEffectAmplifier(effect: Holder<MobEffect>): Int? =
        statusEffectComponent.getEffectAmplifier(effect)

    fun LivingEntity.updateClientSyncedEffects() {
        statusEffectComponent.removeAll()

        this.activeEffectsMap.forEach {
            statusEffectComponent.addEffect(it.key, it.value.amplifier)
        }
    }
}
