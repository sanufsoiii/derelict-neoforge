package dev.mim1q.derelict.particle.colored

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.particles.ParticleType


@JvmRecord
data class ColoredParticleEffect(val type: ParticleType<*>, val color: Int) : ParticleOptions {
    override fun getType() = type

    companion object {
        fun createMapCodec(type: ParticleType<ColoredParticleEffect>): MapCodec<ColoredParticleEffect> =
            RecordCodecBuilder.mapCodec { it ->
                it.group(
                    Codec.INT.fieldOf("color").forGetter { o -> o.color }
                ).apply(it) { color -> ColoredParticleEffect(type, color) }
            }

        fun createPacketCodec(type: ParticleType<ColoredParticleEffect>): StreamCodec<RegistryFriendlyByteBuf, ColoredParticleEffect> =
            StreamCodec.composite(
                ByteBufCodecs.INT, { it.color },
                { color -> ColoredParticleEffect(type, color) }
            )
    }
}