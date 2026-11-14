package dev.mim1q.derelict.entity.spider

import dev.mim1q.derelict.entity.spider.control.ArachneBodyControl
import dev.mim1q.derelict.util.extensions.degrees
import dev.mim1q.derelict.vendor.gimm1q.interpolation.AnimatedProperty
import dev.mim1q.derelict.vendor.gimm1q.interpolation.Easing
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.control.BodyRotationControl
import net.minecraft.world.entity.ai.goal.Goal
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.world.entity.monster.Spider
import net.minecraft.sounds.SoundEvents
import net.minecraft.core.BlockPos
import net.minecraft.util.Mth
import net.minecraft.util.Mth.clamp
import net.minecraft.util.Mth.rotLerp
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.Level
import net.minecraft.world.level.LevelReader
import java.util.*
import kotlin.math.atan2
import kotlin.math.max

class JumpingSpiderEntity(
    entityType: EntityType<out JumpingSpiderEntity>,
    world: Level
) : Spider(entityType, world) {
    var jumpAttackCooldown = 40
    val jumpChargeAnimation = AnimatedProperty(0f, Easing::easeOutCubic)

    override fun defineSynchedData(builder: SynchedEntityData.Builder) {
        super.defineSynchedData(builder)
        builder.define(JUMP_CHARGING, false)
        builder.define(TARGET_ID, -1)
    }

    override fun registerGoals() {
        super.registerGoals()

        removeAllGoals {
            it is LeapAtTargetGoal
        }

        goalSelector.addGoal(0, JumpingSpiderJumpAttack())
    }

    override fun playStepSound(pos: BlockPos, state: BlockState) {
        if (!entityData[JUMP_CHARGING]) {
            super.playStepSound(pos, state)
        }
    }

    override fun tick() {
        super.tick()

        if (!level().isClientSide) {
            jumpAttackCooldown = max(0, jumpAttackCooldown - 1)
        } else {
            if (entityData[JUMP_CHARGING]) {
                jumpChargeAnimation.transitionTo(1f, 20f)
            } else {
                jumpChargeAnimation.transitionTo(0f, 20f)
            }
        }

        val isChargingJump = entityData[JUMP_CHARGING]
        val trackedTarget = level().getEntity(entityData[TARGET_ID])

        if (isChargingJump && trackedTarget != null) {
            setPosRaw(Mth.lerp(0.01, xo, x), y, Mth.lerp(0.01, zo, z))
            yBodyRot = rotLerp(
                0.2f,
                yBodyRotO,
                atan2(trackedTarget.z - z, trackedTarget.x - x).degrees().toFloat() - 90f
            )
        }
    }

    override fun setTarget(target: LivingEntity?) {
        super.setTarget(target)
        entityData[TARGET_ID] = target?.id ?: -1
    }

    override fun createBodyControl(): BodyRotationControl = ArachneBodyControl(this, 0.7f)

    companion object {
        val JUMP_CHARGING: EntityDataAccessor<Boolean> =
            SynchedEntityData.defineId(JumpingSpiderEntity::class.java, EntityDataSerializers.BOOLEAN)
        val TARGET_ID: EntityDataAccessor<Int> =
            SynchedEntityData.defineId(JumpingSpiderEntity::class.java, EntityDataSerializers.INT)
    }

    inner class JumpingSpiderJumpAttack : Goal() {
        override fun canUse(): Boolean =
            target != null && target!!.y <= y && jumpAttackCooldown <= 0 && random.nextFloat() < 0.2f

        override fun canContinueToUse(): Boolean = target != null && ticks < 15
        private var ticks = 0

        init {
            this.flags = EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP)
        }

        override fun tick() {
            ++ticks
        }

        override fun stop() {
            val currentTarget = target
            if (currentTarget != null) {
                playSound(SoundEvents.PHANTOM_FLAP, 1.0f, 1.4f)
                deltaMovement = currentTarget.eyePosition.subtract(eyePosition)
                    .let { Vec3(it.x, clamp(it.y, 0.0, 0.2), it.z) }.normalize().scale(2.0).add(0.0, 0.2, 0.0)

                hurtMarked = true
                hasImpulse = true
            }

            entityData[JUMP_CHARGING] = false
            jumpAttackCooldown = 40 + random.nextInt(100)
        }

        override fun start() {
            playSound(SoundEvents.SPIDER_HURT, 1.0f, 1.5f)
            ticks = 0
            entityData[JUMP_CHARGING] = true
        }
    }

    override fun checkSpawnObstruction(world: LevelReader): Boolean = super.checkSpawnObstruction(world) && world.getMaxLocalRawBrightness(blockPosition()) == 0
}