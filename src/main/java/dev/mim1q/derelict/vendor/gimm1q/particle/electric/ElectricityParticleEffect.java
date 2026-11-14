package dev.mim1q.derelict.vendor.gimm1q.particle.electric;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.mim1q.derelict.vendor.gimm1q.registry.Gimm1qParticles;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

public record ElectricityParticleEffect(
    Vec3 direction,
    int length,
    int color,
    float size,
    boolean isMainBranch
) implements ParticleOptions {
    public static final MapCodec<ElectricityParticleEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
        Vec3.CODEC.fieldOf("direction").forGetter(ElectricityParticleEffect::direction),
        Codec.INT.fieldOf("length").forGetter(ElectricityParticleEffect::length),
        Codec.INT.fieldOf("color").forGetter(ElectricityParticleEffect::color),
        Codec.FLOAT.fieldOf("size").forGetter(ElectricityParticleEffect::size),
        Codec.BOOL.fieldOf("isMainBranch").forGetter(ElectricityParticleEffect::isMainBranch)
    ).apply(instance, ElectricityParticleEffect::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ElectricityParticleEffect> PACKET_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC.codec());

    @Override
    public ParticleType<?> getType() {
        return Gimm1qParticles.ELECTRICITY;
    }
}
