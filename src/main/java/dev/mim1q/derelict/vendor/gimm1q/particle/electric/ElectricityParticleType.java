package dev.mim1q.derelict.vendor.gimm1q.particle.electric;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

public class ElectricityParticleType extends ParticleType<ElectricityParticleEffect> {
    protected ElectricityParticleType(boolean alwaysShow) {
        super(alwaysShow);
    }

    public ElectricityParticleEffect get(Vec3 direction, int length, int color, float size) {
        return new ElectricityParticleEffect(direction, length, color, size, true);
    }

    public static ElectricityParticleType create() {
        return new ElectricityParticleType(true);
    }

    @Override
    public MapCodec<ElectricityParticleEffect> codec() {
        return ElectricityParticleEffect.CODEC;
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, ElectricityParticleEffect> streamCodec() {
        return ElectricityParticleEffect.PACKET_CODEC;
    }
}
