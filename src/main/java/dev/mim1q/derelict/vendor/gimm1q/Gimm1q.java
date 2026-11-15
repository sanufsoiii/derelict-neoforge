package dev.mim1q.derelict.vendor.gimm1q;

import dev.mim1q.derelict.vendor.gimm1q.network.Gimm1qServerNetworkHandler;
import dev.mim1q.derelict.vendor.gimm1q.registry.Gimm1qParticles;
import dev.mim1q.derelict.vendor.gimm1q.registry.Gimm1qRegistries;
import dev.mim1q.derelict.vendor.gimm1q.registry.ValueCalculatorResourceReloader;
import dev.mim1q.derelict.vendor.gimm1q.valuecalculators.variables.VariableSourceTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.common.EffectCures;
import dev.mim1q.derelict.vendor.gimm1q.effect.ExtendedStatusEffect;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;

/**
 * Common entrypoint of the bundled gimm1q library. On NeoForge it is not a separate mod;
 * Derelict calls {@link #init(IEventBus)} from its own constructor.
 * <p>
 * The library keeps the {@code gimm1q} namespace for resource paths and registry entry keys (data pack
 * value calculators refer to {@code gimm1q:constant} etc.), but everything it actually registers into a
 * global registry uses {@link #REGISTRY_ID}: the same library is bundled by other ports under the same
 * package name, and duplicate registry keys, payload ids or GUI layer ids would abort mod loading.
 */
@ApiStatus.Internal
public class Gimm1q {
    /** Namespace of gimm1q resources and of value-calculator type ids used in data packs. */
    public static final String ID = "gimm1q";
    /** Namespace of the things this copy registers (registry key, payloads, GUI layers, commands). */
    public static final String REGISTRY_ID = "derelict_gimm1q";
    public static final Logger LOGGER = LogManager.getLogger(ID);

    /**
     * Only use for the testmod. Don't enable in production because there's lots of debug messages
     */
    @ApiStatus.Internal
    public static boolean debugMessages = false;

    private static boolean initialized = false;

    public static void init(IEventBus modBus) {
        if (initialized) return;
        initialized = true;
        LOGGER.info("Initializing Gimm1q - Mim1q's Fabric modding library!");

        // Custom registry + its built-in entries
        modBus.addListener(NewRegistryEvent.class, event -> event.register(Gimm1qRegistries.VARIABLE_SOURCE_TYPE));
        modBus.addListener(RegisterEvent.class, event -> {
            if (event.getRegistryKey().equals(Gimm1qRegistries.VARIABLE_SOURCE_TYPE_KEY)) {
                VariableSourceTypes.init();
            }
            if (event.getRegistryKey().equals(Registries.PARTICLE_TYPE)) {
                Gimm1qParticles.init();
            }
        });

        // Registering custom resources
        NeoForge.EVENT_BUS.addListener(AddReloadListenerEvent.class,
            event -> event.addListener(ValueCalculatorResourceReloader.INSTANCE));

        // Both events only fire for a logical server. They must also fire on an integrated server:
        // Fabric's ServerPlayConnectionEvents/SYNC_DATA_PACK_CONTENTS did, and LAN players need the
        // value-calculator map too.
        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedInEvent.class, event -> {
            if (event.getEntity() instanceof ServerPlayer player) {
                PacketDistributor.sendToPlayer(player, ValueCalculatorResourceReloader.INSTANCE.createPacket());
            }
        });
        NeoForge.EVENT_BUS.addListener(OnDatapackSyncEvent.class, event ->
            event.getRelevantPlayers().forEach(player ->
                PacketDistributor.sendToPlayer(player, ValueCalculatorResourceReloader.INSTANCE.createPacket())
            )
        );

        // ExtendedStatusEffect#canBeCuredWithMilk: on Fabric the milk bucket calls clearStatusEffects(), which
        // LivingEntityMixin wraps. NeoForge's milk bucket uses removeEffectsCuredBy(EffectCures.MILK) instead,
        // which fires MobEffectEvent.Remove for every effect it is about to remove.
        NeoForge.EVENT_BUS.addListener(MobEffectEvent.Remove.class, event -> {
            if (event.getCure() == EffectCures.MILK
                && event.getEffect().value() instanceof ExtendedStatusEffect extendedEffect
                && !extendedEffect.canBeCuredWithMilk()
            ) {
                event.setCanceled(true);
            }
        });

        // Custom commands
        Gimm1qCommands.init();

        // Networking
        Gimm1qServerNetworkHandler.init(modBus);
    }

    /** Id inside the {@code gimm1q} namespace: resources and data pack value calculator types. */
    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ID, path);
    }

    /** Id inside the port-owned namespace: anything registered into a global NeoForge registry. */
    public static ResourceLocation registeredId(String path) {
        return ResourceLocation.fromNamespaceAndPath(REGISTRY_ID, path);
    }
}
