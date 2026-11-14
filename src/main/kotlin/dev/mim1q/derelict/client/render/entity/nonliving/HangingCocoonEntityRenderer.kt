package dev.mim1q.derelict.client.render.entity.nonliving

import dev.mim1q.derelict.Derelict
import dev.mim1q.derelict.entity.nonliving.HangingCocoonEntity
import dev.mim1q.derelict.entity.nonliving.HangingCocoonEntity.Companion.BROKEN
import dev.mim1q.derelict.init.client.ModRender
import dev.mim1q.derelict.util.render.entry
import net.minecraft.client.model.geom.builders.MeshDefinition
import net.minecraft.client.model.geom.builders.CubeListBuilder
import net.minecraft.client.model.geom.PartPose
import net.minecraft.client.model.geom.builders.LayerDefinition
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.EntityRenderer
import net.minecraft.client.renderer.entity.EntityRendererProvider
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.resources.ResourceLocation
import com.mojang.math.Axis
import kotlin.math.sin

class HangingCocoonEntityRenderer(ctx: EntityRendererProvider.Context) : EntityRenderer<HangingCocoonEntity>(ctx) {
    private val modelPart = ctx.bakeLayer(ModRender.HANGING_COCOON_LAYER)

    override fun render(
        entity: HangingCocoonEntity,
        yaw: Float,
        tickDelta: Float,
        matrices: PoseStack,
        vertexConsumers: MultiBufferSource,
        light: Int
    ) {
        matrices.entry {
            scale(-1f, -1f, 1f)

            translate(0.0, -2.5, 0.0)
            val animationProgress = entity.tickCount + tickDelta
            val punchYaw = entity.punchYaw.update(animationProgress)
            val punchDistance = entity.punchDistance.update(animationProgress)

            mulPose(Axis.YP.rotation(punchYaw))
            mulPose(Axis.XP.rotation(punchDistance * -sin((entity.punchTime + tickDelta) * 0.2f)))
            mulPose(Axis.YP.rotation(-punchYaw))

            translate(0.0, 2.5, 0.0)

            translate(0.0, -2.501, -0.0)

            modelPart.render(
                matrices,
                vertexConsumers.getBuffer(RenderType.entityCutout(getTextureLocation(entity))),
                light,
                OverlayTexture.NO_OVERLAY
            )
        }
    }

    override fun getTextureLocation(entity: HangingCocoonEntity): ResourceLocation =
        if (entity.getEntityData()[BROKEN]) BROKEN_TEXTURE else TEXTURE

    companion object {
        val TEXTURE = Derelict.id("textures/entity/hanging_cocoon/hanging_cocoon.png")
        val BROKEN_TEXTURE = Derelict.id("textures/entity/hanging_cocoon/hanging_cocoon_broken.png")

        fun createTexturedModelData(): LayerDefinition = MeshDefinition().let {
            it.root.apply {
                addOrReplaceChild("root", CubeListBuilder.create().texOffs(0, -10).addBox(-5F, 4F, -5F, 0F, 28F, 10F).texOffs(20, -10).addBox(5F, 4F, -5F, 0F, 28F, 10F).texOffs(20, 28).addBox(-5F, 4F, 5F, 10F, 28F, 0F).texOffs(30, 18).addBox(-5F, 4F, -5F, 10F, 0F, 10F).texOffs(30, 28).addBox(-5F, 32F, -5F, 10F, 0F, 10F).texOffs(0, 28).addBox(-5F, 4F, -5F, 10F, 28F, 0F), PartPose.offset(0F, 8F, 0F)).apply {
                    addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(48, 12).addBox(-1F, 2F, 0F, 1F, 6F, 0F).texOffs(40, 0).addBox(-5F, 7F, 0F, 10F, 6F, 0F), PartPose.offsetAndRotation(0F, -9F, 0F, 0F, -1.1345F, 0F))
                    addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(40, 6).addBox(-5F, 2F, 0F, 10F, 6F, 0F), PartPose.offsetAndRotation(0F, -4F, 0F, 0F, 0.48F, 0F))
                }
            }
            LayerDefinition.create(it, 64, 64)
        }
    }
}