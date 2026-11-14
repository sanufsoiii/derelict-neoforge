package dev.mim1q.derelict.entity.boss

import dev.mim1q.derelict.entity.goal.ReturnGoal
import dev.mim1q.derelict.entity.goal.TickingGoal
import dev.mim1q.derelict.entity.spider.control.ArachneBodyControl
import dev.mim1q.derelict.entity.spider.legs.SpiderLegController
import dev.mim1q.derelict.init.ModEntities
import dev.mim1q.derelict.tag.ModBlockTags
import dev.mim1q.derelict.tag.ModEntityTags
import dev.mim1q.derelict.util.entity.trackedEnum
import dev.mim1q.derelict.util.extensions.radians
import dev.mim1q.derelict.util.pickWeightedRandom
import dev.mim1q.derelict.util.wrapDegrees
import dev.mim1q.derelict.vendor.gimm1q.interpolation.AnimatedProperty
import dev.mim1q.derelict.vendor.gimm1q.interpolation.Easing
import dev.mim1q.derelict.vendor.gimm1q.screenshake.ScreenShakeUtils
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.MoverType
import net.minecraft.world.entity.Pose
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.BossEvent
import net.minecraft.server.level.ServerBossEvent
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.monster.Monster
import net.minecraft.world.entity.player.Player
import net.minecraft.nbt.CompoundTag
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundSource
import net.minecraft.sounds.SoundEvents
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.Level
import net.minecraft.world.level.Explosion
import net.minecraft.world.level.ExplosionDamageCalculator
import kotlin.math.cos
import kotlin.math.sin

class ArachneEntity(entityType: EntityType<ArachneEntity>, world: Level) : Monster(entityType, world) {
    init {
        noCulling = true
    }

    var spawnPos: BlockPos? = null

    private val bossBar = ServerBossEvent(name, BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_12)

    val legController = SpiderLegController(
        this,
        { scale * 24 / 16f },
        { scale * 28 / 16f },
        //@formatter:off
        { ikVec( 0.6, 0.9, 1.0 ) } to { ikVec( 1.5, 0.0,  3.5) },
        { ikVec( 0.6, 1.0, 0.65) } to { ikVec( 2.5, 0.0,  1.5) },
        { ikVec( 0.6, 1.1, 0.3 ) } to { ikVec( 2.0, 0.0,  0.5) },
        { ikVec( 0.6, 1.2, 0.0 ) } to { ikVec( 1.5, 0.0, -1.0) },

        { ikVec(-0.6, 0.9, 1.0 ) } to { ikVec(-1.5, 0.0,  3.5) },
        { ikVec(-0.6, 1.0, 0.65) } to { ikVec(-2.5, 0.0,  1.5) },
        { ikVec(-0.6, 1.1, 0.3 ) } to { ikVec(-2.0, 0.0, -0.5) },
        { ikVec(-0.6, 1.2, 0.0 ) } to { ikVec(-1.5, 0.0, -1.0) },
        //@formatter:on
    )

    val legsRaisedAnimation = AnimatedProperty(0f, Easing::easeOutBack)
    val shakingAnimation = AnimatedProperty(0f, Easing::easeInOutCubic)

    val leftLegStomp = AnimatedProperty(0f, Easing::easeInOutCubic)
    val rightLegStomp = AnimatedProperty(0f, Easing::easeInOutCubic)

    val fangsAnimation = AnimatedProperty(0f, Easing::easeInOutCubic)
    val deathAnimation = AnimatedProperty(0f, Easing::easeInOutCubic)

    private var screamingTicks = 0
    private var wasScreaming = false
    private var goalsSetup = false
    private var lastStage = 0

    private var currentAttack by trackedEnum(CURRENT_ATTACK, ArachneAttackType.entries)
    private var attackTicks = 0

    private var stompCooldown = 100
    private var spawnCooldown = 600
    private var rushCooldown = 400
    private var shootCooldown = 80

    private var forceSpawnAttack = false
    private var playerInFangsTimer = 0

    override fun registerGoals() {
        if (tickCount < 40 || goalsSetup) return

        goalsSetup = true

        goalSelector.removeAllGoals { true }

        goalSelector.addGoal(4, RandomLookAroundGoal(this))
        goalSelector.addGoal(3, LookAtPlayerGoal(this, Player::class.java, 24F))
        goalSelector.addGoal(2, MeleeAttackGoal(this, 0.6, true))

        goalSelector.addGoal(1, createSmashAttack())
        goalSelector.addGoal(1, createSpawnAttack())
        goalSelector.addGoal(1, createRushAttack())
        goalSelector.addGoal(1, createShootAttack())

        goalSelector.addGoal(0, ReturnGoal(this, { !arePlayersNearby() }, { spawnPos }))

        if (targetSelector.availableGoals.isEmpty()) {
            targetSelector.addGoal(0, NearestAttackableTargetGoal(this, Player::class.java, false))
        }
    }

