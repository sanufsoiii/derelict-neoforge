package dev.mim1q.derelict.util

import dev.mim1q.derelict.DerelictClient
import dev.mim1q.derelict.item.CrosshairTipItem
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult


object BlockMarkerUtils {
    private val HONEYCOMB_TEXTURE = ResourceLocation.parse("textures/item/honeycomb.png")

    fun renderWaxedIndicator(textureDrawer: (ResourceLocation, Int, Int, Int, Int, Float) -> Unit, x: Int, y: Int) {
        val size = (DerelictClient.CLIENT_CONFIG.waxedIndicatorScale() * 16).toInt()
        val startX = x - size / 2
        val startY = y - size / 2
        val alpha = DerelictClient.CLIENT_CONFIG.waxedIndicatorOpacity()
        textureDrawer(HONEYCOMB_TEXTURE, startX, startY, 300, size, alpha)
    }

    fun renderCrosshairTip(context: GuiGraphics, item: CrosshairTipItem) {
        val target = Minecraft.getInstance().hitResult
        if (target?.type == HitResult.Type.BLOCK) {
            val block = Minecraft.getInstance().level?.getBlockState((target as BlockHitResult).blockPos)?.block
            if (item.shouldShowTip(block)) {
                renderCrosshairTipTexture(context, item.getTipTexture())
            }
        }
    }

    private fun renderCrosshairTipTexture(context: GuiGraphics, texture: ResourceLocation) {
//    RenderSystem.setShader(GameRenderer::getPositionTexProgram)
        val x = Minecraft.getInstance().window.guiScaledWidth / 2 + 6
        val y = Minecraft.getInstance().window.guiScaledHeight / 2 - 8

        context.blit(texture, x, y, 0F, 0F, 16, 16, 16, 16)
    }
}