package dev.mim1q.derelict.item

import net.minecraft.world.level.block.Block
import net.minecraft.resources.ResourceLocation

interface CrosshairTipItem {
    fun shouldShowTip(block: Block?): Boolean
    fun getTipTexture(): ResourceLocation
}