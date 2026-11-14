package dev.mim1q.derelict.client.render.entity.spider

import dev.mim1q.derelict.Derelict
import dev.mim1q.derelict.entity.spider.WebCasterEntity
import dev.mim1q.derelict.entity.spider.legs.SpiderLegParts
import dev.mim1q.derelict.init.client.ModRender
import dev.mim1q.derelict.util.extensions.radians
import dev.mim1q.derelict.util.extensions.setPartialAnglesDegrees
import dev.mim1q.derelict.vendor.gimm1q.interpolation.Easing
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

class WebCasterEntityRenderer(
    ctx: EntityRendererProvider.Context
) : MobRenderer<WebCasterEntity, WebCasterEntityModel>(
    ctx,
    WebCasterEntityModel(ctx.bakeLayer(ModRender.WEB_CASTER_LAYER)),
    1.5f
) {
    init {
        addLayer(createEyesFeatureRenderer(this, TEXTURE))
    }

    override fun getTextureLocation(entity: WebCasterEntity): ResourceLocation = TEXTURE

    companion object {
        val TEXTURE = Derelict.id("textures/entity/spider/web_caster.png")
    }
}

class WebCasterEntityModel(part: ModelPart) : EntityModel<WebCasterEntity>(RenderType::entityCutout) {

    private val root = part.getChild("root")
    private val web = root.getChild("web")
    private val torso = root.getChild("torso")
    private val head = torso.getChild("head")
    private val back = torso.getChild("back")

    private val legs = SpiderLegParts.createArray(torso)

    override fun renderToBuffer(
        matrices: PoseStack,
        vertices: VertexConsumer,
        light: Int,
        overlay: Int,
        color: Int
    ) {
        root.render(matrices, vertices, light, overlay, color)
    }

    override fun setupAnim(
        entity: WebCasterEntity,
        limbAngle: Float,
        limbDistance: Float,
        animationProgress: Float,
        headYaw: Float,
        headPitch: Float
    ) {
        head.yRot = headYaw.radians()
        head.xRot = headPitch.radians()

        val webHold = entity.webHeldAnimation.update(animationProgress)

        web.xScale = webHold
        web.yScale = webHold

        legs[0].joint.setPartialAnglesDegrees(0f, 0f, 75f, webHold)
        legs[0].upper.setPartialAnglesDegrees(0f, -40f, 0f, webHold)
        legs[0].lower.setPartialAnglesDegrees(0f, 40f, 0f, webHold)

        legs[4].joint.setPartialAnglesDegrees(0f, 0f, -75f, webHold)
        legs[4].upper.setPartialAnglesDegrees(0f, 40f, 0f, webHold)
        legs[4].lower.setPartialAnglesDegrees(0f, -40f, 0f, webHold)

        legs[1].joint.setPartialAnglesDegrees(0f, 0f, 76f, webHold)
        legs[1].upper.setPartialAnglesDegrees(0f, 15f, 0f, webHold)
        legs[1].lower.setPartialAnglesDegrees(0f, -16f, 0f, webHold)

        legs[5].joint.setPartialAnglesDegrees(0f, 0f, -76f, webHold)
        legs[5].upper.setPartialAnglesDegrees(0f, -15f, 0f, webHold)
        legs[5].lower.setPartialAnglesDegrees(0f, 16f, 0f, webHold)

        web.z -= 20f * webHold
        web.xRot -= 10f.radians() * webHold
    }

    override fun prepareMobModel(entity: WebCasterEntity, limbAngle: Float, limbDistance: Float, tickDelta: Float) {
        super.prepareMobModel(entity, limbAngle, limbDistance, tickDelta)

        root.getAllParts().forEach(ModelPart::resetPose)
        legs.forEachIndexed { index, it ->
            it.applyIk(entity.legController.getIk(index), Easing.lerp(entity.yBodyRotO, entity.yBodyRot, tickDelta), tickDelta)
        }
    }

