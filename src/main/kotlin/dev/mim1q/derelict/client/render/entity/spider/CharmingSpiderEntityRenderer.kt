package dev.mim1q.derelict.client.render.entity.spider

import dev.mim1q.derelict.Derelict
import dev.mim1q.derelict.entity.spider.CharmingSpiderEntity
import dev.mim1q.derelict.init.client.ModRender
import dev.mim1q.derelict.util.extensions.radians
import dev.mim1q.derelict.util.extensions.setPartialAnglesDegrees
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
import kotlin.math.sin


class CharmingSpiderEntityRenderer(
    ctx: EntityRendererProvider.Context
) : MobRenderer<CharmingSpiderEntity, CharmingSpiderEntityModel>(
    ctx,
    CharmingSpiderEntityModel(ctx.bakeLayer(ModRender.CHARMING_SPIDER_LAYER)),
    0.5f
) {
    init {
        addLayer(createEyesFeatureRenderer(this, TEXTURE))
    }

    override fun getTextureLocation(entity: CharmingSpiderEntity): ResourceLocation = TEXTURE

    companion object {
        val TEXTURE = Derelict.id("textures/entity/spider/charming_spider.png")
    }
}

class CharmingSpiderEntityModel(
    part: ModelPart
) : EntityModel<CharmingSpiderEntity>(RenderType::entityCutout) {
    private val root: ModelPart = part.getChild("root")
    private val leftLegs: ModelPart = root.getChild("left_legs")
    private val rightLegs: ModelPart = root.getChild("right_legs")
    private val torso: ModelPart = root.getChild("torso")
    private val tail: ModelPart = torso.getChild("tail")
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
    ) = root.render(matrices, vertices, light, overlay)

    override fun setupAnim(
        entity: CharmingSpiderEntity,
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

        val tailWiggle = entity.tailDanceAnimation.update(animationProgress)
        val bodyWiggle = entity.bodyDanceAnimation.update(animationProgress)

        tail.zRot += 180f.radians() * tailWiggle
        tail.xRot -= 60f.radians() * tailWiggle
        tail.yRot += tailWiggle * sin(animationProgress * 0.5f) * 30f.radians()

        allLegs[1].setPartialAnglesDegrees(0f, -65f + bodyWiggle * 15f, 15f, tailWiggle)
        allLegs[5].setPartialAnglesDegrees(0f, 65f + bodyWiggle * 15f, -15f, tailWiggle)

        torso.x += bodyWiggle * 0.5f
        torso.yRot = bodyWiggle * 5f.radians()

        for (i in 0..7) {
            allLegs[i].yRot += bodyWiggle * 5f.radians()
        }

        head.yRot = headYaw.radians()
        head.xRot = headPitch.radians()
    }

    companion object {
        fun createTexturedModelData(): LayerDefinition = MeshDefinition().let {
            it.root.apply {
                addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0F, 23F, 3F)).apply {
                    addOrReplaceChild("left_legs", CubeListBuilder.create(), PartPose.offset(2F, -3F, -3F)).apply {
                        addOrReplaceChild("left_leg0", CubeListBuilder.create().texOffs(26, 26).addBox(0F, -1F, -1F, 12F, 2F, 2F), PartPose.offset(0F, 0F, -1F))
                        addOrReplaceChild("left_leg1", CubeListBuilder.create().texOffs(26, 22).addBox(0F, -1F, -1F, 12F, 2F, 2F), PartPose.offset(0F, 0F, 1F))
                        addOrReplaceChild("left_leg2", CubeListBuilder.create().texOffs(26, 18).addBox(0F, -1F, -1F, 12F, 2F, 2F), PartPose.offset(0F, 0F, 1F))
                        addOrReplaceChild("left_leg3", CubeListBuilder.create().texOffs(26, 14).addBox(0F, -1F, -1F, 12F, 2F, 2F), PartPose.offset(0F, 0F, 3F))
                    }
                    addOrReplaceChild("right_legs", CubeListBuilder.create(), PartPose.offset(-2F, -3F, -3F)).apply {
                        addOrReplaceChild("right_leg0", CubeListBuilder.create().texOffs(0, 24).addBox(-12F, -1F, -1F, 12F, 2F, 2F), PartPose.offset(0F, 0F, -1F))
                        addOrReplaceChild("right_leg1", CubeListBuilder.create().texOffs(0, 20).addBox(-12F, -1F, -1F, 12F, 2F, 2F), PartPose.offset(0F, 0F, 1F))
                        addOrReplaceChild("right_leg2", CubeListBuilder.create().texOffs(0, 16).addBox(-12F, -1F, -1F, 12F, 2F, 2F), PartPose.offset(0F, 0F, 1F))
                        addOrReplaceChild("right_leg3", CubeListBuilder.create().texOffs(0, 12).addBox(-12F, -1F, -1F, 12F, 2F, 2F), PartPose.offset(0F, 0F, 3F))
                    }
                    addOrReplaceChild("torso", CubeListBuilder.create().texOffs(0, 28).addBox(-2F, -5F, -6F, 4F, 3F, 6F), PartPose.ZERO).apply {
                        addOrReplaceChild("tail", CubeListBuilder.create().texOffs(0, 0).addBox(-8F, 0F, 0F, 16F, 0F, 12F), PartPose.offset(0F, -5F, 0F))
                        addOrReplaceChild("head", CubeListBuilder.create().texOffs(20, 30).addBox(-3F, -2F, -4F, 6F, 4F, 4F).texOffs(0, 0).addBox(-2.5F, 2F, -4F, 2F, 2F, 2F).texOffs(0, 0).addBox(0.5F, 2F, -4F, 2F, 2F, 2F), PartPose.offset(0F, -4F, -6F))
                    }
                }
            }
            LayerDefinition.create(it, 64, 64)
        }
    }
}