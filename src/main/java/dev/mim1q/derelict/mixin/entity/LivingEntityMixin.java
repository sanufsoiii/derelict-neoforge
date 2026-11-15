package dev.mim1q.derelict.mixin.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import dev.mim1q.derelict.config.DerelictConfigs;
import dev.mim1q.derelict.effect.DerelictStatusEffect;
import dev.mim1q.derelict.init.ModEntities;
import dev.mim1q.derelict.init.ModStatusEffects;
import dev.mim1q.derelict.init.component.ModCardinalComponents;
import dev.mim1q.derelict.tag.ModEntityTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static java.lang.Math.min;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
    @Shadow
    public abstract AttributeMap getAttributes();

    public LivingEntityMixin(EntityType<?> type, Level world) {
        super(type, world);
    }

    @Inject(
        method = "die",
        at = @At("HEAD")
    )
    private void derelict$onDeath(DamageSource source, CallbackInfo ci) {
        if (this.getType().is(ModEntityTags.INSTANCE.getSPAWNS_SPIDERLINGS_ON_DEATH())
            && !level().isClientSide
            && random.nextFloat() < DerelictConfigs.CONFIG.spiderlingSpawnChance() / 100f
        ) {
            var spiderling = ModEntities.INSTANCE.getSPIDERLING().create(level());
            if (spiderling == null) return;
            spiderling.moveTo(getX(), getY(), getZ(), random.nextFloat() * 360, 0);
            spiderling.finalizeSpawn(
                (ServerLevelAccessor) level(),
                level().getCurrentDifficultyAt(blockPosition()),
                MobSpawnType.EVENT,
                null
            );
            level().addFreshEntity(spiderling);
        }
    }

    @Inject(
        method = "onEffectRemoved",
        at = @At("HEAD")
    )
    public void derelict$removeStatusEffectInternal(MobEffectInstance effect, CallbackInfo ci) {
        if (effect.getEffect().value() instanceof DerelictStatusEffect statusEffect) {
            statusEffect.onRemoved((LivingEntity) (Object) this, this.getAttributes(), effect.getAmplifier());
        }
    }

    @WrapOperation(
        method = "handleRelativeFrictionAndCalculateMovement",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;moveRelative(FLnet/minecraft/world/phys/Vec3;)V"
        )
    )
    private void derelict$cancelMovement(
        LivingEntity instance,
        float v,
        Vec3 movementInput,
        Operation<Void> original
    ) {
        if (ModCardinalComponents.INSTANCE.hasClientSyncedEffect((LivingEntity) (Object) this,
            ModStatusEffects.INSTANCE.getCobwebbed()
        )) {
            original.call(instance, v, new Vec3(0.0, min(0.0, movementInput.y), 0.0));
            return;
        }
        original.call(instance, v, movementInput);
    }
}