package dev.mim1q.derelict.vendor.gimm1q.registry;

import dev.mim1q.derelict.vendor.gimm1q.Gimm1q;
import dev.mim1q.derelict.vendor.gimm1q.particle.electric.ElectricityParticle;
import dev.mim1q.derelict.vendor.gimm1q.particle.electric.ElectricityParticleType;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

/**
 * Custom particles added in gimm1q 0.8.0. Registered under the port-owned namespace so that the copy
 * bundled by another mod cannot collide with this one.
 */
public class Gimm1qParticles {
    public static final ElectricityParticleType ELECTRICITY = ElectricityParticleType.create();

    public static void init() {
        Registry.register(BuiltInRegistries.PARTICLE_TYPE, Gimm1q.registeredId("electricity"), ELECTRICITY);
    }

    public static void initClient(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ELECTRICITY, ElectricityParticle.Factory::new);
    }
}
