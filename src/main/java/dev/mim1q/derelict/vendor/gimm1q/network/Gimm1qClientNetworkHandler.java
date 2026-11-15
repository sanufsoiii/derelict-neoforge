package dev.mim1q.derelict.vendor.gimm1q.network;


import dev.mim1q.derelict.vendor.gimm1q.network.s2c.CameraShakeS2CPacket;
import dev.mim1q.derelict.vendor.gimm1q.network.s2c.ValueCalculatorSyncS2CPacket;
import dev.mim1q.derelict.vendor.gimm1q.registry.ValueCalculatorResourceReloader;
import dev.mim1q.derelict.vendor.gimm1q.screenshake.ScreenShakeAccessor;
import dev.mim1q.derelict.vendor.gimm1q.screenshake.ScreenShakeModifiers;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class Gimm1qClientNetworkHandler {
    // Handlers run on the main client thread (NeoForge default HandlerThread.MAIN)
    public static void handleCameraShake(CameraShakeS2CPacket msg, IPayloadContext ctx) {
        if (!(ctx.player() instanceof LocalPlayer player)) return;
        var intensity = msg.intensity();
        if (!msg.modifierName().isBlank()) {
            intensity *= ScreenShakeModifiers.getModifier(msg.modifierName());
        }
        ((ScreenShakeAccessor) player).derelictGimm1q$shakeCamera(intensity, msg.duration());
    }

    public static void handleValueCalculatorSync(ValueCalculatorSyncS2CPacket msg, IPayloadContext ctx) {
        ValueCalculatorResourceReloader.INSTANCE.replaceWith(msg.map());
    }
}
