package dev.mim1q.derelict.particle.colored

import net.fabricmc.fabric.api.client.particle.v1.FabricSpriteProvider
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry.PendingParticleFactory
import net.minecraft.client.particle.Particle
import net.minecraft.client.particle.ParticleProvider
import net.minecraft.client.particle.TextureSheetParticle
import net.minecraft.client.particle.SpriteSet
import net.minecraft.client.multiplayer.ClientLevel


abstract class ColoredParticle(
    world: ClientLevel,
    x: Double,
    y: Double,
    z: Double,
    velocityX: Double,
    velocityY: Double,
    velocityZ: Double,
    color: Int,
) : TextureSheetParticle(world, x, y, z, velocityX, velocityY, velocityZ) {
    protected var spriteProvider: SpriteSet? = null

    init {
        val r = color shr 16
        val g = color shr 8 and 0xFF
        val b = color and 0xFF
        rCol = r / 255.0f
        gCol = g / 255.0f
        bCol = b / 255.0f
    }

    fun interface ColoredParticleConstructor {
        fun create(
            world: ClientLevel,
            x: Double,
            y: Double,
            z: Double,
            velocityX: Double,
            velocityY: Double,
            velocityZ: Double,
            color: Int
        ): ColoredParticle
    }

    class Factory(private val spriteProvider: SpriteSet, private val constructor: ColoredParticleConstructor) :
        ParticleProvider<ColoredParticleEffect> {
        override fun createParticle(
            parameters: ColoredParticleEffect,
            world: ClientLevel,
            x: Double,
            y: Double,
            z: Double,
            velocityX: Double,
            velocityY: Double,
            velocityZ: Double
        ): Particle {
            val particle = constructor.create(world, x, y, z, velocityX, velocityY, velocityZ, parameters.color)
            particle.pickSprite(spriteProvider)
            particle.spriteProvider = spriteProvider
            return particle
        }
    }

    companion object {
        fun createFactory(constructor: ColoredParticleConstructor): PendingParticleFactory<ColoredParticleEffect> {
            return PendingParticleFactory { spriteProvider: FabricSpriteProvider ->
                Factory(
                    spriteProvider,
                    constructor
                )
            }
        }
    }
}