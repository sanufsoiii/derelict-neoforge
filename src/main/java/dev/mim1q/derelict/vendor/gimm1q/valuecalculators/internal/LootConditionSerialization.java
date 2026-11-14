package dev.mim1q.derelict.vendor.gimm1q.valuecalculators.internal;

import com.mojang.serialization.Codec;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class LootConditionSerialization {
    private LootConditionSerialization() {
    }

    public static final Codec<LootItemCondition> CODEC = LootItemCondition.DIRECT_CODEC;
}