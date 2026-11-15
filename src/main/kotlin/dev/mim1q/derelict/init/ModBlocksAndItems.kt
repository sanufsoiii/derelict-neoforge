package dev.mim1q.derelict.init

import com.mojang.serialization.MapCodec
import dev.mim1q.derelict.Derelict
import dev.mim1q.derelict.block.EmbersBlock
import dev.mim1q.derelict.block.SmolderingLeavesBlock
import dev.mim1q.derelict.block.cobweb.*
import dev.mim1q.derelict.block.flickering.*
import dev.mim1q.derelict.entity.SpiderlingEntity
import dev.mim1q.derelict.featureset.*
import dev.mim1q.derelict.init.ModEntities.CHARMING_SPIDER
import dev.mim1q.derelict.init.ModEntities.DADDY_LONG_LEGS
import dev.mim1q.derelict.init.ModEntities.JUMPING_SPIDER
import dev.mim1q.derelict.init.ModEntities.SPIDERLING
import dev.mim1q.derelict.init.ModEntities.SPINY_SPIDER
import dev.mim1q.derelict.init.ModEntities.WEB_CASTER
import dev.mim1q.derelict.interfaces.AbstractBlockAccessor
import dev.mim1q.derelict.item.StaffItem
import dev.mim1q.derelict.item.armor.ModArmorMaterials
import dev.mim1q.derelict.item.weapon.Arachnicannon
import dev.mim1q.derelict.item.weapon.SpiderSilkBola
import dev.mim1q.derelict.item.weapon.Wildfire
import dev.mim1q.derelict.util.extensions.getNbt
import net.minecraft.world.item.ArmorItem
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.SpawnEggItem
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.LanternBlock
import net.minecraft.world.level.block.LeavesBlock
import net.minecraft.world.level.block.MultifaceBlock
import net.minecraft.world.level.block.MultifaceSpreader
import net.minecraft.world.level.block.state.BlockBehaviour
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.block.state.BlockBehaviour.OffsetFunction
import net.minecraft.world.level.block.state.BlockBehaviour.Properties
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.EntityType.CAVE_SPIDER
import net.minecraft.world.entity.EntityType.SPIDER
import net.minecraft.world.entity.MobSpawnType
import net.minecraft.world.entity.Mob

import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceKey
import net.minecraft.core.Registry
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.SoundType
import net.neoforged.neoforge.common.DataMapHooks
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps
import net.minecraft.sounds.SoundSource
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.InteractionResult
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.phys.Vec3
import net.minecraft.world.level.BlockGetter
import net.minecraft.world.level.gameevent.GameEvent

@Suppress("UNUSED", "MemberVisibilityCanBePrivate")
object ModBlocksAndItems {
    private val lastAgeable = mutableSetOf<Block>()
    private val lastWaxable = mutableSetOf<Block>()

    val AGING_STAFF = registerItem("aging_staff", StaffItem.Aging(defaultItemSettings().stacksTo(1)))
    val WAXING_STAFF = registerItem("waxing_staff", StaffItem.Waxing(defaultItemSettings().stacksTo(1)))

    val BURNED_WOOD = WoodSet(Derelict.id("burned"), defaultItemSettings()).register()
    val BURNED_LEAVES = register("burned_leaves", LeavesBlock(Properties.ofFullCopy(Blocks.OAK_LEAVES)))
    val SMOLDERING_LEAVES =
        register("smoldering_leaves", SmolderingLeavesBlock(Properties.ofFullCopy(Blocks.OAK_LEAVES)))
    val SMOKING_EMBERS = register(
        "smoking_embers",
        EmbersBlock.Smoking(Properties.ofFullCopy(Blocks.FIRE).lightLevel { 1 }.instabreak())
    )
    val SMOLDERING_EMBERS = register(
        "smoldering_embers", EmbersBlock.Smoldering(
            Properties.ofFullCopy(Blocks.FIRE).lightLevel { 4 }.emissiveRendering { _, _, _ -> true }.instabreak()
        )
    )
    val DRIED_GRASS = GrassSet(Derelict.id("dried"), defaultItemSettings())
    val BURNED_GRASS = GrassSet(Derelict.id("burned"), defaultItemSettings())

