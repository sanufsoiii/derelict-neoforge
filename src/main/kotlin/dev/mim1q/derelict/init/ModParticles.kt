package dev.mim1q.derelict.init

import dev.mim1q.derelict.Derelict
import dev.mim1q.derelict.particle.colored.ColoredParticle
import dev.mim1q.derelict.particle.colored.ColoredParticleType
import dev.mim1q.derelict.particle.colored.SprayParticle
import dev.mim1q.derelict.particle.spider.SpiderEmitterParticle
import dev.mim1q.derelict.particle.spider.SpiderParticle
import dev.mim1q.derelict.particle.spider.SpiderParticleType
import net.minecraft.core.Registry
import net.minecraft.core.particles.ParticleType
import net.minecraft.core.particles.SimpleParticleType
import net.minecraft.core.registries.BuiltInRegistries
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent

object ModParticles {
    val SPRAY = register("spray", ColoredParticleType.create())
    val SPIDER = register("spider", SpiderParticleType.create())
    val SPIDER_EMITTER = register("spider_emitter", SimpleParticleType(false))

    fun init() {}

    fun initClient(event: RegisterParticleProvidersEvent) {
        event.registerSpriteSet(SPRAY) { sprites -> ColoredParticle.Factory(sprites, ::SprayParticle) }
        event.registerSpriteSet(SPIDER) { sprites -> SpiderParticle.Factory(sprites) }
        event.registerSpecial(SPIDER_EMITTER, SpiderEmitterParticle.Factory())
    }

    private fun <P, T : ParticleType<P>> register(name: String, particle: T): T =
        Registry.register(BuiltInRegistries.PARTICLE_TYPE, Derelict.id(name), particle)
}