    companion object {
        fun createTexturedModelData(): LayerDefinition = MeshDefinition().let {
            it.root.apply {
                addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0F, 24F, 0F)).apply {
                    addOrReplaceChild("torso", CubeListBuilder.create().texOffs(44, 52).addBox(-4F, -3F, -4F, 8F, 6F, 8F), PartPose.offset(0F, -8F, -4F)).apply {
                        addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 54).addBox(-3.5F, -2.5F, -8F, 7F, 5F, 8F).texOffs(0, 7).addBox(-4.5F, -3F, -8.5F, 4F, 4F, 3F).texOffs(0, 0).addBox(0.5F, -3F, -8.5F, 4F, 4F, 3F), PartPose.offset(0F, -1.5F, -4F)).apply {
                            addOrReplaceChild("right_fang", CubeListBuilder.create(), PartPose.offset(-2F, 2.5F, -6F)).apply {
                                addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(60, 24).addBox(-1F, -3.5F, -1.5F, 2F, 6F, 3F), PartPose.offsetAndRotation(0F, 3.5F, 0F, 0F, 0.7854F, 0F))
                            }
                            addOrReplaceChild("left_fang", CubeListBuilder.create(), PartPose.offset(2F, 2.5F, -6F)).apply {
                                addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(30, 54).addBox(-1F, -3.5F, -1.5F, 2F, 6F, 3F), PartPose.offsetAndRotation(0F, 3.5F, 0F, 0F, -0.7854F, 0F))
                            }
                        }
                        addOrReplaceChild("back", CubeListBuilder.create().texOffs(0, 0).addBox(-3F, -3F, 0F, 6F, 6F, 20F), PartPose.offset(0F, 0F, 4F))
                        addOrReplaceChild("left_leg_joint0", CubeListBuilder.create(), PartPose.offset(4F, 0F, -3F)).apply {
                            addOrReplaceChild("left_leg0", CubeListBuilder.create().texOffs(52, 20).addBox(0F, -1F, -1F, 24F, 2F, 2F).texOffs(0, 14).addBox(23.5F, -1.5F, -1.5F, 3F, 3F, 3F), PartPose.ZERO).apply {
                                addOrReplaceChild("left_leg_front0", CubeListBuilder.create().texOffs(0, 34).addBox(-1F, -1F, -1F, 28F, 2F, 2F), PartPose.offset(25F, 0F, 0F))
                            }
                        }
                        addOrReplaceChild("left_leg_joint1", CubeListBuilder.create(), PartPose.offset(4F, 0F, -1F)).apply {
                            addOrReplaceChild("left_leg1", CubeListBuilder.create().texOffs(50, 48).addBox(0F, -1F, -1F, 24F, 2F, 2F).texOffs(0, 14).addBox(23.5F, -1.5F, -1.5F, 3F, 3F, 3F), PartPose.ZERO).apply {
                                addOrReplaceChild("left_leg_front1", CubeListBuilder.create().texOffs(32, 16).addBox(-1F, -1F, -1F, 28F, 2F, 2F), PartPose.offset(25F, 0F, 0F))
                            }
                        }
                        addOrReplaceChild("left_leg_joint2", CubeListBuilder.create(), PartPose.offset(4F, 0F, 1F)).apply {
                            addOrReplaceChild("left_leg2", CubeListBuilder.create().texOffs(50, 44).addBox(0F, -1F, -1F, 24F, 2F, 2F).texOffs(0, 14).addBox(23.5F, -1.5F, -1.5F, 3F, 3F, 3F), PartPose.ZERO).apply {
                                addOrReplaceChild("left_leg_front2", CubeListBuilder.create().texOffs(32, 12).addBox(-1F, -1F, -1F, 28F, 2F, 2F), PartPose.offset(25F, 0F, 0F))
                            }
                        }
                        addOrReplaceChild("left_leg_joint3", CubeListBuilder.create(), PartPose.offset(4F, 0F, 3F)).apply {
                            addOrReplaceChild("left_leg3", CubeListBuilder.create().texOffs(50, 40).addBox(0F, -1F, -1F, 24F, 2F, 2F).texOffs(0, 14).addBox(23.5F, -1.5F, -1.5F, 3F, 3F, 3F), PartPose.ZERO).apply {
                                addOrReplaceChild("left_leg_front3", CubeListBuilder.create().texOffs(32, 8).addBox(-1F, -1F, -1F, 28F, 2F, 2F), PartPose.offset(25F, 0F, 0F))
                            }
                        }
                        addOrReplaceChild("right_leg_joint0", CubeListBuilder.create(), PartPose.offset(-4F, 0F, -3F)).apply {
                            addOrReplaceChild("right_leg0", CubeListBuilder.create().texOffs(0, 50).addBox(-24F, -1F, -1F, 24F, 2F, 2F).texOffs(0, 14).mirror().addBox(-26.5F, -1.5F, -1.5F, 3F, 3F, 3F), PartPose.ZERO).apply {
                                addOrReplaceChild("right_leg_front0", CubeListBuilder.create().texOffs(32, 4).addBox(-27F, -1F, -1F, 28F, 2F, 2F), PartPose.offset(-25F, 0F, 0F))
                            }
                        }
                        addOrReplaceChild("right_leg_joint1", CubeListBuilder.create(), PartPose.offset(-4F, 0F, -1F)).apply {
                            addOrReplaceChild("right_leg1", CubeListBuilder.create().texOffs(0, 46).addBox(-24F, -1F, -1F, 24F, 2F, 2F).texOffs(0, 14).mirror().addBox(-26.5F, -1.5F, -1.5F, 3F, 3F, 3F), PartPose.ZERO).apply {
                                addOrReplaceChild("right_leg_front1", CubeListBuilder.create().texOffs(32, 0).addBox(-27F, -1F, -1F, 28F, 2F, 2F), PartPose.offset(-25F, 0F, 0F))
                            }
                        }
                        addOrReplaceChild("right_leg_joint2", CubeListBuilder.create(), PartPose.offset(-4F, 0F, 1F)).apply {
                            addOrReplaceChild("right_leg2", CubeListBuilder.create().texOffs(0, 42).addBox(-24F, -1F, -1F, 24F, 2F, 2F).texOffs(0, 14).mirror().addBox(-26.5F, -1.5F, -1.5F, 3F, 3F, 3F), PartPose.ZERO).apply {
                                addOrReplaceChild("right_leg_front2", CubeListBuilder.create().texOffs(0, 30).addBox(-27F, -1F, -1F, 28F, 2F, 2F), PartPose.offset(-25F, 0F, 0F))
                            }
                        }
                        addOrReplaceChild("right_leg_joint3", CubeListBuilder.create(), PartPose.offset(-4F, 0F, 3F)).apply {
                            addOrReplaceChild("right_leg3", CubeListBuilder.create().texOffs(0, 38).addBox(-24F, -1F, -1F, 24F, 2F, 2F).texOffs(0, 14).mirror().addBox(-26.5F, -1.5F, -1.5F, 3F, 3F, 3F), PartPose.ZERO).apply {
                                addOrReplaceChild("right_leg_front3", CubeListBuilder.create().texOffs(0, 26).addBox(-27F, -1F, -1F, 28F, 2F, 2F), PartPose.offset(-25F, 0F, 0F))
                            }
                        }
                    }
                    addOrReplaceChild("web", CubeListBuilder.create().texOffs(0, 104).addBox(-16F, -12F, 0F, 32F, 24F, 0F), PartPose.offset(0F, -13F, -30F))
                }
            }
            LayerDefinition.create(it, 128, 128)
        }
    }
}