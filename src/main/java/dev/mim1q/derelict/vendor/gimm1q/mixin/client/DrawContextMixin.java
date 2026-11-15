package dev.mim1q.derelict.vendor.gimm1q.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.mim1q.derelict.vendor.gimm1q.client.highlight.gui.GuiHighlightDrawerCallback;
import dev.mim1q.derelict.vendor.gimm1q.client.highlight.gui.GuiHighlightDrawerImpl;
import dev.mim1q.derelict.vendor.gimm1q.client.highlight.gui.GuiHighlightDrawerImpl.GuiHighlightDrawerContext;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@ApiStatus.Internal
@Mixin(GuiGraphics.class)
public abstract class DrawContextMixin {
    @Shadow @Final private PoseStack pose;
    @Shadow
    abstract void innerBlit(ResourceLocation texture, int x1, int x2, int y1, int y2, int z, float u1, float u2, float v1, float v2, float red, float green, float blue, float alpha);

    @Inject(
        method = "renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;IIII)V",
        at = @At("TAIL")
    )
    private void derelictGimm1q$injectDrawItem(
        LivingEntity entity,
        Level world,
        ItemStack stack,
        int x,
        int y,
        int seed,
        int z,
        CallbackInfo ci
    ) {
        if (entity instanceof Player player) {
            GuiHighlightDrawerImpl.setContext(
                new GuiHighlightDrawerContext(this::innerBlit, this.pose, x, y)
            );

            GuiHighlightDrawerCallback.EVENT.invoker().drawHighlights(
                GuiHighlightDrawerImpl.INSTANCE,
                new GuiHighlightDrawerCallback.GuiHighlightDrawerContext(player, stack)
            );
        }
    }
}
