package dev.mim1q.derelict.init

import dev.mim1q.derelict.Derelict
import dev.mim1q.derelict.block.cobweb.SpiderEggBlock.SpiderEggBlockEntity
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType

object ModBlockEntities {
    val SPIDER_EGG_BLOCK_ENTITY = register(
        "spider_egg", ::SpiderEggBlockEntity,
        ModBlocksAndItems.SPIDER_EGG, ModBlocksAndItems.SPIDER_EGG_CLUSTER,
        ModBlocksAndItems.SPIDERLING_SPIDER_EGG, ModBlocksAndItems.SPIDER_SPIDER_EGG,
        ModBlocksAndItems.CAVE_SPIDER_SPIDER_EGG, ModBlocksAndItems.JUMPING_SPIDER_SPIDER_EGG,
        ModBlocksAndItems.WEB_CASTER_SPIDER_EGG, ModBlocksAndItems.FAKE_SPIDER_EGG
    )

    fun init() {}

    private fun <T : BlockEntity> register(
        id: String,
        factory: BlockEntityType.BlockEntitySupplier<T>,
        vararg blocks: Block
    ): BlockEntityType<T> = Registry.register(
        BuiltInRegistries.BLOCK_ENTITY_TYPE,
        Derelict.id(id),
        BlockEntityType.Builder.of(factory, *blocks).build(null)
    )
}
