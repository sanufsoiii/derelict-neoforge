package dev.mim1q.derelict.vendor.gimm1q.client;

import dev.mim1q.derelict.vendor.gimm1q.Gimm1q;
import dev.mim1q.derelict.vendor.gimm1q.client.highlight.HighlightDrawerCallback;
import dev.mim1q.derelict.vendor.gimm1q.client.highlight.HighlightDrawerCallback.HighlightDrawerContext;
import dev.mim1q.derelict.vendor.gimm1q.client.highlight.HighlightDrawerImpl;
import dev.mim1q.derelict.vendor.gimm1q.client.highlight.crosshair.CrosshairTipDrawerCallback;
import dev.mim1q.derelict.vendor.gimm1q.client.highlight.crosshair.CrosshairTipDrawerCallback.CrosshairTipDrawerContext;
import dev.mim1q.derelict.vendor.gimm1q.client.highlight.crosshair.CrosshairTipDrawerImpl;
import dev.mim1q.derelict.vendor.gimm1q.client.item.handheld.HandheldItemModelRegistryImpl;
import dev.mim1q.derelict.vendor.gimm1q.registry.Gimm1qParticles;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.jetbrains.annotations.ApiStatus;

/**
 * Client entrypoint of the bundled gimm1q library, called from the Mine Cells client entrypoint.
 */
@ApiStatus.Internal
public class Gimm1qClient {
    private static boolean initialized = false;

    public static void init(IEventBus modBus) {
        if (initialized) return;
        initialized = true;

        Gimm1qClientCommands.init();

        modBus.addListener(RegisterParticleProvidersEvent.class, Gimm1qParticles::initClient);

        // Extra models for HandheldItemModelRegistry (Fabric: ModelLoadingPlugin#addModels)
        modBus.addListener(ModelEvent.RegisterAdditional.class, event ->
            HandheldItemModelRegistryImpl.ADDITIONAL_MODELS.forEach(event::register)
        );

        NeoForge.EVENT_BUS.addListener(RenderLevelStageEvent.class, event -> {
            if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) return;
            var player = Minecraft.getInstance().player;
            if (player == null) return;

            HighlightDrawerImpl.setRenderContext(
                event.getPoseStack(),
                Minecraft.getInstance().renderBuffers().bufferSource(),
                event.getCamera()
            );
            HighlightDrawerCallback.EVENT.invoker().drawHighlights(
                HighlightDrawerImpl.INSTANCE,
                new HighlightDrawerContext(player)
            );
        });

        modBus.addListener(RegisterGuiLayersEvent.class, event ->
            event.registerAboveAll(Gimm1q.registeredId("crosshair_tip"), (context, tickCounter) -> {
                var player = Minecraft.getInstance().player;
                if (player == null) return;

                CrosshairTipDrawerImpl.setDrawContext(context);
                CrosshairTipDrawerCallback.EVENT.invoker().drawCrosshairTip(
                    CrosshairTipDrawerImpl.INSTANCE,
                    new CrosshairTipDrawerContext(player)
                );
            })
        );
    }
}
