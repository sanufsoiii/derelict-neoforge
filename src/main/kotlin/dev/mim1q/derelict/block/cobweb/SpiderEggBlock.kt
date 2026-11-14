package dev.mim1q.derelict.block.cobweb

import com.mojang.serialization.MapCodec
import dev.mim1q.derelict.init.ModBlockEntities
import dev.mim1q.derelict.init.ModBlocksAndItems
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.core.BlockPos
import net.minecraft.util.RandomSource
import net.minecraft.world.level.Level

class SpiderEggBlock(
    settings: Properties,
    val big: Boolean = false,
    private val entityType: EntityType<out LivingEntity>? = null,
    private val count: (RandomSource) -> Int = { 1 },
    private val ticks: Boolean = entityType != null,
) : BaseEntityBlock(settings) {
    override fun codec(): MapCodec<out BaseEntityBlock> = MapCodec.unit(ModBlocksAndItems.SPIDER_EGG)

    override fun newBlockEntity(pos: BlockPos, state: BlockState): BlockEntity = SpiderEggBlockEntity(pos, state)

    override fun <T : BlockEntity> getTicker(
        world: Level,
        state: BlockState,
        type: BlockEntityType<T>,
    ): BlockEntityTicker<T>? = if (!ticks) null else
        BlockEntityTicker<T> { entityWorld, entityPos, entityState, entity ->
            if (entity is SpiderEggBlockEntity) {
                entity.tick(entityWorld, entityPos, entityState)
            }
        }

    class SpiderEggBlockEntity(pos: BlockPos, state: BlockState) :
        BlockEntity(ModBlockEntities.SPIDER_EGG_BLOCK_ENTITY, pos, state) {

        fun tick(world: Level, pos: BlockPos, state: BlockState) {
            if (
                world.gameTime % 10L == (pos.hashCode().toLong() % 10L)
                && world.getNearestPlayer(pos.x + 0.5, pos.y.toDouble(), pos.z + 0.5, 5.0, true) != null
            ) {
                (state.block as? SpiderEggBlock)?.let { block ->
                    val random = world.random
                    if (block.entityType != null) {
                        repeat(block.count(random)) {
                            val entity = block.entityType.create(world) ?: return@let
                            entity.setPosRaw(pos.x + 0.5, pos.y + 0.5, pos.z + 0.5)
                            world.addFreshEntity(entity)
                        }
                    }
                }

                world.destroyBlock(pos, false)
            }
        }
    }
}