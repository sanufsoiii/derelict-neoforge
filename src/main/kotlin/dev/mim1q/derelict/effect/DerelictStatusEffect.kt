package dev.mim1q.derelict.effect

import dev.mim1q.derelict.init.component.ModCardinalComponents
import dev.mim1q.derelict.init.component.ModCardinalComponents.statusEffectComponent
import dev.mim1q.derelict.vendor.gimm1q.effect.ExtendedStatusEffect
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.ai.attributes.AttributeMap
import net.minecraft.world.effect.MobEffect
import net.minecraft.world.effect.MobEffectCategory
import net.minecraft.core.registries.BuiltInRegistries

open class DerelictStatusEffect(
    category: MobEffectCategory,
    color: Int,
    private val curable: Boolean = true
) : MobEffect(category, color), ExtendedStatusEffect {
    override fun onEffectStarted(entity: LivingEntity, amplifier: Int) {
        super.onEffectStarted(entity, amplifier)
        entity.statusEffectComponent.addEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(this), amplifier)
        entity.syncData(ModCardinalComponents.EFFECT_FLAGS)
    }

    open fun onRemoved(entity: LivingEntity, container: AttributeMap, amplifier: Int) {
        super.removeAttributeModifiers(container)
        entity.statusEffectComponent.removeEffect(BuiltInRegistries.MOB_EFFECT.wrapAsHolder(this))
        entity.syncData(ModCardinalComponents.EFFECT_FLAGS)
    }

    override fun canBeCuredWithMilk(): Boolean = curable
}