    override fun tick() {
        if (level().isClientSide) {
            legController.tick()
            handleAnimations()
        } else {
            if (tickCount == 45) {
                registerGoals()
            }

            if (tickCount % 5 == 0) {
                val passenger = firstPassenger as? Player
                if (passenger != null) {
                    passenger.hurt(
                        damageSources().mobAttack(this),
                        getAttributeValue(Attributes.ATTACK_DAMAGE).toFloat()
                    )
                    if (playerInFangsTimer == 30) {
                        playSound(SoundEvents.PLAYER_ATTACK_CRIT, 1.0f, 0.8f)
                        passenger.addEffect(
                            MobEffectInstance(
                                MobEffects.POISON,
                                80,
                                1
                            )
                        )
                    }
                    playerInFangsTimer += 5
                } else {
                    playerInFangsTimer = 0
                }
            }

            if (!dead) {
                processScreaming()
            }
            bossBar.progress = health / maxHealth

            stompCooldown--
            spawnCooldown--
            rushCooldown--
            shootCooldown--

            val currentStage = getStage().coerceAtLeast(lastStage)
            if (lastStage != currentStage) {
                (level() as? ServerLevel)?.sendParticles(
                    ParticleTypes.EXPLOSION_EMITTER,
                    position().x, position().y, position().z,
                    8, 2.0, 2.0, 2.0,
                    0.0
                )
                playSound(SoundEvents.GENERIC_EXPLODE.value(), 1.0f, 0.8f)
                level().getEntitiesOfClass(LivingEntity::class.java, boundingBox.inflate(8.0)) { it != this }.forEach {
                    it.knockback(1.0, it.x - x, it.z - z)
                }
                if (lastStage < currentStage) {
                    changeStage(currentStage)
                }

                lastStage = currentStage
            }

            if (!arePlayersNearby()) {
                if (tickCount % 20 == 0) {
                    heal(0.5f)
                }
            }
        }

        attackTicks++
        super.tick()
    }

    private fun processScreaming() {
        wasScreaming = screamingTicks > 0
        screamingTicks--

        if (tickCount == 20) {
            screamingTicks = 40
        }
        entityData.set(SCREAMING, screamingTicks > 0)
        entityData.set(SHAKING, screamingTicks > 0)

        if (!wasScreaming && screamingTicks > 0) {
            ScreenShakeUtils.shakeAround(level() as ServerLevel, position(), 1.2f, screamingTicks, 5.0, 30.0)
            level().playSound(null, this.blockPosition(), SoundEvents.SPIDER_AMBIENT, SoundSource.HOSTILE, 1.3f, 0.3f)
        }
    }

    private fun handleAnimations() {
        if (shouldShake()) {
            shakingAnimation.transitionTo(1f, 5f, Easing::easeInOutCubic)
        } else {
            shakingAnimation.transitionTo(0f, 20f, Easing::easeInOutCubic)
        }

        if (shouldRaiseLegs()) {
            legsRaisedAnimation.transitionTo(1f, 15f, Easing::easeOutBack)
        } else {
            legsRaisedAnimation.transitionTo(0f, 40f, Easing::easeInOutCubic)
        }

        if (shouldRaiseFangs()) {
            fangsAnimation.transitionTo(1f, 15f, Easing::easeOutBack)
        } else {
            fangsAnimation.transitionTo(0f, 15f, Easing::easeInOutCubic)
        }

        if (currentAttack == ArachneAttackType.SMASH) {
            if (attackTicks > getStompWait()) {
                val interval = getStompInterval()
                when (attackTicks % interval) {
                    1 -> leftLegStomp.transitionTo(1f, interval / 2f, Easing::easeOutBounce)
                    (interval / 2 - 1) -> leftLegStomp.transitionTo(0f, interval.toFloat(), Easing::easeInOutCubic)
                    (interval / 2 + 1) -> rightLegStomp.transitionTo(1f, interval / 2f, Easing::easeOutBounce)
                    (interval - 1) -> rightLegStomp.transitionTo(0f, interval.toFloat(), Easing::easeInOutCubic)
                }
            }
        } else {
            leftLegStomp.transitionTo(0f, 10f, Easing::easeInOutCubic)
            rightLegStomp.transitionTo(0f, 10f, Easing::easeInOutCubic)
        }
    }

