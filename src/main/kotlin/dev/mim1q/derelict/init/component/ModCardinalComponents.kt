package dev.mim1q.derelict.init.component

import dev.mim1q.derelict.Derelict
import net.minecraft.core.Holder
import net.minecraft.core.HolderLookup
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.entity.LivingEntity
import net.neoforged.neoforge.attachment.AttachmentType
import net.neoforged.neoforge.attachment.IAttachmentHolder
import net.neoforged.neoforge.attachment.IAttachmentSerializer
import java.util.function.Supplier

/**
 * Replacement for the Cardinal Components {@code derelict:effect_flags} component: a synced and persisted
 * map of "effect id -> amplifier" attached to every [LivingEntity].
 *
 * Public extension names are kept identical to the Fabric version because the Java mixins and the client
 * HUD code call them.
 */
object ModCardinalComponents {
    val EFFECT_FLAGS: AttachmentType<DerelictEffectsComponent> =
        AttachmentType.builder(Supplier { DerelictEffectsComponent() })
            .serialize(object : IAttachmentSerializer<CompoundTag, DerelictEffectsComponent> {
                override fun read(
                    holder: IAttachmentHolder,
                    tag: CompoundTag,
                    provider: HolderLookup.Provider
                ): DerelictEffectsComponent = DerelictEffectsComponent().also { it.readFromNbt(tag) }

                override fun write(attachment: DerelictEffectsComponent, provider: HolderLookup.Provider): CompoundTag =
                    CompoundTag().also { attachment.writeToNbt(it) }
            })
            .sync(
                StreamCodec.of<RegistryFriendlyByteBuf, DerelictEffectsComponent>(
                    { buffer, attachment -> attachment.write(buffer) },
                    { buffer -> DerelictEffectsComponent().also { it.read(buffer) } }
                )
            )
            // Cardinal Components copied entity components to the respawned player; keep that behaviour.
            .copyOnDeath()
            .build()

    val LivingEntity.statusEffectComponent: DerelictEffectsComponent
        get() = getData(EFFECT_FLAGS)

    fun LivingEntity.hasClientSyncedEffect(effect: Holder<MobEffect>): Boolean =
        statusEffectComponent.getEffectAmplifier(effect) != null

    fun LivingEntity.getClientSyncedEffectAmplifier(effect: Holder<MobEffect>): Int? =
        statusEffectComponent.getEffectAmplifier(effect)

    fun LivingEntity.updateClientSyncedEffects() {
        val component = statusEffectComponent
        component.removeAll()
        activeEffectsMap.forEach { (effect, instance) ->
            component.addEffect(effect, instance.amplifier)
        }
        syncData(EFFECT_FLAGS)
    }

    class DerelictEffectsComponent {
        private val effects = mutableMapOf<ResourceLocation, Int>()

        fun readFromNbt(tag: CompoundTag) {
            effects.clear()
            tag.allKeys.forEach { key ->
                effects[ResourceLocation.parse(key)] = tag.getInt(key)
            }
        }

        fun writeToNbt(tag: CompoundTag) {
            effects.forEach { (key, value) ->
                tag.putInt(key.toString(), value)
            }
        }

        fun addEffect(effect: Holder<MobEffect>, amplifier: Int) {
            val id = effect.unwrapKey().orElseThrow().location()
            effects[id] = amplifier
        }

        fun removeEffect(effect: Holder<MobEffect>) {
            val id = effect.unwrapKey().orElseThrow().location()
            effects.remove(id)
        }

        fun getEffectAmplifier(effect: Holder<MobEffect>): Int? {
            val id = effect.unwrapKey().orElseThrow().location()
            return effects[id]
        }

        fun removeAll() = effects.clear()

        fun read(buffer: RegistryFriendlyByteBuf) {
            effects.clear()
            repeat(buffer.readVarInt()) {
                effects[buffer.readResourceLocation()] = buffer.readVarInt()
            }
        }

        fun write(buffer: RegistryFriendlyByteBuf) {
            buffer.writeVarInt(effects.size)
            effects.forEach { (id, amplifier) ->
                buffer.writeResourceLocation(id)
                buffer.writeVarInt(amplifier)
            }
        }
    }
}