    val OAK_COVER_BOARDS = CoverBoardsSet(Derelict.id("oak"), defaultItemSettings())
    val SPRUCE_COVER_BOARDS = CoverBoardsSet(Derelict.id("spruce"), defaultItemSettings())
    val BIRCH_COVER_BOARDS = CoverBoardsSet(Derelict.id("birch"), defaultItemSettings())
    val JUNGLE_COVER_BOARDS = CoverBoardsSet(Derelict.id("jungle"), defaultItemSettings())
    val ACACIA_COVER_BOARDS = CoverBoardsSet(Derelict.id("acacia"), defaultItemSettings())
    val DARK_OAK_COVER_BOARDS = CoverBoardsSet(Derelict.id("dark_oak"), defaultItemSettings())
    val MANGROVE_COVER_BOARDS = CoverBoardsSet(Derelict.id("mangrove"), defaultItemSettings())
    val CHERRY_COVER_BOARDS = CoverBoardsSet(Derelict.id("cherry"), defaultItemSettings())
    val BAMBOO_COVER_BOARD = CoverBoardsSet(Derelict.id("bamboo"), defaultItemSettings())
    val WARPED_COVER_BOARDS = CoverBoardsSet(Derelict.id("warped"), defaultItemSettings())
    val CRIMSON_COVER_BOARDS = CoverBoardsSet(Derelict.id("crimson"), defaultItemSettings())
    val BURNED_COVER_BOARDS = CoverBoardsSet(Derelict.id("burned"), defaultItemSettings())

    val FLICKERING_REDSTONE_LAMP = register("flickering_redstone_lamp", FlickeringSolidBlock(Properties.ofFullCopy(Blocks.REDSTONE_LAMP)))
    val FLICKERING_SEA_LANTERN = register("flickering_sea_lantern", FlickeringSolidBlock(Properties.ofFullCopy(Blocks.SEA_LANTERN)))
    val EXTINGUISHED_SEA_LANTERN = register("extinguished_sea_lantern", Block(Properties.ofFullCopy(Blocks.SEA_LANTERN).lightLevel { 0 }))
    val FLICKERING_JACK_O_LANTERN = register("flickering_jack_o_lantern", FlickeringCarvedPumpkinBlock(Properties.ofFullCopy(Blocks.JACK_O_LANTERN)))
    val FLICKERING_LANTERN = register("flickering_lantern", FlickeringLanternBlock(Properties.ofFullCopy(Blocks.LANTERN)))
    val FLICKERING_SOUL_LANTERN = register("flickering_soul_lantern", FlickeringLanternBlock(Properties.ofFullCopy(Blocks.SOUL_LANTERN)))
    val EXTINGUISHED_LANTERN = register("extinguished_lantern", LanternBlock(Properties.ofFullCopy(Blocks.LANTERN).lightLevel { 0 }))
    val FLICKERING_OCHRE_FROGLIGHT = register("flickering_ochre_froglight", FlickeringSolidBlock(Properties.ofFullCopy(Blocks.OCHRE_FROGLIGHT)))
    val EXTINGUISHED_OCHRE_FROGLIGHT = register("extinguished_ochre_froglight", Block(Properties.ofFullCopy(Blocks.OCHRE_FROGLIGHT).lightLevel { 0 }))
    val FLICKERING_VERDANT_FROGLIGHT = register("flickering_verdant_froglight", FlickeringSolidBlock(Properties.ofFullCopy(Blocks.VERDANT_FROGLIGHT)))
    val EXTINGUISHED_VERDANT_FROGLIGHT = register("extinguished_verdant_froglight", Block(Properties.ofFullCopy(Blocks.VERDANT_FROGLIGHT).lightLevel { 0 }))
    val FLICKERING_PEARLESCENT_FROGLIGHT = register("flickering_pearlescent_froglight", FlickeringSolidBlock(Properties.ofFullCopy(Blocks.PEARLESCENT_FROGLIGHT)))
    val EXTINGUISHED_PEARLESCENT_FROGLIGHT = register("extinguished_pearlescent_froglight", Block(Properties.ofFullCopy(Blocks.PEARLESCENT_FROGLIGHT).lightLevel { 0 }))
    val FLICKERING_GLOWSTONE = register("flickering_glowstone", FlickeringSolidBlock(Properties.ofFullCopy(Blocks.GLOWSTONE)))
    val EXTINGUISHED_GLOWSTONE = register("extinguished_glowstone", Block(Properties.ofFullCopy(Blocks.GLOWSTONE).lightLevel { 0 }))
    val FLICKERING_SHROOMLIGHT = register("flickering_shroomlight", FlickeringSolidBlock(Properties.ofFullCopy(Blocks.SHROOMLIGHT)))
    val EXTINGUISHED_SHROOMLIGHT = register("extinguished_shroomlight", Block(Properties.ofFullCopy(Blocks.SHROOMLIGHT).lightLevel { 0 }))
    val FLICKERING_END_ROD = register("flickering_end_rod", FlickeringEndRodBlock(Properties.ofFullCopy(Blocks.END_ROD)))
    val EXTINGUISHED_END_ROD = register("extinguished_end_rod", ExtinguishedEndRodBlock(Properties.ofFullCopy(Blocks.END_ROD).lightLevel { 0 }))

