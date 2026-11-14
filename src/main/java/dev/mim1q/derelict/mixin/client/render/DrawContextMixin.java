package dev.mim1q.derelict.mixin.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.mim1q.derelict.config.DerelictConfigs;
import dev.mim1q.derelict.tag.ModItemTags;
import dev.mim1q.derelict.util.BlockMarkerUtils;
import kotlin.Unit;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiGraphics.class)
public abstract class DrawContextMixin {

    @Shadow
    abstract void innerBlit(ResourceLocation texture, int x1, int x2, int y1, int y2, int z, float u1, float u2, float v1, float v2, float red, float green, float blue, float alpha);

    @Shadow
    @Final
    private PoseStack pose;

    @Inject(
        method = "renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;IIII)V",
        at = @At("TAIL")
    )
    void derelict$injectDrawItem(LivingEntity entity, Level world, ItemStack stack, int x, int y, int seed, int z, CallbackInfo ci) {
        if (
            stack.is(ModItemTags.INSTANCE.getWAXED())
                && DerelictConfigs.CLIENT_CONFIG.waxedIndicatorOpacity() > 0F
        ) {
            BlockMarkerUtils.INSTANCE.renderWaxedIndicator(this::derelict$drawTexture, x, y);
        }
    }

    @Unique
    public Unit derelict$drawTexture(ResourceLocation texture, int x, int y, int z, int size, float alpha) {
        this.pose.pushPose();
        this.pose.translate(0.0F, 0.0F, z);
        innerBlit(
            texture, x, x + size, y, y + size, 0, 0, 1, 0, 1, 1F, 1F, 1F, alpha
        );
        this.pose.popPose();
        return Unit.INSTANCE;
    }
}
