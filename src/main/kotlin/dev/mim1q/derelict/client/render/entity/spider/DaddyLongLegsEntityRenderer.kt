package dev.mim1q.derelict.client.render.entity.spider

import dev.mim1q.derelict.Derelict
import dev.mim1q.derelict.entity.spider.DaddyLongLegsEntity
import dev.mim1q.derelict.entity.spider.legs.SpiderLegParts
import dev.mim1q.derelict.init.client.ModRender
import dev.mim1q.derelict.util.render.entry
import dev.mim1q.derelict.vendor.gimm1q.interpolation.Easing
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.client.model.geom.PartPose
import net.minecraft.client.model.geom.builders.CubeDeformation
import net.minecraft.client.model.geom.builders.CubeListBuilder
import net.minecraft.client.model.geom.builders.LayerDefinition
import net.minecraft.client.model.geom.builders.MeshDefinition
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.client.renderer.RenderType
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.MobRenderer
import net.minecraft.client.model.EntityModel
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.world.item.DyeColor
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import com.mojang.math.Axis
import kotlin.math.cos
import kotlin.math.sin

class DaddyLongLegsEntityRenderer(
    context: EntityRendererProvider.Context,
) : MobRenderer<DaddyLongLegsEntity, DaddyLongLegsEntityModel>(context, DaddyLongLegsEntityModel(context.bakeLayer(ModRender.DADDY_LONG_LEGS_LAYER)), 0.5f) {
    init {
        addLayer(createEyesFeatureRenderer(this, TEXTURE))
    }

    override fun getTextureLocation(entity: DaddyLongLegsEntity): ResourceLocation = TEXTURE

    override fun render(mobEntity: DaddyLongLegsEntity, f: Float, g: Float, matrixStack: PoseStack, vertexConsumerProvider: MultiBufferSource, i: Int) {
        super.render(mobEntity, f, g, matrixStack, vertexConsumerProvider, i)

        val sockColor = mobEntity.sockColor
        if (sockColor == null || mobEntity.isDeadOrDying) return

        matrixStack.entry {
            scale(-1f, -1f, 1f)
            translate(0.0, -1.501, 0.0)
            mulPose(Axis.YP.rotationDegrees(180f + Mth.rotLerp(g, mobEntity.yBodyRotO, mobEntity.yBodyRot)))

            getModel().renderToBuffer(
                matrixStack,
                vertexConsumerProvider.getBuffer(RenderType.entityCutoutNoCull(SOCK_TEXTURES[sockColor])),
                i, OverlayTexture.NO_OVERLAY,
                0xFFFFFFFF.toInt()
            )
        }
    }

    companion object {
        val TEXTURE = Derelict.id("textures/entity/spider/daddy_long_legs.png")
        val SOCK_TEXTURES = DyeColor.entries.associateWith { Derelict.id("textures/entity/spider/sock/${it.getName()}_sock.png") }
    }
}

class DaddyLongLegsEntityModel(part: ModelPart) : EntityModel<DaddyLongLegsEntity>() {
    val root = part.getChild("root")
    private val body = root.getChild("body")
    val legs = SpiderLegParts.createArray(root)

    override fun renderToBuffer(matrices: PoseStack, vertices: VertexConsumer, light: Int, overlay: Int, color: Int) {
        root.render(matrices, vertices, light, overlay, color)
    }

    override fun setupAnim(entity: DaddyLongLegsEntity, limbAngle: Float, limbDistance: Float, animationProgress: Float, headYaw: Float, headPitch: Float) {
        root.y -= 10f

        val dance = entity.danceAnimation.update(animationProgress)

        root.x += (cos(animationProgress * 0.5f - Mth.PI / 3f) * 12f) * dance
        root.y -= (sin(animationProgress * 0.5f - Mth.PI / 3f) * 16f - 12f) * dance
    }