    private fun shouldShake() = when {
        entityData[SHAKING] -> true
        currentAttack == ArachneAttackType.SPAWN && attackTicks < 100 -> true
        else -> false
    }

    private fun shouldRaiseLegs() = when {
        entityData[SCREAMING] -> true
        currentAttack == ArachneAttackType.SMASH -> true
        else -> false
    }

    private fun shouldRaiseFangs() = when {
        currentAttack == ArachneAttackType.SHOOT -> true
        else -> false
    }

    override fun setYBodyRot(bodyYaw: Float) = super.setYBodyRot(wrapDegrees(this.yBodyRot, bodyYaw, 10f))
    override fun createBodyControl() = ArachneBodyControl(this)
    override fun defineSynchedData(builder: SynchedEntityData.Builder) {
        super.defineSynchedData(builder)

        builder.define(SCREAMING, false)
        builder.define(SHAKING, false)
        builder.define(CURRENT_ATTACK, 0)
    }

    override fun onSyncedDataUpdated(data: EntityDataAccessor<*>) {
        if (data == CURRENT_ATTACK) {
            attackTicks = 0
        }
        super.onSyncedDataUpdated(data)
    }

    private fun getStage() = when (health / maxHealth) {
        in 0.0..0.25 -> 3
        in 0.25..0.5 -> 2
        in 0.5..0.75 -> 1
        else -> 0
    }

    private fun <T> stageBasedValue(stage0: T, stage1: T, stage2: T, stage3: T) = when (getStage()) {
        0 -> stage0
        1 -> stage1
        2 -> stage2
        else -> stage3
    }

    private fun getStompWait() = stageBasedValue(60, 55, 40, 20)
    private fun getStompInterval() = stageBasedValue(20, 16, 14, 10)

    private fun changeStage(stage: Int) {
        forceSpawnAttack = true

        spawnCooldown = 80
        stompCooldown = 80
        rushCooldown = 80
        shootCooldown = 80

        screamingTicks = 40
    }


    override fun getAgeScale() = 1.0f + 0.05f * getStage()

    override fun getLocalBoundsForPose(pose: Pose): AABB =
        super.getLocalBoundsForPose(pose).let {
            val scale = (getScale() - 1) * 0.5
            it.inflate(it.xsize * scale, it.ysize * scale, it.zsize * scale)
        }

    private fun ikVec(x: Double, y: Double, z: Double): Vec3 {
        val scale = getScale()
        return Vec3(x * scale, y * scale, z * scale)
    }

    override fun startSeenByPlayer(player: ServerPlayer) {
        super.startSeenByPlayer(player)
        bossBar.addPlayer(player)
    }

    override fun stopSeenByPlayer(player: ServerPlayer) {
        super.stopSeenByPlayer(player)
        bossBar.removePlayer(player)
    }

    override fun makeStuckInBlock(state: BlockState, multiplier: Vec3) =
        if (!state.`is`(ModBlockTags.COBWEBS)) super.makeStuckInBlock(state, multiplier) else Unit

    private fun setGlobalCooldown() {
        val cooldown = stageBasedValue(30, 25, 20, 15)

        spawnCooldown = spawnCooldown.coerceAtLeast(cooldown)
        stompCooldown = stompCooldown.coerceAtLeast(cooldown)
        rushCooldown = rushCooldown.coerceAtLeast(cooldown)
    }

    override fun die(damageSource: DamageSource) {
        super.die(damageSource)

        (level() as? ServerLevel)?.let {
            ScreenShakeUtils.shakeAround(it, position(), 1.5f, 60, 5.0, 30.0)
        }
    }

    override fun tickDeath() {
        ++this.deathTime
        if (level().isClientSide) {
            deathAnimation.transitionTo(1f, 60f)
        } else {
            entityData.set(SHAKING, deathTime < 60)
        }

        if (this.deathTime >= 100 && !level().isClientSide() && !this.isRemoved) {
            if (deathTime == 100) {
                playSound(SoundEvents.SPIDER_DEATH, 1f, 0.1f)
                playSound(SoundEvents.SPIDER_DEATH, 1f, 0.2f)
                playSound(SoundEvents.SPIDER_DEATH, 1f, 0.3f)
            }

            level().broadcastEntityEvent(this, 60.toByte())
            this.remove(RemovalReason.KILLED)
        }
    }

