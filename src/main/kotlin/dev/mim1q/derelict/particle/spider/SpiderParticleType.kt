package dev.mim1q.derelict.particle.spider

import com.mojang.serialization.MapCodec
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.core.particles.ParticleType
import net.minecraft.core.Direction

class SpiderParticleType : ParticleType<SpiderParticleEffect>(true) {
    override fun streamCodec(): StreamCodec<in RegistryFriendlyByteBuf, SpiderParticleEffect> =
        SpiderParticleEffect.PACKET_CODEC

    override fun codec(): MapCodec<SpiderParticleEffect> = SpiderParticleEffect.CODEC

    fun get(direction: Direction) = SpiderParticleEffect(this, direction)

    companion object {
        fun create() = SpiderParticleType()
    }
}