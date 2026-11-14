@file:Suppress("DEPRECATION")

package dev.mim1q.derelict.util.extensions

import net.fabricmc.fabric.api.`object`.builder.v1.block.FabricBlockSettings
import net.minecraft.world.level.block.state.BlockBehaviour.Properties
import net.minecraft.core.component.DataComponents
import net.minecraft.world.item.component.CustomData
import net.minecraft.world.item.ItemStack
import net.minecraft.nbt.CompoundTag

fun ItemStack.getNbt(): CompoundTag {
    val component = get(DataComponents.CUSTOM_DATA)
    if (component != null) {
        val nbt = component.copyTag()
        if (nbt != null) {
            return nbt
        }
    }
    return CompoundTag()
}

fun ItemStack.setNbt(nbt: CompoundTag) {
    set(DataComponents.CUSTOM_DATA, CustomData.of(nbt))
}

fun ItemStack.updateNbt(updater: CompoundTag.() -> Unit) {
    val nbt = getNbt()
    updater(nbt)
    setNbt(nbt)
}

fun Properties.copy(): Properties = FabricBlockSettings.copyOf(this)