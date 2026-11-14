package dev.mim1q.derelict.mixin.entity;

import dev.mim1q.derelict.init.ModEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.NaturalSpawner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NaturalSpawner.class)
public abstract class SpawnHelperMixin {
    @Inject(
        method = "isValidPositionForMob",
        at = @At("RETURN"),
        cancellable = true
    )
    private static void derelict$isValidSpawn(ServerLevel world, Mob entity, double squaredDistance, CallbackInfoReturnable<Boolean> cir) {
        if (ModEntities.INSTANCE.getSPAWN_ON_GROUND().contains(entity.getType())) {
            //noinspection unchecked
            cir.setReturnValue(cir.getReturnValue() && Mob.checkMobSpawnRules(
                (EntityType<Mob>) entity.getType(),
                world,
                MobSpawnType.NATURAL,
                entity.blockPosition(),
                world.random)
            );
        }
    }
}
