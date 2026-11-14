package dev.mim1q.derelict.entity.spider

import dev.mim1q.derelict.vendor.gimm1q.interpolation.AnimatedProperty
import dev.mim1q.derelict.vendor.gimm1q.interpolation.Easing
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.entity.monster.Spider
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level

class CharmingSpiderEntity(
    entityType: EntityType<out Spider>,
    world: Level
) : Spider(entityType, world) {
    companion object {
        fun createCharmingSpiderAttributes(): AttributeSupplier.Builder = createAttributes()
            .add(Attributes.MAX_HEALTH, 10.0)
    }

    private var songSource: BlockPos? = null
    private var isSongPlaying: Boolean = false

    val tailDanceAnimation = AnimatedProperty(0f)
    val bodyDanceAnimation = AnimatedProperty(0f, Easing::easeOutBack)

    override fun defineSynchedData(builder: SynchedEntityData.Builder) {
        super.defineSynchedData(builder)
    }

    override fun tick() {
        super.tick()

        if (this.songSource == null || !songSource!!.closerToCenterThan(this.position(), 3.46) || !level().getBlockState(this.songSource).`is`(Blocks.JUKEBOX)) {
            this.isSongPlaying = false
            this.songSource = null
        }

        if (level().isClientSide) {
            if (isSongPlaying) {
                tailDanceAnimation.transitionTo(1f, 10f)

                when (tickCount % 20) {
                    0 -> bodyDanceAnimation.transitionTo(1f, 10f)
                    10 -> bodyDanceAnimation.transitionTo(-1f, 10f)
                }
            } else {
                tailDanceAnimation.transitionTo(0f, 10f)
                bodyDanceAnimation.transitionTo(0f, 10f)
            }
        }
    }

    override fun setRecordPlayingNearby(songPosition: BlockPos, playing: Boolean) {
        this.songSource = songPosition
        this.isSongPlaying = playing
    }
}