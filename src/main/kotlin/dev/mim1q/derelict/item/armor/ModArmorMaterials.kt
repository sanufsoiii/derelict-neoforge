@file:Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")

package dev.mim1q.derelict.item.armor

import net.minecraft.world.item.ArmorItem
import net.minecraft.world.item.ArmorMaterial
import net.minecraft.world.item.ArmorMaterials
import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.Holder
import net.minecraft.sounds.SoundEvents
import java.util.*
import java.util.Map

object ModArmorMaterials {
    val SPIDER: Holder<ArmorMaterial> = ArmorMaterials.register(
        "derelict_spider",
        EnumMap(
            Map.of(
                ArmorItem.Type.HELMET, 13,
                ArmorItem.Type.CHESTPLATE, 15,
                ArmorItem.Type.LEGGINGS, 16,
                ArmorItem.Type.BOOTS, 11
            )
        ),
        1,
        BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.SPIDER_STEP),
        0f,
        0.0f,
        { Ingredient.of(Items.WHITE_WOOL) },
        listOf()
    )
}
