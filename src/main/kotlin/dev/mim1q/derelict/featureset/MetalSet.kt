package dev.mim1q.derelict.featureset

import dev.mim1q.derelict.block.RotatableCoverBlock
import dev.mim1q.derelict.block.RotatableCoverBlock.Normal.OxidizableNormal
import dev.mim1q.derelict.block.RotatableCoverBlock.SquarePatch
import dev.mim1q.derelict.block.RotatableCoverBlock.SquarePatch.OxidizableSquarePatch
import dev.mim1q.derelict.block.metal.BarbedWireBlock
import dev.mim1q.derelict.block.metal.BeamBlock
import dev.mim1q.derelict.block.metal.GrateBlock
import dev.mim1q.derelict.block.metal.MetalLadderBlock
import dev.mim1q.derelict.block.metal.oxidizable.*
import dev.mim1q.derelict.block.metal.oxidizable.OxidizableGrateBlock
import dev.mim1q.derelict.block.metal.oxidizable.OxidizableTrapdoorBlock
import dev.mim1q.derelict.init.ModBlocksAndItems.ItemCategory
import dev.mim1q.derelict.init.noZFighting
import net.fabricmc.fabric.api.registry.OxidizableBlocksRegistry
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.ChainBlock
import net.minecraft.world.level.block.IronBarsBlock
import net.minecraft.world.level.block.LadderBlock
import net.minecraft.world.level.block.RotatedPillarBlock
import net.minecraft.world.level.block.SlabBlock
import net.minecraft.world.level.block.StairBlock
import net.minecraft.world.level.block.TrapDoorBlock
import net.minecraft.world.level.block.WeatheringCopperFullBlock
import net.minecraft.world.level.block.WeatheringCopperSlabBlock
import net.minecraft.world.level.block.WeatheringCopperStairBlock
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.properties.BlockSetType
import net.minecraft.world.level.block.WeatheringCopper.WeatherState
import net.minecraft.world.item.Item
import net.minecraft.resources.ResourceLocation