    val FANCY_COBWEB = register("fancy_cobweb", FancyCobwebBlock(Properties.ofFullCopy(Blocks.COBWEB).offsetType(BlockBehaviour.OffsetType.XYZ).dynamicShape()))
    val FANCY_COBWEB_WITH_SPIDER_NEST = register("fancy_cobweb_with_spider_nest", FancyCobwebWithSpiderNestBlock(Properties.ofFullCopy(FANCY_COBWEB)))
    val CORNER_COBWEB = register("corner_cobweb", FancyCornerCobwebBlock(Properties.ofFullCopy(Blocks.COBWEB)))
    val FANCY_CORNER_COBWEB = register("fancy_corner_cobweb", FancyCornerCobwebBlock(Properties.ofFullCopy(Blocks.COBWEB)))

    class WallCobweb : MultifaceBlock(Properties.ofFullCopy(Blocks.COBWEB)) {
        val codec_: MapCodec<out MultifaceBlock> = MapCodec.unit(WALL_COBWEB)
        override fun getSpreader(): MultifaceSpreader = MultifaceSpreader(this)
        override fun codec(): MapCodec<out MultifaceBlock> = codec_
    }

    val WALL_COBWEB = register("wall_cobweb", WallCobweb())
    val SPIDER_SILK = register("spider_silk", SpiderSilkBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WHITE_WOOL).noCollission().strength(0.2f)))
    val SPIDER_SILK_STRAND = register("spider_silk_strand", SpiderSilkStrandBlock(Properties.ofFullCopy(Blocks.WHITE_WOOL).noCollission().instabreak()))

    val SPIDER_EGG_BLOCK = register("spider_egg_block", Block(Properties.ofFullCopy(Blocks.MUD)))
    val SPIDER_EGG = register("spider_egg", SpiderEggBlock(Properties.ofFullCopy(Blocks.MUD).noOcclusion().noCollission().lightLevel { 1 }.offsetType(BlockBehaviour.OffsetType.XZ), true))
    val SPIDER_EGG_CLUSTER = register("spider_egg_cluster", SpiderEggBlock(Properties.ofFullCopy(SPIDER_EGG)))
    val FAKE_SPIDER_EGG = registerBlock("fake_spider_egg", SpiderEggBlock(Properties.ofFullCopy(SPIDER_EGG), false, null, { 0 }, true))
    val SPIDERLING_SPIDER_EGG = registerBlock("spiderling_spider_egg", SpiderEggBlock(Properties.ofFullCopy(SPIDER_EGG), false, SPIDERLING, { it.nextInt(3) + 1}))
    val SPIDER_SPIDER_EGG = registerBlock("spider_spider_egg", SpiderEggBlock(Properties.ofFullCopy(SPIDER_EGG), true, SPIDER))
    val CAVE_SPIDER_SPIDER_EGG = registerBlock("cave_spider_spider_egg", SpiderEggBlock(Properties.ofFullCopy(SPIDER_EGG), true, CAVE_SPIDER))
    val WEB_CASTER_SPIDER_EGG = registerBlock("web_caster_spider_egg", SpiderEggBlock(Properties.ofFullCopy(SPIDER_EGG), true, WEB_CASTER))
    val JUMPING_SPIDER_SPIDER_EGG = registerBlock("jumping_spider_spider_egg", SpiderEggBlock(Properties.ofFullCopy(SPIDER_EGG), true, JUMPING_SPIDER))

    val SPIDER_MOLT = register("spider_molt", SpiderMoltBlock(Properties.ofFullCopy(Blocks.MUD).noOcclusion()))

    val ARACHNITE =
        StoneSet(Derelict.id("arachnite")) { Properties.ofFullCopy(Blocks.STONE).sound(SoundType.MUD) }.register()

    val NOCTISTEEL = MetalSet.ThreeLevelOxidizable(
        Derelict.id("noctisteel"),
        defaultItemSettings(),
        { Properties.ofFullCopy(Blocks.IRON_BLOCK).strength(2.0f, 1200f).noOcclusion() }
    ).register()

    val SPIDERLING_IN_A_BUCKET = registerItem("spiderling_in_a_bucket", object : Item(defaultItemSettings().stacksTo(1)) {
        override fun useOn(context: UseOnContext): InteractionResult {
            if (context.level.isClientSide) return InteractionResult.SUCCESS

            val entity = SPIDERLING.spawn(
                context.level as ServerLevel,
                context.itemInHand, context.player,
                context.clickedPos.offset(context.clickedFace.normal),
                MobSpawnType.BUCKET,
                true,
                false
            )
            if (entity is SpiderlingEntity) {
                entity.loadFromBucketTag(context.itemInHand.getNbt())
                entity.setFromBucket(true)
                if (context.clickedFace == Direction.DOWN) {
                    entity.anchorPosition = context.clickedPos
                    entity.setDeltaMovement(0.0, -0.1, 0.0)
                }
                entity.setPersistenceRequired()
                context.level.gameEvent(context.player, GameEvent.ENTITY_PLACE, context.clickedPos)
                context.level.playSound(
                    null,
                    context.clickedPos,
                    SoundEvents.SPIDER_STEP,
                    SoundSource.HOSTILE,
                    1.0f,
                    1.0f
                )
                if (context.player?.isCreative == false) {
                    context.player?.setItemInHand(context.hand, ItemStack(Items.BUCKET))
                }
            }
            return InteractionResult.SUCCESS
        }
    })

    val BLOCK_AGING_MAP = mapOf(
        Blocks.REDSTONE_LAMP to FLICKERING_REDSTONE_LAMP,
        Blocks.SEA_LANTERN to FLICKERING_SEA_LANTERN,
        FLICKERING_SEA_LANTERN to EXTINGUISHED_SEA_LANTERN,
        Blocks.JACK_O_LANTERN to FLICKERING_JACK_O_LANTERN,
        Blocks.LANTERN to FLICKERING_LANTERN,
        FLICKERING_LANTERN to EXTINGUISHED_LANTERN,
        Blocks.SOUL_LANTERN to FLICKERING_SOUL_LANTERN,
        FLICKERING_SOUL_LANTERN to EXTINGUISHED_LANTERN,
        Blocks.OCHRE_FROGLIGHT to FLICKERING_OCHRE_FROGLIGHT,
        FLICKERING_OCHRE_FROGLIGHT to EXTINGUISHED_OCHRE_FROGLIGHT,
        Blocks.VERDANT_FROGLIGHT to FLICKERING_VERDANT_FROGLIGHT,
        FLICKERING_VERDANT_FROGLIGHT to EXTINGUISHED_VERDANT_FROGLIGHT,
        Blocks.PEARLESCENT_FROGLIGHT to FLICKERING_PEARLESCENT_FROGLIGHT,
        FLICKERING_PEARLESCENT_FROGLIGHT to EXTINGUISHED_PEARLESCENT_FROGLIGHT,
        Blocks.GLOWSTONE to FLICKERING_GLOWSTONE,
        FLICKERING_GLOWSTONE to EXTINGUISHED_GLOWSTONE,
        Blocks.SHROOMLIGHT to FLICKERING_SHROOMLIGHT,
        FLICKERING_SHROOMLIGHT to EXTINGUISHED_SHROOMLIGHT,
        Blocks.END_ROD to FLICKERING_END_ROD,
        FLICKERING_END_ROD to EXTINGUISHED_END_ROD
    )

    val WILDFIRE = registerItem("wildfire", Wildfire(defaultItemSettings().stacksTo(1)), null)
    val ARACHNICANNON = registerItem("arachnicannon", Arachnicannon(defaultItemSettings().stacksTo(1)))
    val SPIDER_SILK_BOLA = registerItem("spider_silk_bola", SpiderSilkBola(defaultItemSettings()))

    // region ARMORS
    val NETWALKERS = registerItem("netwalkers", ArmorItem(ModArmorMaterials.SPIDER, ArmorItem.Type.LEGGINGS, defaultItemSettings().stacksTo(1)))
    // endregion

    // region SIMPLE ITEMS
    val SPIDER_SILK_GLAND = registerSimpleItem("spider_silk_gland")
    val SPIDER_PHEROMONE_GLAND = registerSimpleItem("spider_pheromone_gland")
    // endregion

    // region SPAWN EGGS
    val WEB_CASTER_SPAWN_EGG = registerSpawnEgg(WEB_CASTER)
    val CHARMING_SPIDER_SPAWN_EGG = registerSpawnEgg(CHARMING_SPIDER)
    val DADDY_LONG_LEGS_SPAWN_EGG = registerSpawnEgg(DADDY_LONG_LEGS)
    val JUMPING_SPIDER_SPAWN_EGG = registerSpawnEgg(JUMPING_SPIDER)
    val SPINY_SPIDER_SPAWN_EGG = registerSpawnEgg(SPINY_SPIDER)
    // endregion

    fun init() {}

    private fun oxidationKey(block: Block): ResourceKey<Block>? =
        BuiltInRegistries.BLOCK.getResourceKey(block).orElse(null)

    fun setupWaxableAndAgeable() {
        lastWaxable.forEach { (it as AbstractBlockAccessor).isWaxable = false }
        lastAgeable.forEach { (it as AbstractBlockAccessor).isAgeable = false }
        lastWaxable.clear()
        lastAgeable.clear()
        BLOCK_AGING_MAP.forEach { (from, _) ->
            (from as AbstractBlockAccessor).isAgeable = true
            lastAgeable.add(from)
        }
        // NeoForge keeps copper-like conversions, including modded pairs, in data maps.
        // The data maps are empty until the data packs are loaded, so this runs again on DataMapsUpdatedEvent.
        val waxables = BuiltInRegistries.BLOCK.getDataMap(NeoForgeDataMaps.WAXABLES)
        waxables.forEach { (key, _) ->
            val block = BuiltInRegistries.BLOCK.get(key) ?: return@forEach
            (block as AbstractBlockAccessor).isWaxable = true
            lastWaxable.add(block)
        }
        val oxidizables = BuiltInRegistries.BLOCK.getDataMap(NeoForgeDataMaps.OXIDIZABLES)
        oxidizables.forEach { (key, _) ->
            val block = BuiltInRegistries.BLOCK.get(key) ?: return@forEach
            (block as AbstractBlockAccessor).isAgeable = true
            lastAgeable.add(block)
        }
        DataMapHooks.INVERSE_WAXABLES_DATAMAP.forEach { (from, to) ->
            if (oxidationKey(to)?.let { oxidizables.containsKey(it) } == true) {
                (from as AbstractBlockAccessor).isAgeable = true
                lastAgeable.add(from)
            }
        }
    }

    /**
     * Catches a drift between the Kotlin metal sets and the data map JSON instead of silently losing pairs.
     * Must run only after the data packs are loaded (DataMapsUpdatedEvent), never during registration.
     */
    fun verifyDataMaps() {
        val waxables = BuiltInRegistries.BLOCK.getDataMap(NeoForgeDataMaps.WAXABLES)
        val oxidizables = BuiltInRegistries.BLOCK.getDataMap(NeoForgeDataMaps.OXIDIZABLES)
        MetalSet.EXPECTED_OXIDIZABLE_PAIRS.forEach { (from, to) ->
            if (oxidationKey(from)?.let { oxidizables[it]?.nextOxidationStage() } != to) {
                Derelict.LOGGER.warn("Missing oxidizable data map entry: {} -> {}", from, to)
            }
        }
        MetalSet.EXPECTED_WAXABLE_PAIRS.forEach { (from, to) ->
            if (oxidationKey(from)?.let { waxables[it]?.waxed() } != to) {
                Derelict.LOGGER.warn("Missing waxable data map entry: {} -> {}", from, to)
            }
        }
    }

    internal fun <T : Block> register(name: String, block: T, category: ItemCategory = ItemCategory.GENERAL): T {
        registerBlock(name, block)
        registerItem(name, BlockItem(block, defaultItemSettings()), category)
        return block
    }

    internal fun <T : Block> registerBlock(name: String, block: T): T = Registry.register(
        BuiltInRegistries.BLOCK, Derelict.id(name), block
    )

    internal fun <T : Item> registerItem(name: String, item: T, category: ItemCategory? = ItemCategory.GENERAL): T {
        category?.add(item)
        return Registry.register(BuiltInRegistries.ITEM, Derelict.id(name), item)
    }

    internal fun registerSimpleItem(name: String): Item = registerItem(name, Item(defaultItemSettings()))

    private fun defaultItemSettings() = Item.Properties()

    private fun registerSpawnEgg(entity: EntityType<out Mob>) =
        registerItem(
            BuiltInRegistries.ENTITY_TYPE.getKey(entity).path + "_spawn_egg",
            SpawnEggItem(entity, 0xFFFFFF, 0xFFFFFF, defaultItemSettings()),
            ItemCategory.SPAWN_EGGS
        )

    enum class ItemCategory {
        GENERAL,
        METAL_BLOCKS,
        METAL_DECORATION,
        WAXED_METAL_BLOCKS,
        WAXED_METAL_DECORATION,
        SPAWN_EGGS;

        val items: MutableList<Item> = mutableListOf()
        fun add(item: Item) = items.add(item)
    }
}

object NoZFightingOffsetter : OffsetFunction {
    override fun evaluate(state: BlockState, world: BlockGetter, pos: BlockPos): Vec3 {
        val x = pos.x % 3
        val y = pos.y % 3
        val z = pos.z % 3
        return (Vec3(
            (z * 0.001) + (y * 0.0015),
            (x * 0.001) + (z * 0.0015),
            (y * 0.001) + (x * 0.0015)
        ))
    }
}

@Suppress("UnstableApiUsage")
fun <T : BlockBehaviour.Properties> T.noZFighting(): T {
    // Access transformer opened BlockBehaviour$Properties.offsetFunction / dynamicShape.
    this.offsetFunction = NoZFightingOffsetter
    this.dynamicShape = true
    return this
}