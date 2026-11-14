package dev.mim1q.derelict.entity.spider

import dev.mim1q.derelict.Derelict
import dev.mim1q.derelict.entity.boss.ArachneEntity
import dev.mim1q.derelict.init.ModBlocksAndItems
import dev.mim1q.derelict.init.ModEntities
import dev.mim1q.derelict.util.entity.tracked
import dev.mim1q.derelict.vendor.gimm1q.interpolation.Easing
import dev.mim1q.derelict.vendor.gimm1q.screenshake.ScreenShakeUtils
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.targeting.TargetingConditions
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.nbt.CompoundTag
import net.minecraft.core.particles.BlockParticleOption
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundSource
import net.minecraft.sounds.SoundEvents
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.phys.AABB
import net.minecraft.world.level.Level

class ArachneEggEntity(type: EntityType<*>, world: Level) : Entity(type, world) {
    var stage by tracked(STAGE)
        private set


    private var lastAnimationTime = 0
    private var animationTime = 0
    private var stageCooldown = 20
    private var lastTimeWithSpiderInArena = 0L

    override fun defineSynchedData(builder: SynchedEntityData.Builder) {
        builder.define(STAGE, 0)
    }

    override fun tick() {
        super.tick()

        lastAnimationTime = animationTime
        animationTime += stage + 1
        if (stage == 3) {
            blocksBuilding = false
            animationTime = lastAnimationTime

            if (!level().isClientSide() && tickCount % 20 == 0 && Derelict.CONFIG.arachneEggRespawnTime() > 0) {
                if (level().getNearestEntity(
                        ArachneEntity::class.java,
                        TargetingConditions.DEFAULT,
                        null,
                        x, y, z,
                        AABB.ofSize(position(), 20.0, 6.0, 20.0)
                    ) != null
                ) {
                    lastTimeWithSpiderInArena = level().gameTime
                }

                if (level().gameTime - lastTimeWithSpiderInArena > 20 * Derelict.CONFIG.arachneEggRespawnTime()) {
                    breakEggStage()
                }
            }


        } else {
            blocksBuilding = true
        }

        if (stageCooldown > 0) {
            stageCooldown--
        }

        if (!level().isClientSide && tickCount % 5 == 0 && stage < 3) {
            (level() as ServerLevel).sendParticles(
                BlockParticleOption(ParticleTypes.BLOCK, ModBlocksAndItems.SPIDER_EGG_BLOCK.defaultBlockState()),
                this.x,
                this.y + 1.2,
                this.z,
                1 + 2 * stage,
                0.8,
                1.2,
                0.8,
                0.05
            )
        }
    }

    fun getAnimationTime(delta: Float) = Easing.lerp(lastAnimationTime.toFloat(), animationTime.toFloat(), delta)

    override fun hurt(source: DamageSource, amount: Float): Boolean {
        if (level().isClientSide) {
            return super.hurt(source, amount)
        }

        val player = source.entity
        if (stageCooldown <= 0 && player is ServerPlayer) {
            if (stage < 3 && canBreakEggIfAdvancementMet(player, Derelict.CONFIG.arachneEggAdvancement())) {
                breakEggStage()
                return true
            } else {
                return false
            }
        }

        return super.hurt(source, amount)
    }

    private fun canBreakEggIfAdvancementMet(player: ServerPlayer, id: String): Boolean {
        if (id.isBlank()) return true
        val identifier = ResourceLocation.tryParse(id) ?: return true
        val advancement = level().server?.advancements?.get(identifier) ?: return true
        if (player.advancements.getOrStartProgress(advancement).isDone) {
            return true
        } else {
            player.displayClientMessage(
                Component.translatable(
                    "text.derelict.spider_egg_required_advancement",
                    advancement.value.display?.get()?.description ?: ""
                ),
                true
            )
            return false
        }
    }

    private fun breakEggStage() {
        if (stage < 4) {
            stage = (stage + 1) % 4
            stageCooldown = 20

            level().playSound(null, blockPosition(), SoundEvents.SPIDER_HURT, SoundSource.HOSTILE, 0.5f, 0.5f)
            ScreenShakeUtils.shakeAround(
                level() as ServerLevel,
                position(),
                1 + 0.5f * stage,
                if (stage == 3) 70 else 40,
                8.0,
                20.0
            )
            (level() as ServerLevel).sendParticles(
                BlockParticleOption(ParticleTypes.BLOCK, ModBlocksAndItems.SPIDER_EGG_BLOCK.defaultBlockState()),
                this.x,
                this.y + 1.2,
                this.z,
                80,
                0.6,
                1.0,
                0.6,
                0.05
            )

            if (stage == 3) {
                (level() as ServerLevel).sendParticles(
                    ParticleTypes.EXPLOSION_EMITTER,
                    this.x,
                    this.y + 1.2,
                    this.z,
                    3,
                    2.8,
                    2.8,
                    2.8,
                    0.05
                )
                playSound(SoundEvents.DRAGON_FIREBALL_EXPLODE, 1.0f, 1.0f)
                playSound(SoundEvents.SPIDER_DEATH, 2.0f, 0.2f)
                val boss = ArachneEntity(ModEntities.ARACHNE, level())
                boss.spawnPos = blockPosition().south(4)
                boss.setPos(position())
                level().addFreshEntity(boss)
            }

            level().getEntitiesOfClass(LivingEntity::class.java, this.boundingBox.inflate(5.0)) { it !is ArachneEntity }
                .forEach {
                    it.hurt(damageSources().generic(), 1f)
                    it.knockback(stage.toDouble(), x - it.x, z - it.z)
                }
        }
    }

    override fun readAdditionalSaveData(nbt: CompoundTag) {
        stage = nbt.getInt("stage")
    }

    override fun addAdditionalSaveData(nbt: CompoundTag) {
        nbt.putInt("stage", stage)
    }

    override fun isPickable(): Boolean = stage != 3
    override fun canBeCollidedWith(): Boolean = isPickable()
    override fun canCollideWith(other: Entity): Boolean = isPickable()

    companion object {
        private val STAGE: EntityDataAccessor<Int> =
            SynchedEntityData.defineId(ArachneEggEntity::class.java, EntityDataSerializers.INT)
    }
}