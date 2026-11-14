package dev.mim1q.derelict.client.render.entity.spider

import dev.mim1q.derelict.Derelict
import dev.mim1q.derelict.entity.spider.JumpingSpiderEntity
import dev.mim1q.derelict.init.client.ModRender
import dev.mim1q.derelict.util.extensions.radians
import dev.mim1q.derelict.util.extensions.setPartialAnglesDegrees
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.client.model.geom.PartPose
import net.minecraft.client.model.geom.builders.CubeListBuilder
import net.minecraft.client.model.geom.builders.LayerDefinition
import net.minecraft.client.model.geom.builders.MeshDefinition
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.MobRenderer
import net.minecraft.client.model.EntityModel
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth

class JumpingSpiderEntityRenderer(
    context: EntityRendererProvider.Context
) : MobRenderer<JumpingSpiderEntity, JumpingSpiderEntityModel>(
    context,
    JumpingSpiderEntityModel(context.bakeLayer(ModRender.JUMPING_SPIDER_LAYER)),
    0.5F
) {
    init {
        addLayer(createEyesFeatureRenderer(this, TEXTURE))
    }

    override fun getTextureLocation(entity: JumpingSpiderEntity): ResourceLocation = TEXTURE

    companion object {
        val TEXTURE = Derelict.id("textures/entity/spider/jumping_spider.png")
    }
}

class JumpingSpiderEntityModel(bone: ModelPart) : EntityModel<JumpingSpiderEntity>() {
    private val root = bone.getChild("root")
    private val leftLegs: ModelPart = root.getChild("left_legs")
    private val rightLegs: ModelPart = root.getChild("right_legs")
    private val head = root.getChild("head")

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
        entity: JumpingSpiderEntity,
        limbAngle: Float,
        limbDistance: Float,
        animationProgress: Float,
        headYaw: Float,
        headPitch: Float
    ) {
        root.getAllParts().forEach(ModelPart::resetPose)

        leftLegs.zRot = (25f).radians()
        rightLegs.zRot = (-25f).radians()

        walkSpiderLegs(allLegs, animationProgress, limbDistance)

        val jumpCharge = entity.jumpChargeAnimation.update(animationProgress)

        if (jumpCharge > Mth.EPSILON) {
            allLegs.forEachIndexed { index, it ->
                val multiplier = if (index < 4) 1 else -1
                val i = index % 4
                it.setPartialAnglesDegrees(0f, (5f - i * 5f) * multiplier, (60f - (10f * i)) * multiplier, jumpCharge)
            }

            root.y += 4f * jumpCharge
            root.z += 8f * jumpCharge
        }

        head.xRot = headPitch.radians()
        head.yRot = headYaw.radians()
    }

    companion object {
        fun createTexturedModelData(): LayerDefinition = MeshDefinition().let {
            it.root.apply {
                addOrReplaceChild("root", CubeListBuilder.create().texOffs(30, 32).addBox(-3F, -2F, 0F, 6F, 4F, 6F).texOffs(0, 0).addBox(-5F, -3F, 6F, 10F, 6F, 10F).texOffs(0, 43).addBox(0F, -5F, 7F, 0F, 2F, 10F), PartPose.offset(0F, 15F, -3F)).apply {
                    addOrReplaceChild("body_r1", CubeListBuilder.create().texOffs(0, 43).addBox(0F, 0F, -5F, 0F, 1F, 10F), PartPose.offsetAndRotation(-3.5F, -4F, 11F, 0F, -0.2618F, 0F))
                    addOrReplaceChild("body_r2", CubeListBuilder.create().texOffs(0, 43).addBox(0F, 0F, -5F, 0F, 1F, 10F), PartPose.offsetAndRotation(3.5F, -4F, 11F, 0F, 0.2618F, 0F))
                    addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 16).addBox(-4F, -3F, -8F, 8F, 6F, 8F).texOffs(0, 42).addBox(-3F, -4F, -8F, 0F, 1F, 8F).texOffs(0, 42).addBox(0F, -5F, -9F, 0F, 2F, 8F).texOffs(0, 42).addBox(3F, -4F, -8F, 0F, 1F, 8F).texOffs(0, 0).addBox(0.5F, 3F, -7.5F, 3F, 4F, 2F).texOffs(0, 38).addBox(-3.5F, 3F, -7.5F, 3F, 4F, 2F), PartPose.offset(0F, -1F, 0F))
                    addOrReplaceChild("left_legs", CubeListBuilder.create(), PartPose.offset(4F, 0F, 2.5F)).apply {
                        addOrReplaceChild("left_leg0", CubeListBuilder.create().texOffs(34, 28).addBox(0F, -1F, -1F, 16F, 2F, 2F), PartPose.offset(-1F, 0F, -2.5F))
                        addOrReplaceChild("left_leg1", CubeListBuilder.create().texOffs(0, 34).addBox(0F, -1F, -1F, 16F, 2F, 2F), PartPose.offset(-1F, 0F, -0.5F))
                        addOrReplaceChild("left_leg2", CubeListBuilder.create().texOffs(32, 24).addBox(0F, -1F, -1F, 16F, 2F, 2F), PartPose.offset(-1F, 0F, 1.5F))
                        addOrReplaceChild("left_leg3", CubeListBuilder.create().texOffs(30, 4).addBox(0F, -1F, -1F, 16F, 2F, 2F), PartPose.offset(-1F, 0F, 3.5F))
                    }
                    addOrReplaceChild("right_legs", CubeListBuilder.create(), PartPose.offset(-4F, 0F, 2.5F)).apply {
                        addOrReplaceChild("right_leg0", CubeListBuilder.create().texOffs(30, 0).addBox(-16F, -1F, -1F, 16F, 2F, 2F), PartPose.offset(1F, 0F, -2.5F))
                        addOrReplaceChild("right_leg1", CubeListBuilder.create().texOffs(0, 30).addBox(-16F, -1F, -1F, 16F, 2F, 2F), PartPose.offset(1F, 0F, -0.5F))
                        addOrReplaceChild("right_leg2", CubeListBuilder.create().texOffs(24, 20).addBox(-16F, -1F, -1F, 16F, 2F, 2F), PartPose.offset(1F, 0F, 1.5F))
                        addOrReplaceChild("right_leg3", CubeListBuilder.create().texOffs(24, 16).addBox(-16F, -1F, -1F, 16F, 2F, 2F), PartPose.offset(1F, 0F, 3.5F))
                    }
                }
            }
            LayerDefinition.create(it, 128, 128)
        }
    }
}