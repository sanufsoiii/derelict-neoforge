package dev.mim1q.derelict.particle.spider

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import dev.mim1q.derelict.init.ModParticles
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.particles.ParticleType
import net.minecraft.core.Direction

class SpiderParticleEffect(
    private val type: ParticleType<SpiderParticleEffect>,
    val direction: Direction,
) : ParticleOptions {
    override fun getType(): ParticleType<SpiderParticleEffect> = type

    companion object {
        val CODEC: MapCodec<SpiderParticleEffect> = RecordCodecBuilder.mapCodec {
            it.group(
                Direction.CODEC.fieldOf("direction").forGetter { it.direction }
            ).apply(it) { dir -> SpiderParticleEffect(ModParticles.SPIDER, dir) }
        }

        val PACKET_CODEC: StreamCodec<RegistryFriendlyByteBuf, SpiderParticleEffect> = StreamCodec.composite(
            Direction.STREAM_CODEC, { it.direction },
            { direction -> SpiderParticleEffect(ModParticles.SPIDER, direction) }
        )
    }
}