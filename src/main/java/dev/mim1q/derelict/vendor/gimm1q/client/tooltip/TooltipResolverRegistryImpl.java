package dev.mim1q.derelict.vendor.gimm1q.client.tooltip;

import org.jetbrains.annotations.ApiStatus;

import java.util.HashMap;
import net.minecraft.world.item.Item;

@ApiStatus.Internal
public class TooltipResolverRegistryImpl implements TooltipResolverRegistry {
    private static final HashMap<Item, TooltipResolver> resolvers = new HashMap<>();

    static TooltipResolverRegistry INSTANCE = new TooltipResolverRegistryImpl();

    @Override
    public void register(TooltipResolver resolver, Item... items) {
        for (Item item : items) {
            resolvers.put(item, resolver);
        }
    }

    @Override
    public TooltipResolver getResolver(Item item) {
        return resolvers.get(item);
    }
}
