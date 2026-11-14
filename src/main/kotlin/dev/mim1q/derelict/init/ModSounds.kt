package dev.mim1q.derelict.init

import dev.mim1q.derelict.Derelict
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.Registry
import net.minecraft.sounds.SoundEvent

object ModSounds {
    val SPIDER_CAVES_AMBIENT = register("ambient.spider_caves")

    fun init() = Unit

    fun register(name: String): SoundEvent = Registry.register(
        BuiltInRegistries.SOUND_EVENT,
        Derelict.id(name),
        SoundEvent.createVariableRangeEvent(Derelict.id(name))
    )
}