package dev.mim1q.derelict.entity.projectile

import dev.mim1q.derelict.init.ModStatusEffects
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.projectile.AbstractArrow
import net.minecraft.world.item.ItemStack
import net.minecraft.core.particles.BlockParticleOption
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.Level

class SpiderSilkBolaProjectile(
    entityType: EntityType<SpiderSilkBolaProjectile>,
    world: Level
) : AbstractArrow(entityType, world) {
    override fun onHitEntity(entityHitResult: EntityHitResult) {
        super.onHitEntity(entityHitResult)
        if (!level().isClientSide) {
            (level() as? ServerLevel)?.applyCobwebOnNearbyEntities(
                entityHitResult.entity as? LivingEntity,
                BlockPos.containing(entityHitResult.getLocation())
            )
            discard()
        }
    }

    override fun onHitBlock(blockHitResult: BlockHitResult) {
        super.onHitBlock(blockHitResult)
        if (!level().isClientSide) {
            (level() as? ServerLevel)?.applyCobwebOnNearbyEntities(null, blockHitResult.blockPos)
            discard()
        }
    }

    private fun ServerLevel.applyCobwebOnNearbyEntities(entity: LivingEntity?, pos: BlockPos) {
        entity?.applyCobweb(2, 60, 4f)

        getEntities(this@SpiderSilkBolaProjectile.owner, AABB.ofSize(Vec3.atCenterOf(pos), 5.0, 5.0, 5.0)).forEach {
            (it as? LivingEntity)?.applyCobweb(1, 40, 2f)
        }

        sendParticles(
            BlockParticleOption(ParticleTypes.BLOCK, Blocks.COBWEB.defaultBlockState()),
            this@SpiderSilkBolaProjectile.x,
            this@SpiderSilkBolaProjectile.y + 0.5,
            this@SpiderSilkBolaProjectile.z,
            25,
            0.4,
            0.4,
            0.4,
            0.01
        )
    }

    private fun LivingEntity.applyCobweb(level: Int, duration: Int, damage: Float) {
        addEffect(
            MobEffectInstance(
                ModStatusEffects.COBWEBBED,
                duration,
                level,
                false,
                false,
                false,
            )
        )
        hurt(
            damageSources().mobProjectile(this, this@SpiderSilkBolaProjectile.owner as? LivingEntity ?: return),
            damage
        )

    }

    override fun getDefaultPickupItem(): ItemStack = ItemStack.EMPTY
}