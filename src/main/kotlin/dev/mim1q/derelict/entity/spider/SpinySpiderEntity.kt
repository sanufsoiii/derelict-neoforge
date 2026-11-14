package dev.mim1q.derelict.entity.spider

import dev.mim1q.derelict.init.ModStatusEffects
import dev.mim1q.derelict.vendor.gimm1q.interpolation.Easing
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.monster.Spider
import net.minecraft.world.entity.animal.IronGolem
import net.minecraft.world.entity.player.Player
import net.minecraft.core.particles.ItemParticleOption
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.AABB
import net.minecraft.util.Mth
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.Explosion
import net.minecraft.world.level.ExplosionDamageCalculator
import kotlin.math.max

private const val MAX_FUSE_TIME = 30

class SpinySpiderEntity(
    entityType: EntityType<out Spider>,
    world: Level
) : Spider(entityType, world) {

    override fun registerGoals() {
        super.registerGoals()

        removeAllGoals {
            it is LeapAtTargetGoal
        }

        targetSelector.removeAllGoals { true }

        targetSelector.addGoal(1, HurtByTargetGoal(this, *arrayOfNulls(0)))
        targetSelector.addGoal(2, NearestAttackableTargetGoal(this, Player::class.java, true))
        targetSelector.addGoal(3, NearestAttackableTargetGoal(this, IronGolem::class.java, true))

    }

    override fun defineSynchedData(builder: SynchedEntityData.Builder) {
        super.defineSynchedData(builder)

        builder.define(FUSE_SPEED, -1)
    }

    private var prevFuse = 0f
    private var fuse = 0f

    override fun tick() {
        super.tick()

        val fuseSpeed = entityData[FUSE_SPEED]

        prevFuse = fuse
        fuse = max(fuse + fuseSpeed, 0f)

        if (!level().isClientSide) {
            if (fuseSpeed == -1 && level().getNearestPlayer(this, 2.0) != null) {
                entityData[FUSE_SPEED] = 1
                playSound(
                    SoundEvents.TNT_PRIMED,
                    1.0f,
                    1.0f
                )
            } else if (fuseSpeed == 1 && level().getNearestPlayer(this, 4.0) == null) {
                entityData[FUSE_SPEED] = -1
            }

            if (fuse > MAX_FUSE_TIME) {
                explode()
            }
        }
    }

    override fun hurt(source: DamageSource, amount: Float): Boolean {
        if (source.entity?.isAlwaysTicking == true) {
            source.entity!!.hurt(level().damageSources().thorns(this), Mth.clamp(amount * 0.2f, 1.0f, 5.0f))
        }
        return super.hurt(source, amount)
    }

    private fun explode() {
        dead = true
        discard()
        level().explode(
            this,
            null,
            object : ExplosionDamageCalculator() {
                override fun shouldBlockExplode(
                    explosion: Explosion,
                    world: BlockGetter,
                    pos: BlockPos,
                    state: BlockState,
                    power: Float
                ): Boolean = false
            },
            position().add(0.0, 0.5, 0.0),
            1.5f,
            false,
            Level.ExplosionInteraction.MOB
        )

        (level() as? ServerLevel)?.sendParticles(
            ItemParticleOption(ParticleTypes.ITEM, Blocks.COBWEB.asItem().defaultInstance),
            position().x + 0.5,
            position().y + 0.5,
            position().z + 0.5,
            40,
            0.5,
            0.5,
            0.5,
            0.3
        )

        level().getEntities(this, AABB.ofSize(position(), 4.0, 4.0, 4.0)) { it.distanceTo(this) <= 4.0 }.forEach {
            if (it is LivingEntity && it !is SpinySpiderEntity) {
                val currentLevel = it.getEffect(ModStatusEffects.COBWEBBED)?.amplifier ?: -1

                it.addEffect(
                    MobEffectInstance(
                        ModStatusEffects.COBWEBBED,
                        60,
                        (currentLevel + 1).coerceAtMost(2),
                        true,
                        true,
                        false
                    )
                )
            }
        }
    }

    fun getFuse(tickDelta: Float) = Easing.lerp(prevFuse, fuse, tickDelta) / (MAX_FUSE_TIME - 2f)

    companion object {
        private val FUSE_SPEED: EntityDataAccessor<Int> =
            SynchedEntityData.defineId(SpinySpiderEntity::class.java, EntityDataSerializers.INT)
    }
}