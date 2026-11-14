package dev.mim1q.derelict.entity.spider

import dev.mim1q.derelict.entity.spider.control.ArachneBodyControl
import dev.mim1q.derelict.entity.spider.legs.SpiderLegController
import dev.mim1q.derelict.init.ModStatusEffects
import dev.mim1q.derelict.tag.ModBlockTags
import dev.mim1q.derelict.util.wrapDegrees
import dev.mim1q.derelict.vendor.gimm1q.interpolation.AnimatedProperty
import dev.mim1q.derelict.vendor.gimm1q.interpolation.Easing
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.goal.FloatGoal
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.entity.monster.Monster
import net.minecraft.world.entity.monster.Spider
import net.minecraft.world.entity.animal.Pig
import net.minecraft.world.entity.player.Player
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.Level
import kotlin.math.pow

class WebCasterEntity(entityType: EntityType<WebCasterEntity>, world: Level) : Monster(entityType, world) {
    companion object {
        val WEB_HELD: EntityDataAccessor<Boolean> =
            SynchedEntityData.defineId(WebCasterEntity::class.java, EntityDataSerializers.BOOLEAN)

        fun createWebCasterAttributes(): AttributeSupplier.Builder = Spider.createAttributes()
            .add(Attributes.MAX_HEALTH, 40.0)
            .add(Attributes.ARMOR_TOUGHNESS, 1.0)
            .add(Attributes.ATTACK_DAMAGE, 3.0)
    }

    val legController = SpiderLegController(
        this,
        { 24 / 16f },
        { 28 / 16f },
        //@formatter:off
        { Vec3( 5 / 16.0, 0.6, 7 / 16.0) } to { Vec3( 1.5, 0.0,  2.0) },
        { Vec3( 5 / 16.0, 0.6, 5 / 16.0) } to { Vec3( 2.0, 0.0,  1.5) },
        { Vec3( 5 / 16.0, 0.6, 3 / 16.0) } to { if (entityData[WEB_HELD]) Vec3(2.0, 0.0, 1.5) else Vec3(2.0, 0.0, -1.0) },
        { Vec3( 5 / 16.0, 0.6, 1 / 16.0) } to { Vec3( 1.5, 0.0, -1.5) },

        { Vec3(-5 / 16.0, 0.6, 7 / 16.0) } to { Vec3(-1.5, 0.0,  2.0) },
        { Vec3(-5 / 16.0, 0.6, 5 / 16.0) } to { Vec3(-2.0, 0.0,  1.5) },
        { Vec3(-5 / 16.0, 0.6, 3 / 16.0) } to { if (entityData[WEB_HELD]) Vec3(-2.0, 0.0, 1.5) else Vec3(-2.0, 0.0, -1.0) },
        { Vec3(-5 / 16.0, 0.6, 1 / 16.0) } to { Vec3(-1.5, 0.0, -1.5) },
        //@formatter:on
    )

    override fun defineSynchedData(builder: SynchedEntityData.Builder) {
        super.defineSynchedData(builder)
        builder.define(WEB_HELD, false)
    }

    override fun registerGoals() {
        goalSelector.addGoal(0, FloatGoal(this))
        goalSelector.addGoal(1, MeleeAttackGoal(this, 1.2, true))
        goalSelector.addGoal(2, LookAtPlayerGoal(this, Player::class.java, 16.0f))
        goalSelector.addGoal(5, WaterAvoidingRandomStrollGoal(this, 0.8))
        goalSelector.addGoal(6, RandomLookAroundGoal(this))

        targetSelector.addGoal(2, NearestAttackableTargetGoal(this, Player::class.java, true))
        targetSelector.addGoal(2, NearestAttackableTargetGoal(this, Pig::class.java, true))

    }

    private var targetTimer = 100
    private var webCooldown = 100
    private var webHeldTimer = 40

    val webHeldAnimation = AnimatedProperty(0f)

    override fun tick() {
        if (level().isClientSide) {
            legController.tick()
        }

        super.tick()

        if (level().isClientSide) {
            handleAnimations()
        } else {
            tickWebLogic()
        }
    }

    private fun handleAnimations() {
        if (entityData[WEB_HELD]) {
            webHeldAnimation.transitionTo(1f, 15f, Easing::easeOutBack)
        } else {
            webHeldAnimation.transitionTo(0f, 5f, Easing::easeOutCubic)
        }
    }

    private fun tickWebLogic() {
        webCooldown--
        if (entityData[WEB_HELD]) {
            webHeldTimer--

            if (target == null) {
                targetTimer = 60 + random.nextInt(60)
                entityData[WEB_HELD] = false
                return
            }
        }

        if (webCooldown <= 0 && target != null) {
            targetTimer--
            if (targetTimer >= 0) return
            if (!entityData[WEB_HELD]) {

                entityData[WEB_HELD] = true
                playSound(
                    SoundEvents.WOOL_PLACE,
                    1.0f,
                    voicePitch
                )
                playSound(
                    SoundEvents.SPIDER_AMBIENT,
                    1.0f,
                    voicePitch
                )
            }
        }
    }

    override fun doHurtTarget(target: Entity): Boolean {
        val webHeld = entityData[WEB_HELD]
        if (webHeld && webHeldTimer > 0) return false

        if (webHeld && target is LivingEntity) {
            target.addEffect(
                MobEffectInstance(
                    ModStatusEffects.COBWEBBED,
                    60,
                    2,
                    true,
                    false,
                    true
                )
            )

            webCooldown = 200 + random.nextInt(200)
            webHeldTimer = 40
            entityData[WEB_HELD] = false

            playSound(
                SoundEvents.WOOL_PLACE,
                1.0f,
                voicePitch
            )

            return false
        }

        return super.doHurtTarget(target)
    }

    override fun getVoicePitch(): Float = 0.6f + random.nextFloat() * 0.25f

    override fun isWithinMeleeAttackRange(entity: LivingEntity): Boolean =
        entity.distanceToSqr(this) <= (bbWidth + 1.0).pow(2) + entity.bbWidth
    
    override fun setYBodyRot(bodyYaw: Float) = super.setYBodyRot(wrapDegrees(this.yBodyRot, bodyYaw, 10f))
    override fun createBodyControl() = ArachneBodyControl(this)
    override fun makeStuckInBlock(state: BlockState, multiplier: Vec3) =
        if (!state.`is`(ModBlockTags.COBWEBS)) super.makeStuckInBlock(state, multiplier) else Unit
}