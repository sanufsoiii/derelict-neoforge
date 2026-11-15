package dev.mim1q.derelict.vendor.gimm1q.mixin.client.tooltip;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.blaze3d.platform.InputConstants;
import dev.mim1q.derelict.vendor.gimm1q.client.tooltip.TooltipResolverRegistry;
import dev.mim1q.derelict.vendor.gimm1q.client.tooltip.TooltipResolverRegistry.TooltipHelper;
import dev.mim1q.derelict.vendor.gimm1q.client.tooltip.TooltipResolverRegistry.TooltipResolver;
import dev.mim1q.derelict.vendor.gimm1q.client.tooltip.TooltipResolverRegistry.TooltipResolverContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

@Mixin(ItemStack.class)
public abstract class ItemStackTooltipMixin {
    @Unique
    private static List<Tuple<Component, Boolean>> derelictGimm1q$tooltipsToAdd = List.of();
    @Unique
    private long derelictGimm1q$lastTooltipTime = 0;
    //    @Unique
//    private static List<ItemStack.TooltipSection> derelictGimm1q$hiddenTooltipSections = List.of();
    @Unique
    private static Style derelictGimm1q$defaultStyle = Style.EMPTY;

    @Shadow
    public abstract Item getItem();

    @Inject(
        method = "getTooltipLines",
        at = @At("HEAD")
    )
    private void derelictGimm1q$onGetTooltip(
        Item.TooltipContext context, Player player, TooltipFlag type, CallbackInfoReturnable<List<Component>> cir, @Share("tooltip") LocalRef<List<Component>> tooltip
    ) {
        if (player == null) return;
        var thisItemStack = (ItemStack) (Object) this;

        var tooltipResolver = TooltipResolverRegistry.getInstance().getResolver(this.getItem());
        if (tooltipResolver == null) {
            return;
        }

        var currentTime = System.currentTimeMillis();
        final int[] maxLineWidth = {1024};

        final List<Tuple<Component, Boolean>> currentTooltip;

        if (currentTime - derelictGimm1q$lastTooltipTime <= 60) {
            currentTooltip = derelictGimm1q$tooltipsToAdd;
        } else {
            currentTooltip = new ArrayList<>();

            tooltipResolver.resolve(
                new TooltipResolverContext(
                    thisItemStack,
                    player,
                    context
                ),
                new TooltipHelper() {
                    @Override
                    public TooltipHelper addLine(Component text, boolean extra) {
                        currentTooltip.add(new Tuple<>(text, extra));
                        return this;
                    }

//                    @Override
//                    public TooltipHelper hideSections(ItemStack.TooltipSection... sections) {
//                        derelictGimm1q$hiddenTooltipSections.addAll(List.of(sections));
//                        return this;
//                    }

                    @Override
                    public TooltipHelper defaultStyle(Style style) {
                        derelictGimm1q$defaultStyle = style;
                        return this;
                    }

                    @Override
                    public TooltipHelper maxLineWidth(int width) {
                        maxLineWidth[0] = width;
                        return this;
                    }
                }
            );

            var newCurrentTooltip = currentTooltip.stream().<Tuple<Component, Boolean>>mapMulti((it, consumer) -> {
                var result = TooltipHelper.splitTextIfExceeds(it.getA(), maxLineWidth[0]);
                for (Component text : result) {
                    consumer.accept(new Tuple<>(text, it.getB()));
                }
            }).toList();

            currentTooltip.clear();
            currentTooltip.addAll(newCurrentTooltip);

            derelictGimm1q$tooltipsToAdd = currentTooltip;
        }

        derelictGimm1q$lastTooltipTime = currentTime;

        var newTooltip = new ArrayList<Component>();
        var altPressed = InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), InputConstants.KEY_LALT);
        var altTooltip = false;
        for (Tuple<Component, Boolean> pair : currentTooltip) {
            if (!altPressed && pair.getB()) {
                altTooltip = true;
                continue;
            }
            var text = pair.getA();
            if (text.getStyle().isEmpty()) {
                text = text.copy().setStyle(derelictGimm1q$defaultStyle);
            }
            newTooltip.add(text);
        }
        if (!altPressed && altTooltip) {
            newTooltip.add(
                Component.translatable(
                    "tooltip.gimm1q.alt_tooltip",
                    Component.translatable("key.keyboard.left.alt").getString()
                ).withStyle(ChatFormatting.DARK_GRAY)
            );
        }
        tooltip.set(newTooltip);
    }

    @Inject(
        method = "getTooltipLines",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/TooltipFlag;isAdvanced()Z",
            ordinal = 0
        )
    )
    private void derelictGimm1q$onGetTooltip2(
        Item.TooltipContext context, Player player, TooltipFlag type, CallbackInfoReturnable<List<Component>> cir, @Local(ordinal = 0) List<Component> list, @Share("tooltip") LocalRef<List<Component>> tooltip
    ) {
        var newTooltip = tooltip.get();
        if (newTooltip == null || newTooltip.isEmpty()) return;
        list.addAll(tooltip.get());
    }

//    @Inject(
//        method = "getHideFlags()I",
//        at = @At("RETURN"),
//        cancellable = true
//    )
//    private void derelictGimm1q$onGetHideFlags(CallbackInfoReturnable<Integer> cir) {
//        if (derelictGimm1q$hiddenTooltipSections.isEmpty()) return;
//
//        var result = cir.getReturnValue();
//        for (var section : derelictGimm1q$hiddenTooltipSections) {
//            result |= section.getFlag();
//        }
//        cir.setReturnValue(result);
//    }
}
