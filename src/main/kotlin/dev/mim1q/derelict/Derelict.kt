package dev.mim1q.derelict

import dev.mim1q.derelict.config.DerelictConfig
import dev.mim1q.derelict.config.DerelictConfigs
import dev.mim1q.derelict.featureset.WoodSet
import dev.mim1q.derelict.init.*
import dev.mim1q.derelict.init.component.ModCardinalComponents
import dev.mim1q.derelict.init.component.ModCardinalComponents.updateClientSyncedEffects
import dev.mim1q.derelict.init.worldgen.AddJumpingSpidersBiomeModifier
import dev.mim1q.derelict.init.worldgen.ModBiomes
import dev.mim1q.derelict.init.worldgen.ModFeatures
import dev.mim1q.derelict.port.ServerSmoke
import dev.mim1q.derelict.vendor.gimm1q.Gimm1q
import io.wispforest.owo.itemgroup.OwoItemGroup
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.neoforged.bus.api.EventPriority
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.common.Mod
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent
import net.neoforged.fml.loading.FMLEnvironment
import net.neoforged.neoforge.common.NeoForge
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent
import net.neoforged.neoforge.event.entity.player.PlayerEvent
import net.neoforged.neoforge.event.level.BlockEvent
import net.neoforged.neoforge.event.server.ServerStartedEvent
import net.neoforged.neoforge.event.tick.ServerTickEvent
import net.neoforged.neoforge.registries.NeoForgeRegistries
import net.neoforged.neoforge.registries.RegisterEvent
import net.neoforged.neoforge.registries.datamaps.DataMapsUpdatedEvent
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.util.function.Supplier

/** Kotlin for Forge entrypoint; the actual mod object is [Derelict]. */
@Mod(Derelict.MOD_ID)
class DerelictMod(modBus: IEventBus, container: ModContainer) {
    init {
        Derelict.onInitialize(modBus, container)
    }
}

/**
 * Common entrypoint.
 *
 * On Fabric all content was registered in the field initializers of the `init` objects, triggered from
 * `onInitialize` (which Fabric calls before the registries freeze). On NeoForge the registries are frozen
 * outside of [RegisterEvent], so the same chain is triggered once from that event. NeoForge unfreezes all
 * registries for the whole registration phase, therefore the original init order is preserved.
 */
object Derelict {
    const val MOD_ID = "derelict"
    @JvmField
    val LOGGER: Logger = LogManager.getLogger(MOD_ID)
    val CONFIG: DerelictConfig = DerelictConfigs.CONFIG

    /** Lazily created: owo registers the creative tab in {@code initialize()}, which must happen inside RegisterEvent. */
    val ITEM_GROUP: OwoItemGroup get() = ModItemGroups.ITEM_GROUP

    fun id(value: String) = ResourceLocation.fromNamespaceAndPath(MOD_ID, value)

    fun onInitialize(modBus: IEventBus, container: ModContainer) {
        LOGGER.info("Derelict initializing")
        CONFIG.save()
        Gimm1q.init(modBus)

        modBus.addListener(EventPriority.HIGHEST, RegisterEvent::class.java) { event ->
            // BLOCK, not ATTRIBUTE: NeoForge registers its own attributes (neoforge:swim_speed, ...) at
            // NORMAL priority during the ATTRIBUTE event, and any AttributeSupplier built before that
            // would touch unbound holders. BLOCK fires after ATTRIBUTE; all registries are still unfrozen.
            if (event.registryKey == Registries.BLOCK) {
                registerContent()
            }
            if (event.registryKey == NeoForgeRegistries.Keys.ATTACHMENT_TYPES) {
                event.register(
                    NeoForgeRegistries.Keys.ATTACHMENT_TYPES,
                    id("effect_flags"),
                    Supplier { ModCardinalComponents.EFFECT_FLAGS }
                )
            }
            if (event.registryKey == NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS) {
                event.register(
                    NeoForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS,
                    id("add_jumping_spiders"),
                    Supplier { AddJumpingSpidersBiomeModifier.CODEC }
                )
            }
        }

        modBus.addListener(EntityAttributeCreationEvent::class.java) { event -> ModEntities.registerAttributes(event) }
        modBus.addListener(RegisterSpawnPlacementsEvent::class.java) { event -> ModEntities.registerSpawnPlacements(event) }
        modBus.addListener(FMLCommonSetupEvent::class.java) { event ->
            // TerraBlender on NeoForge requires the regions to be registered during common setup.
            event.enqueueWork { ModBiomes.init() }
        }

        // Fabric: ServerPlayConnectionEvents.JOIN (fires at the end of PlayerList.placeNewPlayer)
        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedInEvent::class.java) { event ->
            (event.entity as? ServerPlayer)?.updateClientSyncedEffects()
        }
        // Fabric: ServerPlayerEvents.COPY_FROM (fires at the end of ServerPlayer.restoreFrom)
        NeoForge.EVENT_BUS.addListener(PlayerEvent.Clone::class.java) { event ->
            event.entity.updateClientSyncedEffects()
        }
        // Fabric: StrippableBlockRegistry.register
        NeoForge.EVENT_BUS.addListener(BlockEvent.BlockToolModificationEvent::class.java) { event ->
            WoodSet.onToolModification(event)
        }
        // Fabric: OxidizableBlocksRegistry / HoneycombItem maps, filled from the data maps on NeoForge.
        NeoForge.EVENT_BUS.addListener(DataMapsUpdatedEvent::class.java) { event ->
            if (event.registryKey == Registries.BLOCK) {
                ModBlocksAndItems.setupWaxableAndAgeable()
                ModBlocksAndItems.verifyDataMaps()
            }
        }
        NeoForge.EVENT_BUS.addListener(ServerStartedEvent::class.java) { ServerSmoke.onServerStarted(it) }
        NeoForge.EVENT_BUS.addListener(ServerTickEvent.Post::class.java) { ServerSmoke.onServerTick(it) }

        if (FMLEnvironment.dist.isClient()) {
            DerelictClient.init(modBus, container)
        }
    }

    private fun registerContent() {
        LOGGER.info("Derelict: registering content")
        ModBlocksAndItems.init()
        ModBlockEntities.init()
        ModEntities.init()
        ModStatusEffects.init()
        ModParticles.init()
        ModSounds.init()
        ModFeatures.init()
        ModItemGroups.init()
        ModBlocksAndItems.setupWaxableAndAgeable()
    }
}
