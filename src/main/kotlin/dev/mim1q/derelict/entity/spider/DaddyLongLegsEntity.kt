package dev.mim1q.derelict.entity.spider

import dev.mim1q.derelict.entity.spider.legs.SpiderLegController
import dev.mim1q.derelict.tag.ModBlockTags
import dev.mim1q.derelict.vendor.gimm1q.interpolation.AnimatedProperty
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Items
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.InteractionResult
import net.minecraft.world.item.DyeColor
import net.minecraft.world.InteractionHand
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.Level
import kotlin.math.cos
import kotlin.math.sin

class DaddyLongLegsEntity(
    entityType: EntityType<DaddyLongLegsEntity>,
    world: Level,
) : PathfinderMob(entityType, world) {
    private var songSource: BlockPos? = null
    private var isSongPlaying: Boolean = false

    val danceAnimation = AnimatedProperty(0f)
    private var danceOffsetVector: Vec3 = Vec3.ZERO

    val sockColor: DyeColor?
        get() = entityData[SOCK_COLOR].let {
            if (it == -1) null else DyeColor.byId(it)
        }

    val legController = SpiderLegController(
        this,
        { 32 / 16f },
        { 32 / 16f },
        //@formatter:off
        { danceOffsetVector.add( 0.25, 60 / 16.0,  3 / 32.0 ) } to { Vec3( 0.5, 0.0,  0.5) },
        { danceOffsetVector.add( 0.25, 60 / 16.0,  1 / 32.0 ) } to { Vec3( 0.8, 0.0,  0.1) },
        { danceOffsetVector.add( 0.25, 60 / 16.0, -1 / 32.0 ) } to { Vec3( 0.8, 0.0, -0.1) },
        { danceOffsetVector.add( 0.25, 60 / 16.0, -3 / 32.0 ) } to { Vec3( 0.5, 0.0, -0.5) },

        { danceOffsetVector.add(-0.25, 60 / 16.0,  3 / 32.0 ) } to { Vec3(-0.5, 0.0,  0.5) },
        { danceOffsetVector.add(-0.25, 60 / 16.0,  1 / 32.0 ) } to { Vec3(-0.8, 0.0,  0.1) },
        { danceOffsetVector.add(-0.25, 60 / 16.0, -1 / 32.0 ) } to { Vec3(-0.8, 0.0, -0.1) },
        { danceOffsetVector.add(-0.25, 60 / 16.0, -3 / 32.0 ) } to { Vec3(-0.5, 0.0, -0.5) },
        //@formatter:on
    )

    override fun registerGoals() {
        super.registerGoals()

        goalSelector.addGoal(4, WaterAvoidingRandomStrollGoal(this, 1.0))
        goalSelector.addGoal(3, LookAtPlayerGoal(this, Player::class.java, 24F))
        goalSelector.addGoal(3, RandomLookAroundGoal(this))
    }

    override fun defineSynchedData(builder: SynchedEntityData.Builder) {
        super.defineSynchedData(builder)

        builder.define(SOCK_COLOR, -1)
    }

    override fun tick() {
        super.tick()

        if (this.songSource == null || !songSource!!.closerToCenterThan(
                this.position(),
                3.46
            ) || !level().getBlockState(this.songSource).`is`(Blocks.JUKEBOX)
        ) {
            this.isSongPlaying = false
            this.songSource = null
        }

        if (level().isClientSide) {
            legController.tick()

            if (isSongPlaying) {
                danceAnimation.transitionTo(1f, 20f)
            } else {
                danceAnimation.transitionTo(0f, 20f)
            }
            danceOffsetVector =
                Vec3(cos(tickCount * 0.5), sin(tickCount * 0.5) - 1.0, 0.0).scale(danceAnimation.value.toDouble())
        }
    }

    override fun mobInteract(player: Player, hand: InteractionHand): InteractionResult {
        val stack = player.getItemInHand(hand)

        if (stack.`is`(Items.SHEARS) && sockColor != null) {
            if (!level().isClientSide) {
                playSound(SoundEvents.SHEEP_SHEAR, 1f, 1f)
                stack.hurtAndBreak(1, level() as ServerLevel, player as ServerPlayer) {
                    player.onEquippedItemBroken(
                        stack.item,
                        if (hand == InteractionHand.MAIN_HAND) EquipmentSlot.MAINHAND else EquipmentSlot.OFFHAND
                    )
                }
                entityData.set(SOCK_COLOR, -1)
            }
            return InteractionResult.SUCCESS
        }

        if (WOOL_TO_COLOR.any { it.key == stack.item }) {
            val newColor = WOOL_TO_COLOR[stack.item]
            if (newColor == sockColor) return InteractionResult.PASS

            if (!level().isClientSide) {
                playSound(SoundEvents.WOOL_PLACE, 1f, 1f)
                stack.shrink(1)
                entityData.set(SOCK_COLOR, WOOL_TO_COLOR[stack.item]?.id ?: -1)
            }
            return InteractionResult.SUCCESS
        }

        return super.mobInteract(player, hand)
    }

    override fun setRecordPlayingNearby(songPosition: BlockPos, playing: Boolean) {
        this.songSource = songPosition
        this.isSongPlaying = playing
    }

    override fun addAdditionalSaveData(nbt: CompoundTag) {
        super.addAdditionalSaveData(nbt)

        if (entityData.get(SOCK_COLOR) != -1) nbt.putInt("SockColor", entityData.get(SOCK_COLOR))
    }

    override fun readAdditionalSaveData(nbt: CompoundTag) {
        super.readAdditionalSaveData(nbt)

        if (nbt.contains("SockColor")) entityData.set(SOCK_COLOR, nbt.getInt("SockColor"))
    }

    override fun makeStuckInBlock(state: BlockState, multiplier: Vec3) =
        if (!state.`is`(ModBlockTags.COBWEBS)) super.makeStuckInBlock(state, multiplier) else Unit

    companion object {
        private val WOOL_TO_COLOR = mapOf(
            //@formatter:off
            Blocks.WHITE_WOOL.asItem()      to DyeColor.WHITE,
            Blocks.LIGHT_GRAY_WOOL.asItem() to DyeColor.LIGHT_GRAY,
            Blocks.GRAY_WOOL.asItem()       to DyeColor.GRAY,
            Blocks.BLACK_WOOL.asItem()      to DyeColor.BLACK,
            Blocks.BROWN_WOOL.asItem()      to DyeColor.BROWN,
            Blocks.RED_WOOL.asItem()        to DyeColor.RED,
            Blocks.ORANGE_WOOL.asItem()     to DyeColor.ORANGE,
            Blocks.YELLOW_WOOL.asItem()     to DyeColor.YELLOW,
            Blocks.LIME_WOOL.asItem()       to DyeColor.LIME,
            Blocks.GREEN_WOOL.asItem()      to DyeColor.GREEN,
            Blocks.CYAN_WOOL.asItem()       to DyeColor.CYAN,
            Blocks.LIGHT_BLUE_WOOL.asItem() to DyeColor.LIGHT_BLUE,
            Blocks.BLUE_WOOL.asItem()       to DyeColor.BLUE,
            Blocks.PURPLE_WOOL.asItem()     to DyeColor.PURPLE,
            Blocks.MAGENTA_WOOL.asItem()    to DyeColor.MAGENTA,
            Blocks.PINK_WOOL.asItem()       to DyeColor.PINK
            //@formatter:on
        )

        val SOCK_COLOR: EntityDataAccessor<Int> =
            SynchedEntityData.defineId(DaddyLongLegsEntity::class.java, EntityDataSerializers.INT)
    }
}