    private fun createSmashAttack() = createArachneAttack(
        ArachneAttackType.SMASH,
        120,
        tick@{ ticks ->
            val world = this@ArachneEntity.level() as? ServerLevel ?: return@tick

            val interval = getStompInterval()
            if (ticks > getStompWait() && (ticks % interval == interval / 4 || ticks % interval == interval * 3 / 4)) {
                val target = target
                if (target != null) {
                    navigation.moveTo(target, stageBasedValue(0.5, 0.6, 0.65, 0.7))
                }

                ScreenShakeUtils.shakeAround(world, position(), 1f, 20, 4.0, 16.0)
                val bodyYawRad = (yBodyRot + 90).radians()
                val scale = getScale()
                val pos = position().add(3.0 * cos(bodyYawRad) * scale, 0.0, 3.0 * sin(bodyYawRad) * scale)

                world.sendParticles(ParticleTypes.EXPLOSION, pos.x, pos.y + 0.5, pos.z, 2, 0.3, 0.3, 0.3, 0.0)
                playSound(SoundEvents.GENERIC_EXPLODE.value(), 0.7f, 0.3f + random.nextFloat() * 0.2f)

                world.getEntities(this, AABB.ofSize(pos, 5.0, 4.0, 5.0)).forEach {
                    if (it !is LivingEntity) return@forEach

                    it.hurt(
                        damageSources().mobAttack(this),
                        getAttributeValue(Attributes.ATTACK_DAMAGE).toFloat() * 1.5f
                    )
                    it.knockback(0.1, x - it.x, z - it.z)
                }
            }
        },
        { this.stompCooldown <= 0 && random.nextFloat() < 0.1f },
        { this.stompCooldown = 200 }
    )

    private fun createSpawnAttack() = createArachneAttack(
        ArachneAttackType.SPAWN,
        300,
        tick@{ ticks ->
            if (ticks <= 80 && ticks % 10 == 0) {
                val entityType = mapOf(
                    ModEntities.SPIDERLING to 80,
                    EntityType.SPIDER to 6,
                    ModEntities.JUMPING_SPIDER to 4,
                    EntityType.CAVE_SPIDER to 2,
                    ModEntities.SPINY_SPIDER to 2,
                    ModEntities.WEB_CASTER to 1,
                ).pickWeightedRandom(random) ?: return@tick

                val world = this@ArachneEntity.level() as? ServerLevel ?: return@tick
                val entity = entityType.create(world) ?: return@tick

                entity.setPos(position().x + random.nextDouble() - 0.5, position().y + 0.75, position().z + random.nextDouble() - 0.5)
                if (target != null) {
                    entity.target = target
                }
                world.addFreshEntity(entity)
            }
        },
        {
            forceSpawnAttack || (spawnCooldown <= 0 && random.nextFloat() < 0.01f && level().getEntities(
                this,
                AABB.ofSize(position(), 20.0, 20.0, 20.0)
            ) { it.type.`is`(ModEntityTags.SPAWNS_SPIDERLINGS_ON_DEATH) }.size <= 3)
        },
        {
            spawnCooldown = 1200
            forceSpawnAttack = false
        }
    )

