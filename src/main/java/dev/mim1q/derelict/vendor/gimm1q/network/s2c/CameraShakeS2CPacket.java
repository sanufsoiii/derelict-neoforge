package dev.mim1q.derelict.vendor.gimm1q.network.s2c;

import dev.mim1q.derelict.vendor.gimm1q.Gimm1q;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public record CameraShakeS2CPacket(
    float intensity,
    int duration,
    String modifierName
) implements CustomPacketPayload {
    public static final Type<CameraShakeS2CPacket> ID = new Type<>(Gimm1q.registeredId("camera_shake"));
    public static final StreamCodec<RegistryFriendlyByteBuf, CameraShakeS2CPacket> CODEC = StreamCodec.composite(
        ByteBufCodecs.FLOAT, CameraShakeS2CPacket::intensity,
        ByteBufCodecs.INT, CameraShakeS2CPacket::duration,
        ByteBufCodecs.STRING_UTF8, CameraShakeS2CPacket::modifierName,
        CameraShakeS2CPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}