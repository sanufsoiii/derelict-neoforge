package dev.mim1q.derelict.client.render.armor

import com.mojang.blaze3d.vertex.PoseStack
import dev.mim1q.derelict.Derelict
import dev.mim1q.derelict.init.ModBlocksAndItems
import dev.mim1q.derelict.init.client.ModRender
import net.minecraft.client.model.HumanoidModel
import net.minecraft.client.model.geom.PartPose
import net.minecraft.client.model.geom.builders.CubeDeformation
import net.minecraft.client.model.geom.builders.CubeListBuilder
import net.minecraft.client.model.geom.builders.LayerDefinition
import net.minecraft.client.model.geom.builders.MeshDefinition
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.RenderLayerParent
import net.minecraft.client.renderer.entity.layers.RenderLayer
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.LivingEntity

/**
 * Fabric used an {@code ArmorRenderer} registered for the netwalkers item. NeoForge draws armor by the
 * (empty) layer list of the material, so the same model is added as a regular render layer to every
 * humanoid renderer; it only draws when the entity wears the leggings.
 */
class NetwalkersRenderer<T : LivingEntity, M : HumanoidModel<T>>(
    parent: RenderLayerParent<T, M>,
    ctx: EntityRendererProvider.Context
) : RenderLayer<T, M>(parent) {
    private val modelRoot = ctx.bakeLayer(ModRender.NETWALKERS_LAYER)
    private val modelWaist = modelRoot.getChild("waist")
    private val modelLeftLeg = modelRoot.getChild("left_leg")
    private val modelRightLeg = modelRoot.getChild("right_leg")

    override fun render(
        matrices: PoseStack,
        vertexConsumers: MultiBufferSource,
        light: Int,
        entity: T,
        limbAngle: Float,
        limbDistance: Float,
        tickDelta: Float,
        animationProgress: Float,
        headYaw: Float,
        headPitch: Float
    ) {
        if (!entity.getItemBySlot(EquipmentSlot.LEGS).`is`(ModBlocksAndItems.NETWALKERS)) return

        val parentModel = getParentModel()
        modelWaist.copyFrom(parentModel.body)
        modelRightLeg.copyFrom(parentModel.rightLeg)
        modelLeftLeg.copyFrom(parentModel.leftLeg)

        modelRoot.render(
            matrices,
            vertexConsumers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)),
            light,
            OverlayTexture.NO_OVERLAY
        )
    }

    companion object {
        val TEXTURE = Derelict.id("textures/armor/netwalkers.png")

        fun createTexturedModelData(): LayerDefinition = MeshDefinition().let {
            it.root.apply {
                addOrReplaceChild(
                    "waist",
                    CubeListBuilder.create().texOffs(0, 22).addBox(-4.5F, 7F, -2.5F, 9F, 5F, 5F, CubeDeformation(0.1F)),
                    PartPose.ZERO
                )
                addOrReplaceChild(
                    "left_leg",
                    CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(-2.5F, 0F, -2.48F, 5F, 12F, 5F)
                        .texOffs(44, 23).mirror()
                        .addBox(-2.5F, 4F, -2.48F, 5F, 4F, 5F, CubeDeformation(0.01F)),
                    PartPose.offset(1.9F, 12F, 0F)
                ).apply {
                    addOrReplaceChild(
                        "left_leg_r1",
                        CubeListBuilder.create().texOffs(30, 20).mirror().addBox(0F, -6F, 0F, 1F, 12F, 0F),
                        PartPose.offsetAndRotation(2.5F, 6F, 2.48F, 0F, -0.7854F, 0F)
                    )
                    addOrReplaceChild(
                        "left_leg_r2",
                        CubeListBuilder.create().texOffs(28, 20).mirror().addBox(0F, -6F, 0F, 1F, 12F, 0F),
                        PartPose.offsetAndRotation(2.5F, 6F, -2.48F, 0F, 0.7854F, 0F)
                    )
                }
                addOrReplaceChild(
                    "right_leg",
                    CubeListBuilder.create().texOffs(20, 0)
                        .addBox(-2.5F, 0F, -2.5F, 5F, 12F, 5F)
                        .texOffs(42, 0)
                        .addBox(-2.75F, -4F, -2.75F, 6F, 9F, 5F),
                    PartPose.offset(-1.9F, 12F, 0F)
                ).apply {
                    addOrReplaceChild(
                        "right_leg_r1",
                        CubeListBuilder.create().texOffs(32, 20).addBox(-1F, -6F, 0F, 1F, 12F, 0F),
                        PartPose.offsetAndRotation(-2.5F, 6F, 2.48F, 0F, 0.7854F, 0F)
                    )
                    addOrReplaceChild(
                        "right_leg_r2",
                        CubeListBuilder.create().texOffs(34, 20).addBox(-1F, -6F, 0F, 1F, 12F, 0F),
                        PartPose.offsetAndRotation(-2.5F, 6F, -2.48F, 0F, -0.7854F, 0F)
                    )
                }
            }
            LayerDefinition.create(it, 64, 32)
        }
    }
}
