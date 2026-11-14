package dev.mim1q.derelict.vendor.gimm1q.client.highlight.crosshair;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public class CrosshairTipDrawerImpl implements CrosshairTipDrawer {
    public static final CrosshairTipDrawerImpl INSTANCE = new CrosshairTipDrawerImpl();
    private GuiGraphics drawContext = null;

    private CrosshairTipDrawerImpl() {}

    public static void setDrawContext(GuiGraphics drawContext) {
        INSTANCE.drawContext = drawContext;
    }

    @Override
    public void drawCrosshairTip(int xOffset, int yOffset, int size, ResourceLocation texture) {
        if (drawContext == null) return;

        var x = Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2 + xOffset;
        var y = Minecraft.getInstance().getWindow().getGuiScaledHeight() / 2 + yOffset;

        var centerOffset = -size / 2;

        drawContext.blit(
            texture,
            x + centerOffset,
            y + centerOffset,
            0F, 0F,
            size, size,
            size, size
        );
    }
}