    private fun createRushAttack() = createArachneAttack(
        ArachneAttackType.RUSH,
        50,
        tick@{ ticks ->
            (level() as? ServerLevel)?.sendParticles(
                ParticleTypes.LARGE_SMOKE,
                position().x, position().y + 0.5, position().z,
                3, 0.5, 0.2, 0.5, 0.01
            )

            if (ticks < 30) {
                return@tick
            }
            if (ticks == 30) {
                push(0.0, 0.5, 0.0)
            }

            val bodyYawRad = (yBodyRot + 90).radians()
            move(MoverType.SELF, Vec3(cos(bodyYawRad) * 0.7, 0.0, sin(bodyYawRad) * 0.7))
        },
        { rushCooldown <= 0 && random.nextFloat() < 0.1f },
        {
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
                2f,
                false,
                Level.ExplosionInteraction.MOB
            )
            rushCooldown = stageBasedValue(200, 160, 130, 100)
            setGlobalCooldown()
        },
        { ticks -> !(ticks > 40 && (xo == x && zo == z)) }
    )

    private fun createShootAttack() = createArachneAttack(
        ArachneAttackType.SHOOT,
        40,
        tick@{ ticks ->
            val target = target
            if (ticks == 30 && target != null) {
                val projectile = ModEntities.SPIDER_SILK_BOLA.create(level()) ?: return@tick
                val diff = target.position().add(0.0, 1.5, 0.0).subtract(position()).normalize()
                projectile.setDeltaMovement(diff.x, diff.y + 0.03, diff.z)
                projectile.setPos(position().add(0.0, 0.5, 0.0).add(lookAngle))
                projectile.owner = this@ArachneEntity
                level().addFreshEntity(projectile)
            }
        },
        { shootCooldown <= 0 && random.nextFloat() < 0.1f && (target != null && target!!.distanceTo(this) > 6.0) },
        { shootCooldown = stageBasedValue(110, 100, 90, 80) }
    )

    private fun createArachneAttack(
        type: ArachneAttackType,
        duration: Int,
        onTick: (ticks: Int) -> Unit,
        startPredicate: () -> Boolean,
        cooldownSetter: () -> Unit = { },
        shouldContinue: (ticks: Int) -> Boolean = { true },
    ) = object : TickingGoal(
        duration,
        onTick,
        {
            currentAttack = type
            this@ArachneEntity.navigation.stop()
        },
        {
            currentAttack = ArachneAttackType.NONE
            setGlobalCooldown()
            cooldownSetter()
        },
        { currentAttack == ArachneAttackType.NONE && startPredicate() },
        shouldContinue
    ) {
        init {
            flags.add(Flag.MOVE)
        }
    }

    override fun doHurtTarget(target: Entity): Boolean {
        val result = super.doHurtTarget(target)
        if (result && target is Player && random.nextFloat() < 0.1f) {
            target.startRiding(this, true)
        }

        return result
    }

    override fun isInvulnerableTo(damageSource: DamageSource): Boolean =
        entityData[SCREAMING] || !arePlayersNearby() || super.isInvulnerableTo(damageSource)

    override fun positionRider(passenger: Entity, positionUpdater: MoveFunction) {
        if (this.hasPassenger(passenger)) {
            val y = this.y + 0.3 + passenger.bbHeight
            val bodyYawRad = (yBodyRot + 90).radians()
            val dx = cos(bodyYawRad) * 2.0 * scale
            val dz = sin(bodyYawRad) * 2.0 * scale
            positionUpdater.accept(passenger, this.x + dx, y, this.z + dz)
        }
    }

    private fun arePlayersNearby(): Boolean {
        val pos = Vec3.atBottomCenterOf(spawnPos ?: blockPosition())

        return level().getNearestPlayer(pos.x, pos.y, pos.z, 16.0, false) != null
    }

    override fun readAdditionalSaveData(nbt: CompoundTag) {
        super.readAdditionalSaveData(nbt)

        if (nbt.contains("spawn_pos")) spawnPos = BlockPos.of(nbt.getLong("spawn_pos"))
        if (nbt.contains("stomp_cooldown")) stompCooldown = nbt.getInt("stomp_cooldown")
        if (nbt.contains("rush_cooldown")) rushCooldown = nbt.getInt("rush_cooldown")
        if (nbt.contains("shoot_cooldown")) shootCooldown = nbt.getInt("shoot_cooldown")
        if (nbt.contains("spawn_cooldown")) spawnCooldown = nbt.getInt("spawn_cooldown")
        if (nbt.contains("last_stage")) lastStage = nbt.getInt("last_stage")
    }

    override fun addAdditionalSaveData(nbt: CompoundTag) {
        super.addAdditionalSaveData(nbt)

        if (spawnPos != null) nbt.putLong("spawn_pos", spawnPos!!.asLong())
        nbt.putInt("stomp_cooldown", stompCooldown)
        nbt.putInt("rush_cooldown", rushCooldown)
        nbt.putInt("shoot_cooldown", shootCooldown)
        nbt.putInt("spawn_cooldown", spawnCooldown)
        nbt.putInt("last_stage", lastStage)
    }

    enum class ArachneAttackType {
        NONE,
        SMASH,
        SPAWN,
        RUSH,
        SHOOT;
    }

    companion object {
        val SCREAMING: EntityDataAccessor<Boolean> =
            SynchedEntityData.defineId(ArachneEntity::class.java, EntityDataSerializers.BOOLEAN)

        val SHAKING: EntityDataAccessor<Boolean> =
            SynchedEntityData.defineId(ArachneEntity::class.java, EntityDataSerializers.BOOLEAN)

        val CURRENT_ATTACK: EntityDataAccessor<Int> =
            SynchedEntityData.defineId(ArachneEntity::class.java, EntityDataSerializers.INT)

        fun createArachneAttributes(): AttributeSupplier.Builder = createMonsterAttributes()
            .add(Attributes.MAX_HEALTH, 250.0)
            .add(Attributes.ARMOR, 8.0)
            .add(Attributes.MOVEMENT_SPEED, 0.45)
            .add(Attributes.KNOCKBACK_RESISTANCE, 0.9)
            .add(Attributes.ATTACK_DAMAGE, 4.0)
            .add(Attributes.ATTACK_SPEED, 0.5)
    }
}