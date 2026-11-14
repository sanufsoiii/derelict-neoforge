package dev.mim1q.derelict.vendor.gimm1q.effect;

import net.minecraft.world.effect.MobEffect;

/**
 * Extend this interface to add additional functionality to your {@link MobEffect}
 */
public interface ExtendedStatusEffect {

    /**
     * @return true if this effect can be cured by drinking a Milk Bucket
     */
    default boolean canBeCuredWithMilk() {
        return true;
    }
}
