package dev.mim1q.derelict.init.client

import dev.mim1q.derelict.Derelict
import dev.mim1q.derelict.client.render.armor.NetwalkersRenderer
import dev.mim1q.derelict.client.render.block.SpiderEggClusterRenderer
import dev.mim1q.derelict.client.render.effect.SpiderWebModelFeature
import dev.mim1q.derelict.client.render.entity.boss.arachne.ArachneEntityRenderer
import dev.mim1q.derelict.client.render.entity.boss.arachne.ArachneTexturedModelData
import dev.mim1q.derelict.client.render.entity.nonliving.HangingCocoonEntityRenderer
import dev.mim1q.derelict.client.render.entity.projectile.SpiderEggProjectileRenderer
import dev.mim1q.derelict.client.render.entity.projectile.SpiderSilkBolaRenderer
import dev.mim1q.derelict.client.render.entity.spider.*
import dev.mim1q.derelict.init.ModBlockEntities
import dev.mim1q.derelict.init.ModBlocksAndItems
import dev.mim1q.derelict.init.ModEntities
import net.minecraft.client.model.EntityModel
import net.minecraft.client.model.HumanoidModel
import net.minecraft.client.model.geom.ModelLayerLocation
import net.minecraft.client.renderer.ItemBlockRenderTypes
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.LivingEntityRenderer
import net.minecraft.client.renderer.item.ItemProperties
import net.minecraft.client.resources.model.ModelResourceLocation
import net.neoforged.neoforge.client.event.EntityRenderersEvent
import net.minecraft.client.renderer.Sheets
import java.util.function.Supplier

object ModRender {
    val ARACHNE_LAYER = layer("arachne")

    val SPIDERLING_LAYER = layer("spiderling")
    val CHARMING_SPIDER_LAYER = layer("charming_spider")
    val WEB_CASTER_LAYER = layer("web_caster")
    val DADDY_LONG_LEGS_LAYER = layer("daddy_long_legs")
    val JUMPING_SPIDER_LAYER = layer("jumping_spider")
    val SPINY_SPIDER_LAYER = layer("spiny_spider")

    val HANGING_COCOON_LAYER = layer("hanging_cocoon")

    val NETWALKERS_LAYER = layer("netwalkers")

    fun registerLayers(event: EntityRenderersEvent.RegisterLayerDefinitions) {
        event.registerLayerDefinition(ARACHNE_LAYER, Supplier { ArachneTexturedModelData.create() })
        event.registerLayerDefinition(SPIDERLING_LAYER, Supplier { getSpiderlingTexturedModelData() })
        event.registerLayerDefinition(CHARMING_SPIDER_LAYER, Supplier { CharmingSpiderEntityModel.createTexturedModelData() })
        event.registerLayerDefinition(WEB_CASTER_LAYER, Supplier { WebCasterEntityModel.createTexturedModelData() })
        event.registerLayerDefinition(DADDY_LONG_LEGS_LAYER, Supplier { DaddyLongLegsEntityModel.createTexturedModelData() })
        event.registerLayerDefinition(JUMPING_SPIDER_LAYER, Supplier { JumpingSpiderEntityModel.createTexturedModelData() })
        event.registerLayerDefinition(SPINY_SPIDER_LAYER, Supplier { SpinySpiderEntityModel.createTexturedModelData() })
        event.registerLayerDefinition(HANGING_COCOON_LAYER, Supplier { HangingCocoonEntityRenderer.createTexturedModelData() })
        event.registerLayerDefinition(NETWALKERS_LAYER, Supplier { NetwalkersRenderer.createTexturedModelData() })
    }

    fun registerRenderers(event: EntityRenderersEvent.RegisterRenderers) {
        event.registerBlockEntityRenderer(ModBlockEntities.SPIDER_EGG_BLOCK_ENTITY) { SpiderEggClusterRenderer(it) }

        event.registerEntityRenderer(ModEntities.ARACHNE) { ArachneEntityRenderer(it) }
        event.registerEntityRenderer(ModEntities.SPIDERLING) { SpiderlingEntityRenderer(it) }
        event.registerEntityRenderer(ModEntities.SPIDERLING_ALLY) { SpiderlingEntityRenderer(it) }
        event.registerEntityRenderer(ModEntities.CHARMING_SPIDER) { CharmingSpiderEntityRenderer(it) }
        event.registerEntityRenderer(ModEntities.WEB_CASTER) { WebCasterEntityRenderer(it) }
        event.registerEntityRenderer(ModEntities.DADDY_LONG_LEGS) { DaddyLongLegsEntityRenderer(it) }
        event.registerEntityRenderer(ModEntities.JUMPING_SPIDER) { JumpingSpiderEntityRenderer(it) }
        event.registerEntityRenderer(ModEntities.SPINY_SPIDER) { SpinySpiderEntityRenderer(it) }
        event.registerEntityRenderer(ModEntities.ARACHNE_EGG) { ArachneEggRenderer(it) }
        event.registerEntityRenderer(ModEntities.SPIDER_SILK_BOLA) { SpiderSilkBolaRenderer(it) }
        event.registerEntityRenderer(ModEntities.SPIDER_EGG_PROJECTILE) { SpiderEggProjectileRenderer(it) }
    }

