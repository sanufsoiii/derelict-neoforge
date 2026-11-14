package dev.mim1q.derelict.block.cobweb

import com.mojang.serialization.MapCodec
import dev.mim1q.derelict.init.ModBlocksAndItems
import dev.mim1q.derelict.init.ModParticles
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.MultifaceBlock
import net.minecraft.world.entity.Entity
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.phys.Vec3
import net.minecraft.util.RandomSource
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelAccessor

class FancyCobwebWithSpiderNestBlock(settings: Properties) : FancyCobwebBlock(settings, true) {
    private fun spawnParticle(
        state: BlockState,
        world: LevelAccessor,
        blockPos: BlockPos,
        random: RandomSource,
        chance: Float,
        speed: Double
    ) {
        if (random.nextFloat() > chance) return
        val vel = Vec3(random.nextDouble() - 0.5, 0.0, random.nextDouble() - 0.5)
            .normalize()
            .scale((random.nextDouble() + 1) * speed)
        val directions = getDirections(state)
        if (directions.isEmpty()) return
        val direction = directions.toList()[random.nextInt(directions.size)]
        val pos = Vec3.atCenterOf(blockPos).add(Vec3.atLowerCornerOf(direction.normal).scale(0.49))
        world.addParticle(ModParticles.SPIDER.get(direction), pos.x, pos.y, pos.z, vel.x, vel.y, vel.z)
    }

    private fun getDirections(state: BlockState) =
        Direction.entries.filter { state.getValue(getFaceProperty(it)) }.toList()

    override fun animateTick(state: BlockState, world: Level, pos: BlockPos, random: RandomSource) {
        spawnParticle(state, world, pos, random, 0.5F, 0.03)
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun entityInside(state: BlockState, world: Level, pos: BlockPos, entity: Entity) {
        if (world.isClientSide && entity.showVehicleHealth()) {
            val times = if (entity.xo != entity.x || entity.yo != entity.y || entity.zo != entity.z) 4 else 2
            repeat(times) {
                spawnParticle(state, world, pos, world.random, 0.5F, 0.05)
            }
        }
        super.entityInside(state, world, pos, entity)
    }

    override fun codec(): MapCodec<out MultifaceBlock> = MapCodec.unit(ModBlocksAndItems.FANCY_COBWEB_WITH_SPIDER_NEST)

    override fun destroy(world: LevelAccessor, pos: BlockPos, state: BlockState) {
        super.destroy(world, pos, state)
        repeat(16) {
            spawnParticle(state, world, pos, world.random, 1.0F, 0.07)
        }
    }
}