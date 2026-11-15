package dev.mim1q.derelict

import com.mojang.blaze3d.systems.RenderSystem
import dev.mim1q.derelict.Derelict.id
import dev.mim1q.derelict.config.DerelictClientConfig
import dev.mim1q.derelict.config.DerelictConfigs
import dev.mim1q.derelict.init.ModBlocksAndItems
import dev.mim1q.derelict.init.ModParticles
import dev.mim1q.derelict.init.ModStatusEffects
import dev.mim1q.derelict.init.client.ModRender
import dev.mim1q.derelict.init.component.ModCardinalComponents.getClientSyncedEffectAmplifier
import dev.mim1q.derelict.init.component.ModCardinalComponents.hasClientSyncedEffect
import dev.mim1q.derelict.interfaces.AbstractBlockAccessor
import dev.mim1q.derelict.item.CrosshairTipItem
import dev.mim1q.derelict.port.assets.OriginalAssetsClient
import dev.mim1q.derelict.util.BlockMarkerUtils
import dev.mim1q.derelict.vendor.gimm1q.client.Gimm1qClient
import dev.mim1q.derelict.vendor.gimm1q.client.highlight.HighlightDrawerCallback
import dev.mim1q.derelict.vendor.gimm1q.client.item.handheld.HandheldItemModelRegistry
import dev.mim1q.derelict.vendor.gimm1q.interpolation.Easing
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.client.gui.screens.TitleScreen
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import net.minecraft.world.item.Items
import net.neoforged.bus.api.IEventBus
import net.neoforged.fml.ModContainer
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent
import net.neoforged.neoforge.client.event.ClientTickEvent
import net.neoforged.neoforge.client.event.EntityRenderersEvent
import net.neoforged.neoforge.client.event.ModelEvent
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent
import net.neoforged.neoforge.client.event.ScreenEvent
import net.neoforged.neoforge.common.NeoForge
import kotlin.math.sin

/**
 * Client entrypoint, called from [Derelict] only on the physical client.
 * The Fabric `onInitializeClient` body is split over the matching NeoForge events.
 */
object DerelictClient {
    val CLIENT_CONFIG: DerelictClientConfig = DerelictConfigs.CLIENT_CONFIG

    private val hudWebTextures = arrayOf(
        id("textures/gui/effect/spider_web_gui_sparse.png"),
        id("textures/gui/effect/spider_web_gui.png"),
        id("textures/gui/effect/spider_web_gui_dense.png")
    )

    fun init(modBus: IEventBus, container: ModContainer) {
        CLIENT_CONFIG.save()

        modBus.addListener(EntityRenderersEvent.RegisterLayerDefinitions::class.java, ModRender::registerLayers)
        modBus.addListener(EntityRenderersEvent.RegisterRenderers::class.java, ModRender::registerRenderers)
        modBus.addListener(EntityRenderersEvent.AddLayers::class.java, ModRender::addLayers)
        modBus.addListener(RegisterParticleProvidersEvent::class.java) { event -> ModParticles.initClient(event) }
        modBus.addListener(ModelEvent.RegisterAdditional::class.java, ModRender::registerAdditionalModels)
        modBus.addListener(RegisterGuiLayersEvent::class.java) { event ->
            event.registerAboveAll(id("crosshair_tip")) { context, _ -> renderCrosshairTip(context) }
            event.registerAboveAll(id("cobweb_overlay")) { context, _ -> renderCobwebOverlay(context) }
        }
        modBus.addListener(FMLClientSetupEvent::class.java) { event ->
            event.enqueueWork {
                ModRender.setupClient()
                // Handheld model for the arachnicannon (gimm1q). Runs after the RegisterEvent phase, so
                // touching ModBlocksAndItems here is safe; Gimm1qClient reads this set on ModelEvent.RegisterAdditional.
                HandheldItemModelRegistry.getInstance().register(
                    ModBlocksAndItems.ARACHNICANNON, id("arachnicannon_hand")
                )
            }
        }

        Gimm1qClient.init(modBus)

        HighlightDrawerCallback.EVENT.register { drawer, ctx ->
            val range = CLIENT_CONFIG.waxableAndAgeableHightlightRange()
            if (range <= 0) return@register
            val stack = ctx.player.mainHandItem

            val waxing = CLIENT_CONFIG.waxableHighlights() &&
                (stack.`is`(ModBlocksAndItems.WAXING_STAFF) || stack.`is`(Items.HONEYCOMB))
            val aging = CLIENT_CONFIG.ageableHighlights() && stack.`is`(ModBlocksAndItems.AGING_STAFF)

            if (!aging && !waxing) return@register

            val world = ctx.player.level()
            val tickDelta = Minecraft.getInstance().timer.getGameTimeDeltaPartialTick(false)
            val opacity = (sin((world.gameTime + tickDelta) * 0.25F) * 32F + 32F).toInt()
            val color = (opacity shl 24) or 0xfcad03

            BlockPos.withinManhattan(ctx.player.blockPosition(), range, range, range).forEach {
                val block = world.getBlockState(it).block as AbstractBlockAccessor
                if (
                    (aging && block.isAgeable)
                    || (waxing && block.isWaxable)
                ) {
                    drawer.highlightBlock(it, color, 0x80fcad03.toInt())
                }
            }
        }

        PortSmoke.init()
        OriginalAssetsClient.init(modBus)
    }

