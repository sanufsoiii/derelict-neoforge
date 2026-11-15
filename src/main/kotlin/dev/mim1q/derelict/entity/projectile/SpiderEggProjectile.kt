package dev.mim1q.derelict.entity.projectile

import dev.mim1q.derelict.entity.SpiderlingEntity
import dev.mim1q.derelict.init.ModEntities
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.ai.targeting.TargetingConditions
import net.minecraft.world.entity.monster.Monster
import net.minecraft.world.entity.projectile.AbstractArrow
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.Level

class SpiderEggProjectile(
    type: EntityType<out AbstractArrow>,
    world: Level
) : AbstractArrow(type, world) {
    override fun onHitEntity(entityHitResult: EntityHitResult) {
        super.onHitEntity(entityHitResult)
        explodeIntoSpiderling(entityHitResult.getLocation())
    }

    override fun onHitBlock(blockHitResult: BlockHitResult) {
        super.onHitBlock(blockHitResult)
        explodeIntoSpiderling(blockHitResult.getLocation())
    }

    private fun explodeIntoSpiderling(pos: Vec3) {
        if (level().isClientSide) return

        (level() as? ServerLevel)?.sendParticles(
            ParticleTypes.EXPLOSION,
            pos.x, pos.y + 1.0, pos.z,
            3, 0.8, 0.8, 0.8,
            0.01
        )

        level().getEntities(this, AABB.ofSize(pos, 4.0, 4.0, 4.0)) {
            it !is SpiderlingEntity.Ally
        }.forEach {
            it.hurt(damageSources().explosion(this, this.owner), 6.0f)
        }

        val spiderling = ModEntities.SPIDERLING_ALLY.create(level()) ?: return
        spiderling.setPos(pos)
        spiderling.moveTo(spiderling.x, spiderling.y, spiderling.z, random.nextFloat() * 360f, 0.0f)
        (spiderling as? SpiderlingEntity.Ally)?.owner = owner?.getUUID()

        level().getNearestEntity(
            level().getEntitiesOfClass(
                Monster::class.java,
                AABB.ofSize(pos, 4.0, 4.0, 4.0)
            ) {
                it !is SpiderlingEntity.Ally
            }, TargetingConditions.DEFAULT, spiderling, x, y, z
        )?.let {
            spiderling.target = it
        }

        level().addFreshEntity(spiderling)
        discard()
    }

    override fun getDefaultHitGroundSoundEvent(): SoundEvent = SoundEvents.ITEM_PICKUP

    // AbstractArrow#addAdditionalSaveData always writes this stack; an empty one throws on 1.21.1.
    // Pickup stays DISALLOWED, so this only supplies a valid NBT value.
    override fun getDefaultPickupItem(): ItemStack = ItemStack(Items.SPIDER_EYE)
}