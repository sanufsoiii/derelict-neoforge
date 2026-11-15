package dev.mim1q.derelict.compat.emi

import dev.emi.emi.api.EmiEntrypoint
import dev.emi.emi.api.EmiPlugin
import dev.emi.emi.api.EmiRegistry
import dev.emi.emi.api.recipe.EmiWorldInteractionRecipe
import dev.emi.emi.api.stack.EmiStack
import dev.mim1q.derelict.init.ModBlocksAndItems.AGING_STAFF
import dev.mim1q.derelict.init.ModBlocksAndItems.WAXING_STAFF
import dev.mim1q.derelict.item.getFullAgingMap
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.level.ItemLike
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps

/** NeoForge EMI discovers plugins by annotation and instantiates them, hence a class, not an object. */
@EmiEntrypoint
class EmiDerelictPlugin : EmiPlugin {
    override fun register(registry: EmiRegistry) {
        BuiltInRegistries.BLOCK.getDataMap(NeoForgeDataMaps.WAXABLES).forEach { (key, waxable) ->
            val input = BuiltInRegistries.BLOCK.get(key) ?: return@forEach
            registry.addRecipe(
                EmiWorldInteractionRecipe.builder()
                    .leftInput(input.emiStack())
                    .rightInput(EmiStack.of(WAXING_STAFF), true)
                    .output(waxable.waxed().emiStack())
                    .build()
            )
        }

        getFullAgingMap().forEach {
            registry.addRecipe(
                EmiWorldInteractionRecipe.builder()
                    .leftInput(it.key.emiStack())
                    .rightInput(EmiStack.of(AGING_STAFF), true)
                    .output(it.value.emiStack())
                    .build()
            )
        }
    }

    private fun ItemLike.emiStack(): EmiStack = EmiStack.of(this)
}