    private fun renderCrosshairTip(context: GuiGraphics) {
        val player = Minecraft.getInstance().player ?: return
        listOf(player.mainHandItem.item, player.offhandItem.item).forEach { item ->
            if (item is CrosshairTipItem || item == Items.HONEYCOMB) {
                BlockMarkerUtils.renderCrosshairTip(context, item as CrosshairTipItem)
                return
            }
        }
    }

    private fun GuiGraphics.drawFullScreenTexture(texture: ResourceLocation, ratio: Float) {
        val windowWidth = Minecraft.getInstance().window.guiScaledWidth
        val windowHeight = Minecraft.getInstance().window.guiScaledHeight
        val isHorizontal = (windowWidth.toFloat() / windowHeight) > ratio

        val width = if (isHorizontal) windowWidth else (windowHeight * ratio).toInt()
        val height = if (isHorizontal) (windowWidth / ratio).toInt() else windowHeight

        RenderSystem.enableBlend()
        blit(
            texture,
            (windowWidth - width) / 2, (windowHeight - height) / 2,
            0f, 0f,
            width, height,
            width, height
        )
    }

    private fun renderCobwebOverlay(context: GuiGraphics) {
        val player = Minecraft.getInstance().player ?: return
        val camera = Minecraft.getInstance().gameRenderer.mainCamera
        val world = player.level()

        val light = Easing.lerp(world.getLightLevelDependentMagicValue(camera.blockPosition) + 1 / 16f, 1f, 0.2f)

        RenderSystem.setShaderColor(light, light, light, 1f)

        if (player.hasClientSyncedEffect(ModStatusEffects.COBWEBBED)) {
            val level = Mth.clamp(player.getClientSyncedEffectAmplifier(ModStatusEffects.COBWEBBED) ?: 0, 0, 2)
            context.drawFullScreenTexture(hudWebTextures[level], 16 / 9f)
        }

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f)
    }

    /** Client half of the port smoke test: title screen reached, then exit when the system property is set. */
    private object PortSmoke {
        private var reached = false
        private var exitCountdown = -1

        fun init() {
            NeoForge.EVENT_BUS.addListener(ScreenEvent.Opening::class.java) { event ->
                if (reached || event.newScreen !is TitleScreen) return@addListener
                reached = true
                Derelict.LOGGER.info("[derelict-port] TITLE_SCREEN_REACHED")
                if (java.lang.Boolean.getBoolean("derelict.port.exitOnTitle")) {
                    exitCountdown = 100
                }
            }
            NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post::class.java) {
                if (exitCountdown < 0) return@addListener
                if (exitCountdown-- == 0) {
                    Derelict.LOGGER.info("[derelict-port] exitOnTitle: stopping the client")
                    Minecraft.getInstance().execute { Minecraft.getInstance().stop() }
                }
            }
        }
    }
}
