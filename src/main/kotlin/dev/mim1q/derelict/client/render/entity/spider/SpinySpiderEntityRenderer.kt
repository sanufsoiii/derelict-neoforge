package dev.mim1q.derelict.client.render.entity.spider

import dev.mim1q.derelict.Derelict
import dev.mim1q.derelict.entity.spider.SpinySpiderEntity
import dev.mim1q.derelict.init.client.ModRender
import dev.mim1q.derelict.util.extensions.radians
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.client.model.geom.PartPose
import net.minecraft.client.model.geom.builders.CubeListBuilder
import net.minecraft.client.model.geom.builders.LayerDefinition
import net.minecraft.client.model.geom.builders.MeshDefinition
import net.minecraft.client.renderer.RenderType
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.MobRenderer
import net.minecraft.client.model.EntityModel
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth

class SpinySpiderEntityRenderer(
    context: EntityRendererProvider.Context
) : MobRenderer<SpinySpiderEntity, SpinySpiderEntityModel>(
    context,
    SpinySpiderEntityModel(context.bakeLayer(ModRender.SPINY_SPIDER_LAYER)),
    0.5f
) {
    init {
        addLayer(createEyesFeatureRenderer(this, TEXTURE))
    }

    override fun getTextureLocation(entity: SpinySpiderEntity): ResourceLocation = TEXTURE

    override fun scale(entity: SpinySpiderEntity, matrixStack: PoseStack, f: Float) {
        var g = entity.getFuse(f)
        val h = 1.0f + Mth.sin(g * 100.0f) * g * 0.01f
        g = Mth.clamp(g, 0.0f, 1.0f)
        g *= g
        g *= g
        val i = (1.0f + g * 0.4f) * h
        val j = (1.0f + g * 0.1f) / h
        matrixStack.scale(i, j, i)
    }

    override fun getWhiteOverlayProgress(entity: SpinySpiderEntity, f: Float): Float {
        val g = entity.getFuse(f)
        return if ((g * 10.0f).toInt() % 2 == 0) 0.0f else Mth.clamp(g, 0.5f, 1.0f)
    }

    companion object {
        val TEXTURE = Derelict.id("textures/entity/spider/spiny_spider.png")
    }
}

class SpinySpiderEntityModel(part: ModelPart) : EntityModel<SpinySpiderEntity>(RenderType::entityCutout) {
    private val root = part.getChild("root")
    private val leftLegs: ModelPart = root.getChild("left_legs")
    private val rightLegs: ModelPart = root.getChild("right_legs")
    private val torso: ModelPart = root.getChild("torso")
    private val head: ModelPart = torso.getChild("head")

    private val allLegs: Array<ModelPart> =
        Array(8) { i ->
            val child = if (i < 4) "left_leg" else "right_leg"
            root.getChild("${child}s").getChild("$child${i % 4}")
        }

    override fun renderToBuffer(
        matrices: PoseStack,
        vertices: VertexConsumer,
        light: Int,
        overlay: Int,
        color: Int
    ) {
        root.render(matrices, vertices, light, overlay)
    }

    override fun setupAnim(
        entity: SpinySpiderEntity,
        limbAngle: Float,
        limbDistance: Float,
        animationProgress: Float,
        headYaw: Float,
        headPitch: Float
    ) {
        root.getAllParts().forEach(ModelPart::resetPose)

        root.y -= 2f

        leftLegs.zRot = (20f).radians()
        rightLegs.zRot = (-20f).radians()

        walkSpiderLegs(allLegs, animationProgress, limbDistance)

        head.yRot = headYaw.radians()
        head.xRot = headPitch.radians()
    }

    companion object {
        fun createTexturedModelData(): LayerDefinition = MeshDefinition().let {
            it.root.apply {
                addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0F, 23F, 1F)).apply {
                    addOrReplaceChild("left_legs", CubeListBuilder.create(), PartPose.offset(2F, -3F, -3F)).apply {
                        addOrReplaceChild("left_leg0", CubeListBuilder.create().texOffs(28, 34).addBox(0F, -1F, -1F, 12F, 2F, 2F), PartPose.offset(0F, 0F, -1F))
                        addOrReplaceChild("left_leg1", CubeListBuilder.create().texOffs(34, 26).addBox(0F, -1F, -1F, 12F, 2F, 2F), PartPose.offset(0F, 0F, 1F))
                        addOrReplaceChild("left_leg2", CubeListBuilder.create().texOffs(34, 22).addBox(0F, -1F, -1F, 12F, 2F, 2F), PartPose.offset(0F, 0F, 1F))
                        addOrReplaceChild("left_leg3", CubeListBuilder.create().texOffs(34, 18).addBox(0F, -1F, -1F, 12F, 2F, 2F), PartPose.offset(0F, 0F, 3F))
                    }
                    addOrReplaceChild("right_legs", CubeListBuilder.create(), PartPose.offset(-2F, -3F, -3F)).apply {
                        addOrReplaceChild("right_leg0", CubeListBuilder.create().texOffs(0, 34).addBox(-12F, -1F, -1F, 12F, 2F, 2F), PartPose.offset(0F, 0F, -1F))
                        addOrReplaceChild("right_leg1", CubeListBuilder.create().texOffs(28, 30).addBox(-12F, -1F, -1F, 12F, 2F, 2F), PartPose.offset(0F, 0F, 1F))
                        addOrReplaceChild("right_leg2", CubeListBuilder.create().texOffs(30, 14).addBox(-12F, -1F, -1F, 12F, 2F, 2F), PartPose.offset(0F, 0F, 1F))
                        addOrReplaceChild("right_leg3", CubeListBuilder.create().texOffs(0, 30).addBox(-12F, -1F, -1F, 12F, 2F, 2F), PartPose.offset(0F, 0F, 3F))
                    }
                    addOrReplaceChild("torso", CubeListBuilder.create().texOffs(0, 0).addBox(-7F, -6F, -3F, 14F, 3F, 11F).texOffs(0, 38).addBox(-2F, -5F, -6F, 4F, 3F, 6F), PartPose.ZERO).apply {
                        addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 14).addBox(-6F, 0F, 0F, 12F, 0F, 6F), PartPose.offsetAndRotation(0F, -6F, 6F, 0.8727F, 0F, 0F))
                        addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 20).addBox(-6F, 0F, -4F, 6F, 0F, 10F), PartPose.offsetAndRotation(-5F, -6F, 1F, 0F, 0F, 0.6981F))
                        addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(12, 20).addBox(0F, 0F, -4F, 6F, 0F, 10F), PartPose.offsetAndRotation(5F, -6F, 1F, 0F, 0F, -0.6981F))
                        addOrReplaceChild("head", CubeListBuilder.create().texOffs(20, 38).addBox(-3F, -2F, -4F, 6F, 4F, 4F), PartPose.offset(0F, -4F, -6F))
                    }
                }
            }
            LayerDefinition.create(it, 64, 64)
        }
    }

}