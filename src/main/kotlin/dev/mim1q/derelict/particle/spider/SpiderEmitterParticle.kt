package dev.mim1q.derelict.particle.spider

import dev.mim1q.derelict.init.ModParticles
import net.minecraft.client.Minecraft
import net.minecraft.client.particle.NoRenderParticle
import net.minecraft.client.particle.Particle
import net.minecraft.client.particle.ParticleProvider
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.core.particles.SimpleParticleType
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction

class SpiderEmitterParticle private constructor(
    world: ClientLevel, x: Double, y: Double, z: Double
) : NoRenderParticle(world, x, y, z) {
    override fun tick() {
        val blockPos = BlockPos.MutableBlockPos(x.toInt(), y.toInt(), z.toInt())
        val player = Minecraft.getInstance().player
        if (player == null) {
            remove()
            return
        }

        val direction = player.position()
            .subtract(x, y, z)
            .multiply(1.0, 0.0, 1.0)
            .normalize()
            .scale(random.nextDouble() * 0.05 + 0.02)

        repeat(10) {
            if (
                level.getBlockState(blockPos).isFaceSturdy(level, blockPos, Direction.UP)
                && level.getBlockState(blockPos.above()).isAir
            ) {
                level.addParticle(
                    ModParticles.SPIDER.get(Direction.DOWN),
                    x, blockPos.y + 1.1, z,
                    direction.x, direction.y, direction.z
                )
                remove()
                return
            }

            blockPos.move(0, -1, 0)
        }
        remove()
    }

    class Factory : ParticleProvider<SimpleParticleType> {
        override fun createParticle(
            defaultParticleType: SimpleParticleType,
            clientWorld: ClientLevel,
            d: Double,
            e: Double,
            f: Double,
            g: Double,
            h: Double,
            i: Double
        ): Particle {
            return SpiderEmitterParticle(clientWorld, d, e, f)
        }
    }
}