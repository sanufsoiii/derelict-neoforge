package dev.mim1q.derelict.item

import dev.mim1q.derelict.config.DerelictConfigs
import dev.mim1q.derelict.block.flickering.FlickeringBlock
import dev.mim1q.derelict.init.ModBlocksAndItems
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.WeatheringCopper
import net.neoforged.neoforge.common.DataMapHooks
import net.minecraft.world.item.Item
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.core.particles.ParticleOptions
import net.minecraft.core.particles.ParticleTypes
import net.minecraft.util.ParticleUtils
import net.minecraft.sounds.SoundSource
import net.minecraft.sounds.SoundEvent
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.InteractionResult
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.valueproviders.UniformInt

sealed class StaffItem(settings: Item.Properties) : Item(settings), CrosshairTipItem {
    private var lastBlock: Block? = null
    private var didShowTip: Boolean = false
    protected abstract val texture: ResourceLocation
    protected abstract val particle: ParticleOptions
    protected abstract val sound: SoundEvent

    override fun shouldShowTip(block: Block?): Boolean {
        if (block == null) return false
        if (block == lastBlock) return didShowTip
        lastBlock = block
        didShowTip = getBlockConversion(block) != null
        return didShowTip
    }

    override fun getTipTexture(): ResourceLocation = texture
    abstract fun getBlockConversion(block: Block): Block?

    override fun useOn(context: UseOnContext): InteractionResult {
        val world = context.level
        val pos = context.clickedPos
        val state = world.getBlockState(pos)
        val conversion = getBlockConversion(state.block)
        if (conversion != null) {
            if (world.isClientSide) ParticleUtils.spawnParticlesOnBlockFaces(world, pos, particle, UniformInt.of(3, 5))
            world.playSound(null, pos, sound, SoundSource.BLOCKS, 1F, 1F)
            world.setBlockAndUpdate(context.clickedPos, conversion.withPropertiesOf(state))
            if (conversion is FlickeringBlock) {
                world.scheduleTick(pos, conversion, 1)
            }
            return InteractionResult.SUCCESS
        }
        return InteractionResult.PASS
    }

    class Aging(settings: Item.Properties) : StaffItem(settings) {
        override val texture: ResourceLocation = ResourceLocation.parse("textures/item/clock_00.png")
        override val particle: ParticleOptions = ParticleTypes.WHITE_ASH
        override val sound: SoundEvent = SoundEvents.STONE_BREAK

        override fun getBlockConversion(block: Block): Block? = firstNonNull(
            { ModBlocksAndItems.BLOCK_AGING_MAP[block] },
            { DataMapHooks.getNextOxidizedStage(block) },
            {
                DataMapHooks.getBlockUnwaxed(block)
                    ?.let { DataMapHooks.getNextOxidizedStage(it) }
                    ?.let { DataMapHooks.getBlockWaxed(it) }
            }
        )

        override fun shouldShowTip(block: Block?) =
            DerelictConfigs.CLIENT_CONFIG.ageableCrosshairTip() && super.shouldShowTip(block)
    }

    class Waxing(settings: Item.Properties) : StaffItem(settings) {
        override val texture: ResourceLocation = ResourceLocation.parse("textures/item/honeycomb.png")
        override val particle: ParticleOptions = ParticleTypes.WAX_ON
        override val sound: SoundEvent = SoundEvents.HONEYCOMB_WAX_ON

        override fun getBlockConversion(block: Block): Block? = DataMapHooks.getBlockWaxed(block)

        override fun shouldShowTip(block: Block?) =
            DerelictConfigs.CLIENT_CONFIG.waxableCrosshairTip() && super.shouldShowTip(block)
    }

    protected fun firstNonNull(vararg suppliers: () -> Block?): Block? {
        suppliers.forEach {
            val block = it()
            if (block != null) return block
        }
        return null
    }
}

fun getFullAgingMap(): Map<Block, Block> {
    val map = mutableMapOf<Block, Block>()
    map.putAll(ModBlocksAndItems.BLOCK_AGING_MAP)
    WeatheringCopper.NEXT_BY_BLOCK.get().forEach { (from, to) ->
        map[from] = to
    }
    DataMapHooks.INVERSE_WAXABLES_DATAMAP.forEach { (waxed, unwaxed) ->
        val aged = DataMapHooks.getNextOxidizedStage(unwaxed) ?: return@forEach
        val agedWaxed = DataMapHooks.getBlockWaxed(aged) ?: return@forEach
        map[waxed] = agedWaxed
    }

    return map
}