    /**
     * Fabric: LivingEntityFeatureRendererRegistrationCallback. Adds the cobweb overlay to every living
     * renderer and the netwalkers legs layer to every humanoid one (Fabric had an ArmorRenderer for that).
     */
    fun addLayers(event: EntityRenderersEvent.AddLayers) {
        val renderers = LinkedHashSet<EntityRenderer<*>>()
        event.entityTypes.forEach { type ->
            @Suppress("UNCHECKED_CAST")
            val renderer: EntityRenderer<net.minecraft.world.entity.Entity>? =
                event.getRenderer(type as net.minecraft.world.entity.EntityType<net.minecraft.world.entity.Entity>)
            if (renderer != null) renderers.add(renderer)
        }
        event.skins.forEach { model ->
            val renderer: EntityRenderer<net.minecraft.world.entity.player.Player>? = event.getSkin(model)
            if (renderer != null) renderers.add(renderer)
        }

        renderers.forEach { renderer ->
            if (renderer !is LivingEntityRenderer<*, *>) return@forEach

            @Suppress("UNCHECKED_CAST")
            val livingRenderer = renderer as LivingEntityRenderer<net.minecraft.world.entity.LivingEntity, EntityModel<net.minecraft.world.entity.LivingEntity>>
            livingRenderer.addLayer(SpiderWebModelFeature(livingRenderer))

            if (renderer.model is HumanoidModel<*>) {
                @Suppress("UNCHECKED_CAST")
                val humanoidRenderer =
                    renderer as LivingEntityRenderer<net.minecraft.world.entity.LivingEntity, HumanoidModel<net.minecraft.world.entity.LivingEntity>>
                humanoidRenderer.addLayer(NetwalkersRenderer(humanoidRenderer, event.context))
            }
        }
    }

    fun setupClient() {
        val cutout = RenderType.cutout()
        listOf(
            ModBlocksAndItems.BURNED_WOOD.trapdoor,
            ModBlocksAndItems.BURNED_WOOD.door,
            ModBlocksAndItems.BURNED_LEAVES,
            ModBlocksAndItems.SMOLDERING_LEAVES,
            ModBlocksAndItems.BURNED_GRASS.grassBlock,
            ModBlocksAndItems.BURNED_GRASS.grass,
            ModBlocksAndItems.BURNED_GRASS.tallGrass,
            ModBlocksAndItems.DRIED_GRASS.grassBlock,
            ModBlocksAndItems.DRIED_GRASS.grass,
            ModBlocksAndItems.DRIED_GRASS.tallGrass,
            ModBlocksAndItems.SMOLDERING_EMBERS,
            ModBlocksAndItems.SMOKING_EMBERS,
            ModBlocksAndItems.FLICKERING_LANTERN,
            ModBlocksAndItems.FLICKERING_SOUL_LANTERN,
            ModBlocksAndItems.EXTINGUISHED_LANTERN,
            ModBlocksAndItems.FANCY_COBWEB,
            ModBlocksAndItems.FANCY_COBWEB_WITH_SPIDER_NEST,
            ModBlocksAndItems.FANCY_CORNER_COBWEB,
            ModBlocksAndItems.CORNER_COBWEB,
            *ModBlocksAndItems.NOCTISTEEL.getCutoutBlocks(),
            ModBlocksAndItems.WALL_COBWEB,
            ModBlocksAndItems.SPIDER_SILK,
            ModBlocksAndItems.SPIDER_SILK_STRAND
        ).forEach { ItemBlockRenderTypes.setRenderLayer(it, cutout) }

        // Required for signs: Fabric's WoodTypeBuilder registered the type with the block constructor.
        Sheets.addWoodType(ModBlocksAndItems.BURNED_WOOD.woodType)

        ItemProperties.register(
            ModBlocksAndItems.ARACHNICANNON,
            Derelict.id("targeting")
        ) { _, _, livingEntity, _ -> if (livingEntity?.isUsingItem == true) 1f else 0f }
    }

    fun registerAdditionalModels(event: net.neoforged.neoforge.client.event.ModelEvent.RegisterAdditional) {
        SpiderEggClusterRenderer.MODEL_IDS.forEach {
            event.register(ModelResourceLocation.standalone(it))
        }
        ArachneEggRenderer.MODEL_IDS.forEach {
            event.register(ModelResourceLocation.standalone(it))
        }
    }

    private fun layer(path: String, name: String = "main") =
        ModelLayerLocation(Derelict.id(path), name)
}
