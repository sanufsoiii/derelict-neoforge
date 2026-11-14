package dev.mim1q.derelict.entity.nonliving

import dev.mim1q.derelict.Derelict
import dev.mim1q.derelict.util.lerpAngleRadians
import dev.mim1q.derelict.vendor.gimm1q.interpolation.AnimatedProperty
import dev.mim1q.derelict.vendor.gimm1q.interpolation.Easing
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.FurnaceBlock
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.item.FallingBlockEntity
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Items
import net.minecraft.world.item.SwordItem
import net.minecraft.world.level.storage.loot.LootParams
import net.minecraft.world.level.storage.loot.parameters.LootContextParams
import net.minecraft.nbt.CompoundTag
import net.minecraft.core.particles.BlockParticleOption
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.resources.ResourceKey
import net.minecraft.core.registries.Registries
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.util.Mth.HALF_PI
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.Level
import org.joml.Math
import kotlin.math.cos

class HangingCocoonEntity(type: EntityType<*>, world: Level) : Entity(type, world) {
    init {
        blocksBuilding = true
    }

    val punchYaw = AnimatedProperty(0f) { start, end, delta ->
        lerpAngleRadians(start, end, Easing.easeOutQuad(0f, 1f, delta))
    }

    private var prevPunchAngle = 0f
    var punchTime = 0f
        private set
    val punchDistance = AnimatedProperty(0f)

    override fun tick() {
        super.tick()

        if (level().isClientSide) {
            punchTime++

            val punchAngle = entityData[PUNCH_ANGLE]
            punchYaw.transitionTo(punchAngle, 30f, Easing::easeInOutQuad)

            if (prevPunchAngle != punchAngle) {
                punchDistance.transitionTo(1f, 5f, Easing::easeOutCubic)

                if (cos(punchTime * 0.2f) < 0) {
                    punchTime += 1f
                }
            } else if (punchDistance.value == 1f) {
                punchDistance.transitionTo(0f, 80f, Easing::easeOutCubic)
            }

            prevPunchAngle = punchAngle
        }
    }

    override fun hurt(source: DamageSource, amount: Float): Boolean {
        if (level().isClientSide) return false

        val player = source.entity as? Player
        val world = level() as? ServerLevel
        val cobwebState = Blocks.COBWEB.defaultBlockState()

        if (player != null && world != null) {
            if (shouldBreak(player)) {
                if (entityData[BROKEN]) {
                    dropLoot(true, source, player)
                    discard()
                } else {
                    dropLoot(false, source, player)
                    entityData[BROKEN] = true
                }
                playSound(SoundEvents.WOOL_BREAK, 1.0f, 0.8f)
                world.sendParticles(
                    BlockParticleOption(ParticleTypes.BLOCK, cobwebState),
                    x, y + 1, z, 20, 0.2, 0.5, 0.2, 1.0
                )
            } else {
                playSound(SoundEvents.WOOL_HIT, 1.0f, 0.8f)
                world.sendParticles(
                    BlockParticleOption(ParticleTypes.BLOCK, cobwebState),
                    x, y + 1, z, 2, 0.2, 0.5, 0.2, 1.0
                )
            }
        }

        val srcPos = source.sourcePosition
        if (srcPos != null) {
            entityData[PUNCH_ANGLE] = Math.atan2(srcPos.z - z, srcPos.x - x).toFloat() - HALF_PI
        }

        return true
    }

    private fun dropLoot(broken: Boolean, source: DamageSource, player: Player) {
        (level() as? ServerLevel)?.let { serverWorld ->
            val lootTableId = Derelict.id("misc/hanging_cocoon_${if (broken) "break" else "damage"}")
            val lootTableKey = ResourceKey.create(Registries.LOOT_TABLE, lootTableId)
            val lootTable = serverWorld.registryAccess().registryOrThrow(Registries.LOOT_TABLE).getHolderOrThrow(lootTableKey)
            lootTable?.value()?.getRandomItems(
                LootParams(
                    serverWorld,
                    mapOf(
                        LootContextParams.THIS_ENTITY to this,
                        LootContextParams.DAMAGE_SOURCE to source
                    ),
                    mapOf(),
                    player.luck
                )
            ) {
                if (it.`is`(Items.SKELETON_SKULL)) {
                    FallingBlockEntity.fall(
                        level(),
                        blockPosition(),
                        Blocks.FURNACE.defaultBlockState().setValue(FurnaceBlock.LIT, true)
                    )
                } else {
                    val item = ItemEntity(level(), x, y, z, it)
                    item.deltaMovement = Vec3(item.deltaMovement.x * 0.5, -0.1, item.deltaMovement.z * 0.5)
                    item.setPos(
                        item.position().add(
                            0.75 * (random.nextDouble() - 0.5),
                            random.nextDouble(),
                            0.75 * (random.nextDouble() - 0.5)
                        )
                    )
                    level().addFreshEntity(item)
                }
            }
        }
    }

    private fun shouldBreak(player: Player): Boolean {
        if (player.mainHandItem.item is SwordItem || player.offhandItem.item is SwordItem) {
            return true
        }

        return random.nextFloat() < 0.2f
    }

    override fun defineSynchedData(builder: SynchedEntityData.Builder) {
        builder.define(BROKEN, false)
        builder.define(PUNCH_ANGLE, 0f)
    }

    override fun readAdditionalSaveData(nbt: CompoundTag) {
        entityData[BROKEN] = nbt.getBoolean("Broken")
    }

    override fun addAdditionalSaveData(nbt: CompoundTag) {
        nbt.putBoolean("Broken", entityData[BROKEN])
    }

    override fun isPickable(): Boolean = true

    companion object {
        val BROKEN: EntityDataAccessor<Boolean> =
            SynchedEntityData.defineId(HangingCocoonEntity::class.java, EntityDataSerializers.BOOLEAN)
        val PUNCH_ANGLE: EntityDataAccessor<Float> =
            SynchedEntityData.defineId(HangingCocoonEntity::class.java, EntityDataSerializers.FLOAT)
    }
}