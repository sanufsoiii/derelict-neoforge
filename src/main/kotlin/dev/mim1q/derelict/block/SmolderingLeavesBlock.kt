package dev.mim1q.derelict.block

import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.LeavesBlock
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.core.BlockPos
import net.minecraft.util.RandomSource
import net.minecraft.world.level.Level

class SmolderingLeavesBlock(settings: Properties) : LeavesBlock(
    settings.randomTicks().lightLevel { 4 }.emissiveRendering { _, _, _ -> true }
) {
    override fun animateTick(state: BlockState, world: Level, pos: BlockPos, random: RandomSource) {
        super.animateTick(state, world, pos, random)
        if (random.nextFloat() < 0.1f) {
            world.addParticle(
                ParticleTypes.FLAME,
                pos.x + random.nextDouble(),
                pos.y + random.nextDouble(),
                pos.z + random.nextDouble(),
                0.0,
                0.01,
                0.0
            )
        }
    }
}