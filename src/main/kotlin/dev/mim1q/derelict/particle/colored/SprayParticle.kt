package dev.mim1q.derelict.particle.colored

import net.minecraft.client.particle.ParticleRenderType
import net.minecraft.client.multiplayer.ClientLevel

class SprayParticle(
    world: ClientLevel,
    x: Double,
    y: Double,
    z: Double,
    velocityX: Double,
    velocityY: Double,
    velocityZ: Double,
    color: Int
) : ColoredParticle(world, x, y, z, velocityX, velocityY, velocityZ, color) {
    init {
        this.xd = velocityX
        this.yd = velocityY
        this.zd = velocityZ
        this.quadSize = 0.1F + world.getRandom().nextFloat() * 0.1F
        this.lifetime = 30
        val multiplier = 1.0f - random.nextFloat() * 0.25f
        rCol *= multiplier
        gCol *= multiplier
        bCol *= multiplier
    }

    override fun tick() {
        super.tick()
        val progress = age / lifetime.toFloat()
        friction = 1.0f - progress * 0.2f
        setSpriteFromAge(this.spriteProvider)
    }

    override fun getRenderType(): ParticleRenderType = ParticleRenderType.PARTICLE_SHEET_OPAQUE
}