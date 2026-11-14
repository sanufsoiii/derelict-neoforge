package dev.mim1q.derelict.entity

import dev.mim1q.derelict.entity.damage.DerelictDamageSource
import dev.mim1q.derelict.init.ModBlocksAndItems
import dev.mim1q.derelict.util.entity.nullableTracked
import dev.mim1q.derelict.util.entity.tracked
import dev.mim1q.derelict.vendor.gimm1q.interpolation.AnimatedProperty
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.entity.animal.Bucketable
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal
import net.minecraft.world.entity.ai.goal.RandomStrollGoal
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.damagesource.DamageSources
import net.minecraft.world.damagesource.DamageTypes
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.world.entity.monster.Monster
import net.minecraft.world.entity.monster.Spider
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag
import net.minecraft.core.particles.BlockParticleOption
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.InteractionResult
import net.minecraft.world.InteractionHand
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.Level
import java.util.*

sealed class SpiderlingEntity(entityType: EntityType<SpiderlingEntity>, world: Level) : Spider(entityType, world),
    Bucketable {
    companion object {
        val ANCHOR_POSITION: EntityDataAccessor<Optional<BlockPos>> =
            SynchedEntityData.defineId(SpiderlingEntity::class.java, EntityDataSerializers.OPTIONAL_BLOCK_POS)
        val SPAWNED_FROM_BUCKET: EntityDataAccessor<Boolean> =
            SynchedEntityData.defineId(SpiderlingEntity::class.java, EntityDataSerializers.BOOLEAN)

        fun createSpiderlingAttributes(): AttributeSupplier.Builder {
            return createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 2.0)
                .add(Attributes.ATTACK_DAMAGE, 2.0)
                .add(Attributes.MOVEMENT_SPEED, 0.33)
        }

        fun createSpiderlingAllyAttributes(): AttributeSupplier.Builder = createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 2.0)
            .add(Attributes.ATTACK_DAMAGE, 4.0)
            .add(Attributes.MOVEMENT_SPEED, 0.36)
    }

    init {
        noCulling = true
    }

    var anchorPosition: BlockPos? by nullableTracked(ANCHOR_POSITION)
    val anchored = AnimatedProperty(0f)
    private var spawnedFromBucket by tracked(SPAWNED_FROM_BUCKET)

    override fun defineSynchedData(builder: SynchedEntityData.Builder) {
        super.defineSynchedData(builder)
        builder.define(ANCHOR_POSITION, Optional.empty())
        builder.define(SPAWNED_FROM_BUCKET, false)
    }

    override fun registerGoals() {
        goalSelector.addGoal(4, RandomStrollGoal(this, 1.0))
        goalSelector.addGoal(5, WaterAvoidingRandomStrollGoal(this, 1.0))

        goalSelector.addGoal(1, MeleeAttackGoal(this, 1.0, true))
        goalSelector.addGoal(0, LeapAtTargetGoal(this, 0.3f))
        goalSelector.addGoal(1, LookAtPlayerGoal(this, Player::class.java, 8.0f))

        targetSelector.addGoal(0, NearestAttackableTargetGoal(this, Player::class.java, true))
    }

    override fun tick() {
        super.tick()
        if (level().isClientSide) {
            anchored.transitionTo(if (anchorPosition != null) 1f else 0f, 10f)
        } else {
            if (anchorPosition != null) {
                if (!level().getBlockState(blockPosition().below()).isAir) setDeltaMovement(0.0, 0.01, 0.0)
                if (tickCount % 5 == 0 && level().getBlockState(anchorPosition).isAir) anchorPosition = null
            }
        }

        if (anchorPosition != null) {
            val direction = Vec3.atCenterOf(anchorPosition!!).subtract(position()).normalize()
            if (direction.horizontalDistance() > 0.05) {
                push(direction.x * 0.1, 0.0, direction.z * 0.1)
            }
            setNoGravity(true)
            if (deltaMovement.y > 0) setDeltaMovement(deltaMovement.x, deltaMovement.y * 0.99, deltaMovement.z)
        } else {
            setNoGravity(false)
        }
    }

    override fun causeFallDamage(fallDistance: Float, damageMultiplier: Float, damageSource: DamageSource) = false

    override fun hurt(source: DamageSource, amount: Float): Boolean {
        val result =
            super.hurt(source, if (anchorPosition == null || source.entity?.isAlwaysTicking != true) amount else 0f)
        if (!spawnedFromBucket && !level().isClientSide) {
            anchorPosition = null
        }
        deltaMovement = deltaMovement.multiply(1.0, 0.0, 1.0)
        return result
    }

    override fun getPickResult(): ItemStack = ModBlocksAndItems.SPIDERLING_IN_A_BUCKET.defaultInstance

    override fun addAdditionalSaveData(nbt: CompoundTag) {
        super.addAdditionalSaveData(nbt)
        val anchorPosition = anchorPosition
        if (anchorPosition != null) {
            nbt.putLong("AnchorPosition", anchorPosition.asLong())
        }
        nbt.putBoolean("SpawnedFromBucket", spawnedFromBucket)
    }

    override fun readAdditionalSaveData(nbt: CompoundTag) {
        super.readAdditionalSaveData(nbt)
        if (nbt.contains("AnchorPosition")) {
            anchorPosition = BlockPos.of(nbt.getLong("AnchorPosition"))
        }
        spawnedFromBucket = nbt.getBoolean("SpawnedFromBucket")
    }

    override fun fromBucket(): Boolean = spawnedFromBucket
    override fun setFromBucket(fromBucket: Boolean) {
        this.spawnedFromBucket = fromBucket
    }

    @Suppress("DEPRECATION")
    override fun saveToBucketTag(stack: ItemStack) = Bucketable.saveDefaultDataToBucketTag(this, stack)

    @Suppress("DEPRECATION")
    override fun loadFromBucketTag(nbt: CompoundTag) = Bucketable.loadDefaultDataFromBucketTag(this, nbt)
    override fun getBucketItemStack(): ItemStack = ModBlocksAndItems.SPIDERLING_IN_A_BUCKET.defaultInstance
    override fun getPickupSound(): SoundEvent = SoundEvents.SPIDER_STEP

    class Enemy(entityType: EntityType<SpiderlingEntity>, world: Level) : SpiderlingEntity(entityType, world) {
        override fun mobInteract(player: Player, hand: InteractionHand): InteractionResult =
            Bucketable.bucketMobPickup(player, hand, this).orElse(super.mobInteract(player, hand))
    }

    class Ally(entityType: EntityType<SpiderlingEntity>, world: Level) : SpiderlingEntity(entityType, world) {
        var owner: UUID? = null

        private val maxAge = 200 + world.random.nextInt(100)

        override fun tick() {
            super.tick()

            if (!level().isClientSide && tickCount > maxAge) {
                discard()
                (level() as? ServerLevel)?.sendParticles(
                    BlockParticleOption(ParticleTypes.BLOCK, Blocks.COBWEB.defaultBlockState()),
                    position().x, position().y + 0.5, position().z,
                    20,
                    0.2, 0.2, 0.2,
                    0.01
                )
            }
        }

        override fun isInvulnerableTo(damageSource: DamageSource) =
            (owner != null && damageSource.entity?.getUUID() == owner)
                || damageSource.`is`(DamageTypes.IN_WALL)
                || super.isInvulnerableTo(damageSource)


        override fun registerGoals() {
            goalSelector.addGoal(3, RandomLookAroundGoal(this))
            goalSelector.addGoal(1, MeleeAttackGoal(this, 1.0, true))
            goalSelector.addGoal(0, LeapAtTargetGoal(this, 0.3f))

            targetSelector.addGoal(0, HurtByTargetGoal(this))
            targetSelector.addGoal(0, NearestAttackableTargetGoal(this, Monster::class.java, true) {
                it !is Ally
            })
        }

        override fun damageSources(): DamageSources = sources ?: super.damageSources()

        override fun addAdditionalSaveData(nbt: CompoundTag) {
            super.addAdditionalSaveData(nbt)

            if (owner != null) nbt.putUUID("owner", owner!!)
        }

        override fun readAdditionalSaveData(nbt: CompoundTag) {
            super.readAdditionalSaveData(nbt)

            if (nbt.hasUUID("owner")) owner = nbt.getUUID("owner")
        }

        private val sources = createSources()

        private fun createSources(): DamageSources? = level()?.registryAccess()?.let {
            object :
                DamageSources(it) {
                override fun mobAttack(attacker: LivingEntity): DamageSource =
                    DerelictDamageSource.SPIDERLING_ALLY.get(level(), attacker)
            }
        }
    }
}

