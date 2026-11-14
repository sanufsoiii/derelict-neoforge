package dev.mim1q.derelict.vendor.gimm1q.mixin.entity;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.mim1q.derelict.vendor.gimm1q.effect.ExtendedStatusEffect;
import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Iterator;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

@ApiStatus.Internal
@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Shadow
    public abstract ItemStack getUseItem();

    @Unique
    private boolean gimm1q$preventRemovingCurrentEffect = false;

    @WrapOperation(
        method = "removeAllEffects",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;onEffectRemoved(Lnet/minecraft/world/effect/MobEffectInstance;)V"
        )
    )
    private void gimm1q$wrapOnStatusEffectRemoved(LivingEntity instance, MobEffectInstance effect, Operation<Void> original) {
        if (
            getUseItem().is(Items.MILK_BUCKET)
            && effect.getEffect() instanceof ExtendedStatusEffect extendedEffect
            && !extendedEffect.canBeCuredWithMilk()
        ) {
            gimm1q$preventRemovingCurrentEffect = true;
            return;
        }
        original.call(instance, effect);
    }

    @WrapWithCondition(
        method = "removeAllEffects",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/Iterator;remove()V"
        )
    )
    private boolean gimm1q$wrapIteratorRemoveWithConditionInClearStatusEffects(Iterator<MobEffectInstance> instance) {
        if (gimm1q$preventRemovingCurrentEffect) {
            gimm1q$preventRemovingCurrentEffect = false;
            return false;
        }
        return true;
    }
}