sealed class MetalSet(
    id: ResourceLocation,
    defaultItemSettings: Item.Properties,
    defaultBlockSettings: BlockBehaviour.Properties
) : FeatureSet(id, defaultItemSettings, defaultBlockSettings) {
    abstract val block: Block
    abstract val cut: Block
    abstract val pillar: RotatedPillarBlock
    abstract val stairs: StairBlock
    abstract val slab: SlabBlock
    abstract val trapdoor: TrapDoorBlock
    abstract val chain: ChainBlock
    abstract val grate: GrateBlock
    abstract val beam: BeamBlock
    abstract val ladder: LadderBlock
    abstract val patch: SquarePatch
    abstract val sheet: RotatableCoverBlock.Normal
    abstract val chainLinkFence: IronBarsBlock
    abstract val barbedWire: BarbedWireBlock

    fun getCutoutBlocks() = arrayOf(
        chain, grate, ladder, chainLinkFence, barbedWire, trapdoor
    )

    class Regular(
        id: ResourceLocation,
        defaultItemSettings: Item.Properties,
        defaultBlockSettings: BlockBehaviour.Properties,
        prefix: String = ""
    ) : MetalSet(id, defaultItemSettings, defaultBlockSettings) {
        override val block: Block = registerBlockWithItem(
            "$prefix${name}_block",
            Block(defaultBlockSettings()),
            ItemCategory.WAXED_METAL_BLOCKS
        )
        override val cut: Block =
            registerBlockWithItem("${prefix}cut_$name", Block(defaultBlockSettings()), ItemCategory.WAXED_METAL_BLOCKS)
        override val pillar: RotatedPillarBlock = registerBlockWithItem(
            "$prefix${name}_pillar",
            RotatedPillarBlock(defaultBlockSettings()),
            ItemCategory.WAXED_METAL_BLOCKS
        )
        override val stairs: StairBlock = registerBlockWithItem(
            "${prefix}cut_${name}_stairs",
            StairBlock(block.defaultBlockState(), defaultBlockSettings().noOcclusion()),
            ItemCategory.WAXED_METAL_BLOCKS
        )
        override val slab: SlabBlock = registerBlockWithItem(
            "${prefix}cut_${name}_slab",
            SlabBlock(defaultBlockSettings().noOcclusion()),
            ItemCategory.WAXED_METAL_BLOCKS
        )
        override val trapdoor: TrapDoorBlock = registerBlockWithItem(
            "$prefix${name}_trapdoor",
            TrapDoorBlock(BlockSetType.IRON, defaultBlockSettings().noOcclusion()),
            ItemCategory.WAXED_METAL_DECORATION
        )
        override val chain: ChainBlock = registerBlockWithItem(
            "$prefix${name}_chain",
            ChainBlock(defaultBlockSettings().noOcclusion()),
            ItemCategory.WAXED_METAL_DECORATION
        )
        override val grate: GrateBlock = registerBlockWithItem(
            "$prefix${name}_grate",
            GrateBlock(defaultBlockSettings().noOcclusion()),
            ItemCategory.WAXED_METAL_DECORATION
        )
        override val beam: BeamBlock = registerBlockWithItem(
            "$prefix${name}_beam",
            BeamBlock(defaultBlockSettings().noOcclusion()),
            ItemCategory.WAXED_METAL_DECORATION
        )
        override val ladder: LadderBlock = registerBlockWithItem(
            "$prefix${name}_ladder",
            MetalLadderBlock(defaultBlockSettings().noOcclusion()),
            ItemCategory.WAXED_METAL_DECORATION
        )
        override val patch: SquarePatch = registerBlockWithItem(
            "$prefix${name}_patch",
            SquarePatch(defaultBlockSettings().noOcclusion().noZFighting()),
            ItemCategory.WAXED_METAL_DECORATION
        )
        override val sheet: RotatableCoverBlock.Normal = registerBlockWithItem(
            "$prefix${name}_sheet",
            RotatableCoverBlock.Normal(defaultBlockSettings().noOcclusion().noZFighting()),
            ItemCategory.WAXED_METAL_DECORATION
        )
        override val chainLinkFence: IronBarsBlock = registerBlockWithItem(
            "$prefix${name}_chain_link_fence",
            IronBarsBlock(defaultBlockSettings().noOcclusion()),
            ItemCategory.WAXED_METAL_DECORATION
        )
        override val barbedWire: BarbedWireBlock = registerBlockWithItem(
            "$prefix${name}_barbed_wire",
            BarbedWireBlock(defaultBlockSettings().noOcclusion().noCollission()),
            ItemCategory.WAXED_METAL_DECORATION
        )
    }

    class Oxidized internal constructor(
        id: ResourceLocation,
        prefix: String,
        defaultItemSettings: Item.Properties,
        defaultBlockSettings: BlockBehaviour.Properties,
        level: WeatherState
    ) : MetalSet(id, defaultItemSettings, defaultBlockSettings) {
        override val block: Block = registerBlockWithItem(
            "$prefix${name}_block",
            WeatheringCopperFullBlock(level, defaultBlockSettings()),
            ItemCategory.METAL_BLOCKS
        )
        override val cut: Block = registerBlockWithItem(
            "${prefix}cut_$name",
            WeatheringCopperFullBlock(level, defaultBlockSettings()),
            ItemCategory.METAL_BLOCKS
        )
        override val pillar: RotatedPillarBlock = registerBlockWithItem(
            "$prefix${name}_pillar",
            OxidizablePillarBlock(level, defaultBlockSettings()),
            ItemCategory.METAL_BLOCKS
        )
        override val stairs: StairBlock = registerBlockWithItem(
            "${prefix}cut_${name}_stairs",
            WeatheringCopperStairBlock(level, block.defaultBlockState(), defaultBlockSettings().noOcclusion()),
            ItemCategory.METAL_BLOCKS
        )
        override val slab: SlabBlock = registerBlockWithItem(
            "${prefix}cut_${name}_slab",
            WeatheringCopperSlabBlock(level, defaultBlockSettings().noOcclusion()),
            ItemCategory.METAL_BLOCKS
        )
        override val trapdoor: TrapDoorBlock = registerBlockWithItem(
            "$prefix${name}_trapdoor",
            OxidizableTrapdoorBlock(level, defaultBlockSettings().noOcclusion(), BlockSetType.IRON),
            ItemCategory.METAL_DECORATION
        )
        override val chain: ChainBlock = registerBlockWithItem(
            "$prefix${name}_chain",
            OxidizableChainBlock(level, defaultBlockSettings().noOcclusion()),
            ItemCategory.METAL_DECORATION
        )
        override val grate: OxidizableGrateBlock = registerBlockWithItem(
            "$prefix${name}_grate",
            OxidizableGrateBlock(level, defaultBlockSettings().noOcclusion()),
            ItemCategory.METAL_DECORATION
        )
        override val beam: BeamBlock = registerBlockWithItem(
            "$prefix${name}_beam",
            OxidizableBeamBlock(level, defaultBlockSettings().noOcclusion()),
            ItemCategory.METAL_DECORATION
        )
        override val ladder: LadderBlock = registerBlockWithItem(
            "$prefix${name}_ladder",
            OxidizableMetalLadderBlock(level, defaultBlockSettings().noOcclusion()),
            ItemCategory.METAL_DECORATION
        )
        override val patch: SquarePatch = registerBlockWithItem(
            "$prefix${name}_patch",
            OxidizableSquarePatch(level, defaultBlockSettings().noOcclusion().noZFighting()),
            ItemCategory.METAL_DECORATION
        )
        override val sheet: RotatableCoverBlock.Normal = registerBlockWithItem(
            "$prefix${name}_sheet",
            OxidizableNormal(level, defaultBlockSettings().noOcclusion().noZFighting()),
            ItemCategory.METAL_DECORATION
        )
        override val chainLinkFence: IronBarsBlock = registerBlockWithItem(
            "$prefix${name}_chain_link_fence",
            OxidizablePaneBlock(level, defaultBlockSettings().noOcclusion()),
            ItemCategory.METAL_DECORATION
        )
        override val barbedWire: BarbedWireBlock = registerBlockWithItem(
            "$prefix${name}_barbed_wire",
            OxidizableBarbedWireBlock(level, defaultBlockSettings().noOcclusion().noCollission()),
            ItemCategory.METAL_DECORATION
        )

        private fun registerOxidizable(base: Block, moreOxidized: Block?, waxed: Block) {
            if (moreOxidized != null) {
                OxidizableBlocksRegistry.registerOxidizableBlockPair(base, moreOxidized)
            }
            OxidizableBlocksRegistry.registerWaxableBlockPair(base, waxed)
        }

        fun register(moreOxidizedSet: MetalSet?, waxedSet: MetalSet): Oxidized {
            registerOxidizable(block, moreOxidizedSet?.block, waxedSet.block)
            registerOxidizable(cut, moreOxidizedSet?.cut, waxedSet.cut)
            registerOxidizable(pillar, moreOxidizedSet?.pillar, waxedSet.pillar)
            registerOxidizable(stairs, moreOxidizedSet?.stairs, waxedSet.stairs)
            registerOxidizable(slab, moreOxidizedSet?.slab, waxedSet.slab)
            registerOxidizable(trapdoor, moreOxidizedSet?.trapdoor, waxedSet.trapdoor)
            registerOxidizable(chain, moreOxidizedSet?.chain, waxedSet.chain)
            registerOxidizable(grate, moreOxidizedSet?.grate, waxedSet.grate)
            registerOxidizable(beam, moreOxidizedSet?.beam, waxedSet.beam)
            registerOxidizable(ladder, moreOxidizedSet?.ladder, waxedSet.ladder)
            registerOxidizable(patch, moreOxidizedSet?.patch, waxedSet.patch)
            registerOxidizable(sheet, moreOxidizedSet?.sheet, waxedSet.sheet)
            registerOxidizable(chainLinkFence, moreOxidizedSet?.chainLinkFence, waxedSet.chainLinkFence)
            registerOxidizable(barbedWire, moreOxidizedSet?.barbedWire, waxedSet.barbedWire)
            return this
        }
    }

    class ThreeLevelOxidizable(
        id: ResourceLocation,
        defaultItemSettings: Item.Properties,
        defaultBlockSettings: BlockBehaviour.Properties
    ) : FeatureSet(id, defaultItemSettings, defaultBlockSettings) {
        val waxedUnaffected = Regular(id, defaultItemSettings, defaultBlockSettings, "waxed_")
        val waxedWeathered = Regular(id, defaultItemSettings, defaultBlockSettings, "waxed_weathered_")
        val waxedOxidized = Regular(id, defaultItemSettings, defaultBlockSettings, "waxed_oxidized_")
        val unaffected = Oxidized(id, "", defaultItemSettings, defaultBlockSettings, WeatherState.UNAFFECTED)
        val weathered = Oxidized(id, "weathered_", defaultItemSettings, defaultBlockSettings, WeatherState.WEATHERED)
        val oxidized = Oxidized(id, "oxidized_", defaultItemSettings, defaultBlockSettings, WeatherState.OXIDIZED)

        fun getCutoutBlocks() =
            unaffected.getCutoutBlocks() + waxedUnaffected.getCutoutBlocks() +
                weathered.getCutoutBlocks() + waxedWeathered.getCutoutBlocks() +
                oxidized.getCutoutBlocks() + waxedOxidized.getCutoutBlocks()

        override fun register(): ThreeLevelOxidizable {
            unaffected.register(weathered, waxedUnaffected)
            weathered.register(oxidized, waxedWeathered)
            oxidized.register(null, waxedOxidized)
            return this
        }
    }
}