    override fun prepareMobModel(entity: DaddyLongLegsEntity, limbAngle: Float, limbDistance: Float, tickDelta: Float) {
        super.prepareMobModel(entity, limbAngle, limbDistance, tickDelta)

        root.getAllParts().forEach(ModelPart::resetPose)

        legs.forEachIndexed { index, it ->
            it.applyIk(entity.legController.getIk(index), Easing.lerp(entity.yBodyRotO, entity.yBodyRot, tickDelta), tickDelta)
        }
    }

    companion object {
        fun createTexturedModelData(): LayerDefinition = MeshDefinition().let {
            it.root.apply {
                addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0F, 21F, 0F)).apply {
                    addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 16).addBox(-4F, -4F, -4F, 8F, 8F, 8F), PartPose.offset(0F, -46F, 0F)).apply {
                        addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(46, 48).addBox(-4.5F, -4F, 0F, 9F, 9F, 0F), PartPose.offsetAndRotation(3.5F, -4F, -4.5F, 0F, -0.5236F, 0F))
                    }
                    addOrReplaceChild("left_leg_joint0", CubeListBuilder.create(), PartPose.offset(3F, -44F, -1.5F)).apply {
                        addOrReplaceChild("left_leg0", CubeListBuilder.create().texOffs(0, 15).addBox(0F, -0.5F, 0F, 32F, 1F, 0F), PartPose.ZERO).apply {
                            addOrReplaceChild("left_leg_front0", CubeListBuilder.create().texOffs(0, 14).addBox(0F, -0.5F, 0F, 32F, 1F, 0F), PartPose.offset(32F, 0F, 0F)).apply {
                                addOrReplaceChild("left_sock0", CubeListBuilder.create().texOffs(2, 62).addBox(2.5F, -1F, 0F, 30F, 1F, 0F, CubeDeformation(0.1F)), PartPose.offset(0F, 0.5F, 0F))
                            }
                        }
                    }
                    addOrReplaceChild("right_leg_joint0", CubeListBuilder.create(), PartPose.offset(-3F, -44F, -1.5F)).apply {
                        addOrReplaceChild("right_leg0", CubeListBuilder.create().texOffs(0, 15).mirror().addBox(-32F, -0.5F, 0F, 32F, 1F, 0F), PartPose.ZERO).apply {
                            addOrReplaceChild("right_leg_front0", CubeListBuilder.create().texOffs(0, 14).mirror().addBox(-32F, -0.5F, 0F, 32F, 1F, 0F), PartPose.offset(-32F, 0F, 0F)).apply {
                                addOrReplaceChild("right_sock0", CubeListBuilder.create().texOffs(2, 62).mirror().addBox(-32.5F, -1F, 0F, 30F, 1F, 0F, CubeDeformation(0.1F)), PartPose.offset(0F, 0.5F, 0F))
                            }
                        }
                    }
                    addOrReplaceChild("left_leg_joint1", CubeListBuilder.create(), PartPose.offset(3F, -44F, -0.5F)).apply {
                        addOrReplaceChild("left_leg1", CubeListBuilder.create().texOffs(0, 13).addBox(0F, -0.5F, 0F, 32F, 1F, 0F), PartPose.ZERO).apply {
                            addOrReplaceChild("left_leg_front1", CubeListBuilder.create().texOffs(0, 12).addBox(0F, -0.5F, 0F, 32F, 1F, 0F), PartPose.offset(32F, 0F, 0F)).apply {
                                addOrReplaceChild("left_sock1", CubeListBuilder.create().texOffs(2, 63).addBox(2.5F, -1F, 0F, 30F, 1F, 0F, CubeDeformation(0.1F)), PartPose.offset(0F, 0.5F, 0F))
                            }
                        }
                    }
                    addOrReplaceChild("right_leg_joint1", CubeListBuilder.create(), PartPose.offset(-3F, -44F, -0.5F)).apply {
                        addOrReplaceChild("right_leg1", CubeListBuilder.create().texOffs(0, 13).mirror().addBox(-32F, -0.5F, 0F, 32F, 1F, 0F), PartPose.ZERO).apply {
                            addOrReplaceChild("right_leg_front1", CubeListBuilder.create().texOffs(0, 12).mirror().addBox(-32F, -0.5F, 0F, 32F, 1F, 0F), PartPose.offset(-32F, 0F, 0F)).apply {
                                addOrReplaceChild("right_sock1", CubeListBuilder.create().texOffs(2, 63).mirror().addBox(-32.5F, -1F, 0F, 30F, 1F, 0F, CubeDeformation(0.1F)), PartPose.offset(0F, 0.5F, 0F))
                            }
                        }
                    }
                    addOrReplaceChild("left_leg_joint2", CubeListBuilder.create(), PartPose.offset(3F, -44F, 0.5F)).apply {
                        addOrReplaceChild("left_leg2", CubeListBuilder.create().texOffs(0, 15).addBox(0F, -0.5F, 0F, 32F, 1F, 0F), PartPose.ZERO).apply {
                            addOrReplaceChild("left_leg_front2", CubeListBuilder.create().texOffs(0, 14).addBox(0F, -0.5F, 0F, 32F, 1F, 0F), PartPose.offset(32F, 0F, 0F)).apply {
                                addOrReplaceChild("left_sock2", CubeListBuilder.create().texOffs(2, 62).addBox(2.5F, -1F, 0F, 30F, 1F, 0F, CubeDeformation(0.1F)), PartPose.offset(0F, 0.5F, 0F))
                            }
                        }
                    }
                    addOrReplaceChild("right_leg_joint2", CubeListBuilder.create(), PartPose.offset(-3F, -44F, 0.5F)).apply {
                        addOrReplaceChild("right_leg2", CubeListBuilder.create().texOffs(0, 15).mirror().addBox(-32F, -0.5F, 0F, 32F, 1F, 0F), PartPose.ZERO).apply {
                            addOrReplaceChild("right_leg_front2", CubeListBuilder.create().texOffs(0, 14).mirror().addBox(-32F, -0.5F, 0F, 32F, 1F, 0F), PartPose.offset(-32F, 0F, 0F)).apply {
                                addOrReplaceChild("right_sock2", CubeListBuilder.create().texOffs(2, 62).mirror().addBox(-32.5F, -1F, 0F, 30F, 1F, 0F, CubeDeformation(0.1F)), PartPose.offset(0F, 0.5F, 0F))
                            }
                        }
                    }
                    addOrReplaceChild("left_leg_joint3", CubeListBuilder.create(), PartPose.offset(3F, -44F, 1.5F)).apply {
                        addOrReplaceChild("left_leg3", CubeListBuilder.create().texOffs(0, 15).addBox(0F, -0.5F, 0F, 32F, 1F, 0F), PartPose.ZERO).apply {
                            addOrReplaceChild("left_leg_front3", CubeListBuilder.create().texOffs(0, 14).addBox(0F, -0.5F, 0F, 32F, 1F, 0F), PartPose.offset(32F, 0F, 0F)).apply {
                                addOrReplaceChild("left_sock3", CubeListBuilder.create().texOffs(2, 63).addBox(2.5F, -1F, 0F, 30F, 1F, 0F, CubeDeformation(0.1F)), PartPose.offset(0F, 0.5F, 0F))
                            }
                        }
                    }
                    addOrReplaceChild("right_leg_joint3", CubeListBuilder.create(), PartPose.offset(-3F, -44F, 1.5F)).apply {
                        addOrReplaceChild("right_leg3", CubeListBuilder.create().texOffs(0, 15).mirror().addBox(-32F, -0.5F, 0F, 32F, 1F, 0F), PartPose.ZERO).apply {
                            addOrReplaceChild("right_leg_front3", CubeListBuilder.create().texOffs(0, 14).mirror().addBox(-32F, -0.5F, 0F, 32F, 1F, 0F), PartPose.offset(-32F, 0F, 0F)).apply {
                                addOrReplaceChild("right_sock3", CubeListBuilder.create().texOffs(2, 63).mirror().addBox(-32.5F, -1F, 0F, 30F, 1F, 0F, CubeDeformation(0.1F)), PartPose.offset(0F, 0.5F, 0F))
                            }
                        }
                    }
                }
            }
            LayerDefinition.create(it, 64, 64)
        }
    }
}