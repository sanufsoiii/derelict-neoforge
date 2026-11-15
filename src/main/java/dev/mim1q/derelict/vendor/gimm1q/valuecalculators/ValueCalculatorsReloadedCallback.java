package dev.mim1q.derelict.vendor.gimm1q.valuecalculators;

import dev.mim1q.derelict.vendor.gimm1q.event.Event;
import dev.mim1q.derelict.vendor.gimm1q.event.EventFactory;
import net.minecraft.resources.ResourceLocation;
import java.util.Set;

public interface ValueCalculatorsReloadedCallback {
    Event<ValueCalculatorsReloadedCallback> EVENT = EventFactory.createArrayBacked(
        ValueCalculatorsReloadedCallback.class,
        listeners -> (ids) -> {
            for (ValueCalculatorsReloadedCallback listener : listeners) {
                listener.onValueCalculatorsReloaded(ids);
            }
        }
    );

    void onValueCalculatorsReloaded(Set<ResourceLocation> ids);
}
