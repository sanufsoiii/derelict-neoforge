package dev.mim1q.derelict.init

import dev.mim1q.derelict.Derelict
import dev.mim1q.derelict.effect.DerelictStatusEffect
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.AttributeMap
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectCategory
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.core.particles.BlockParticleOption
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.Registry
import net.minecraft.core.Holder
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundSource
import net.minecraft.sounds.SoundEvents

object ModStatusEffects {
    val COBWEBBED = register("cobwebbed", object : DerelictStatusEffect(MobEffectCategory.HARMFUL, 0x000000, false) {
        override fun shouldApplyEffectTickThisTick(duration: Int, amplifier: Int): Boolean = duration == 5 && amplifier > 0


        override fun applyEffectTick(entity: LivingEntity, amplifier: Int): Boolean {
            entity.removeEffectNoUpdate(getCobwebbed())
            entity.addEffect(
                MobEffectInstance(
                    getCobwebbed(),
                    if (entity.isAlwaysTicking) 60 else 40,
                    amplifier - 1,
                    false,
                    false,
                    true
                )
            )
            spawnParticlesAndPlaySound(entity)
            return true
        }

        override fun onRemoved(entity: LivingEntity, container: AttributeMap, amplifier: Int) {
            super.onRemoved(entity, container, amplifier)
            spawnParticlesAndPlaySound(entity)
        }

        private fun spawnParticlesAndPlaySound(entity: LivingEntity) {
            (entity.level() as? ServerLevel)?.let {
                it.sendParticles(
                    BlockParticleOption(ParticleTypes.BLOCK, Blocks.COBWEB.defaultBlockState()),
                    entity.x,
                    entity.y + entity.bbHeight / 2.0,
                    entity.z,
                    20,
                    entity.bbWidth / 3.0,
                    entity.bbHeight / 3.0,
                    entity.bbWidth / 3.0,
                    0.0
                )

                it.playSound(null, entity.blockPosition(), SoundEvents.WOOL_BREAK, SoundSource.BLOCKS, 1.0f, 1.2f)
            }
        }
    })

    fun getCobwebbed(): Holder<MobEffect> = COBWEBBED

    fun init() {}

    fun <S : MobEffect> register(name: String, effect: S): Holder<MobEffect> {
        val value = Registry.register(
            BuiltInRegistries.MOB_EFFECT,
            Derelict.id(name),
            effect
        )

        return BuiltInRegistries.MOB_EFFECT.wrapAsHolder(value)
    }
}