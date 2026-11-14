package dev.mim1q.derelict.particle.colored

import com.mojang.serialization.MapCodec
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.particles.ParticleType


class ColoredParticleType private constructor() :
    ParticleType<ColoredParticleEffect>(true) {

    override fun codec(): MapCodec<ColoredParticleEffect> =
        ColoredParticleEffect.createMapCodec(this)

    override fun streamCodec(): StreamCodec<RegistryFriendlyByteBuf, ColoredParticleEffect> =
        ColoredParticleEffect.createPacketCodec(this)

    fun get(color: Int): ParticleOptions = ColoredParticleEffect(this, color)

    companion object {
        fun create(): ColoredParticleType {
            return ColoredParticleType()
        }
    }
}