package dev.mim1q.derelict.vendor.gimm1q.network.s2c;

import dev.mim1q.derelict.vendor.gimm1q.Gimm1q;
import dev.mim1q.derelict.vendor.gimm1q.valuecalculators.internal.ValueCalculatorInternal;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record ValueCalculatorSyncS2CPacket(
    Map<ResourceLocation, List<ValueCalculatorInternal>> map
) implements CustomPacketPayload {
    public static final Type<ValueCalculatorSyncS2CPacket> ID = new Type<>(Gimm1q.id("value_calculator_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ValueCalculatorSyncS2CPacket> CODEC = StreamCodec.ofMember(
        (packet, buf) -> Data.writeBuf(buf, packet.map()),
        (buf) -> new ValueCalculatorSyncS2CPacket(Data.readMap(buf))
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return ID;
    }

    private static class Data {
        public static void writeBuf(RegistryFriendlyByteBuf buf, Map<ResourceLocation, List<ValueCalculatorInternal>> map) {
            buf.writeInt(map.size());

            for (var entry : map.entrySet()) {
                buf.writeResourceLocation(entry.getKey());
                var nbts = new ArrayList<CompoundTag>();

                for (ValueCalculatorInternal calculator : entry.getValue()) {
                    try {
                        nbts.add((CompoundTag) ValueCalculatorInternal.CODEC
                            .encodeStart(NbtOps.INSTANCE, calculator)
                            .getOrThrow(e -> new RuntimeException(String.format(
                                "Failed to encode Value Calculator %s to send to client. %s", entry.getKey(), e
                            )))
                        );
                    } catch (Exception e) {
                        Gimm1q.LOGGER.warn("Failed to encode Value Calculator {} to send to client. {}", entry.getKey(), e);
                    }
                }

                buf.writeInt(nbts.size());
                for (CompoundTag nbt : nbts) {
                    buf.writeNbt(nbt);
                }
            }
        }

        public static Map<ResourceLocation, List<ValueCalculatorInternal>> readMap(FriendlyByteBuf buffer) {
            var mapSize = buffer.readInt();

            var map = new HashMap<ResourceLocation, List<ValueCalculatorInternal>>();
            for (int i = 0; i < mapSize; ++i) {
                var id = buffer.readResourceLocation();
                var listSize = buffer.readInt();

                var list = new ArrayList<ValueCalculatorInternal>();
                for (int j = 0; j < listSize; ++j) {
                    var nbt = buffer.readNbt();
                    if (nbt == null) continue;
                    list.add(ValueCalculatorInternal.CODEC
                        .parse(NbtOps.INSTANCE, nbt)
                        .result()
                        .orElseThrow(() -> new IllegalStateException("Failed to decode Value Calculator from client."))
                    );
                }

                map.put(id, list);
            }

            return map;
        }
    }
}
