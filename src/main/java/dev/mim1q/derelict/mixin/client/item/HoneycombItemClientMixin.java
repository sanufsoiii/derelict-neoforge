package dev.mim1q.derelict.mixin.client.item;

import dev.mim1q.derelict.config.DerelictConfigs;
import dev.mim1q.derelict.item.CrosshairTipItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.HoneycombItem;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.DataMapHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(HoneycombItem.class)
public class HoneycombItemClientMixin implements CrosshairTipItem {
    @Unique
    private static final ResourceLocation TEXTURE = ResourceLocation.parse("textures/item/honeycomb.png");
    @Unique
    private Block lastBlock = null;
    @Unique
    private boolean didShowTip = false;

    @Override
    public boolean shouldShowTip(@Nullable Block block) {
        if (block == null || !DerelictConfigs.CLIENT_CONFIG.waxableCrosshairTip()) return false;
        if (block == lastBlock) return didShowTip;
        lastBlock = block;
        // NeoForge replaces the vanilla waxable map with a data map; the hook covers both.
        didShowTip = DataMapHooks.getBlockWaxed(block) != null;
        return didShowTip;
    }

    @NotNull
    @Override
    public ResourceLocation getTipTexture() {
        return TEXTURE;
    }
}
