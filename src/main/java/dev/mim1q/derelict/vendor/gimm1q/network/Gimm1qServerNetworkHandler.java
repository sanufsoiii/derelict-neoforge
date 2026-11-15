package dev.mim1q.derelict.vendor.gimm1q.network;

import dev.mim1q.derelict.vendor.gimm1q.Gimm1q;
import dev.mim1q.derelict.vendor.gimm1q.network.s2c.CameraShakeS2CPacket;
import dev.mim1q.derelict.vendor.gimm1q.network.s2c.ValueCalculatorSyncS2CPacket;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public class Gimm1qServerNetworkHandler {
    public static void init(IEventBus modBus) {
        modBus.addListener(RegisterPayloadHandlersEvent.class, event -> {
            var registrar = event.registrar(Gimm1q.ID);
            // Handlers live in a client-only class; it is only loaded when a packet is actually handled on the client.
            registrar.playToClient(CameraShakeS2CPacket.ID, CameraShakeS2CPacket.CODEC,
                (msg, ctx) -> Gimm1qClientNetworkHandler.handleCameraShake(msg, ctx));
            registrar.playToClient(ValueCalculatorSyncS2CPacket.ID, ValueCalculatorSyncS2CPacket.CODEC,
                (msg, ctx) -> Gimm1qClientNetworkHandler.handleValueCalculatorSync(msg, ctx));
        });
    }
}
