package dev.mim1q.derelict.client.render.entity.spider

import dev.mim1q.derelict.Derelict
import dev.mim1q.derelict.entity.SpiderlingEntity
import dev.mim1q.derelict.init.client.ModRender
import dev.mim1q.derelict.util.extensions.drawBillboard
import dev.mim1q.derelict.util.extensions.radians
import dev.mim1q.derelict.util.render.entry
import dev.mim1q.derelict.vendor.gimm1q.interpolation.Easing
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.client.model.geom.PartPose
import net.minecraft.client.model.geom.builders.CubeDeformation
import net.minecraft.client.model.geom.builders.CubeListBuilder
import net.minecraft.client.model.geom.builders.LayerDefinition
import net.minecraft.client.model.geom.builders.MeshDefinition
import net.minecraft.client.renderer.RenderType
import com.mojang.blaze3d.vertex.VertexConsumer
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.MobRenderer
import net.minecraft.client.model.EntityModel
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.util.Mth
import com.mojang.math.Axis
import org.joml.Vector2f
import kotlin.math.atan2
import kotlin.math.sqrt


class SpiderlingEntityRenderer(context: EntityRendererProvider.Context) :
    MobRenderer<SpiderlingEntity, SpiderlingModel>(
        context, SpiderlingModel(context.bakeLayer(ModRender.SPIDERLING_LAYER)), 0.3f
    ) {

    init {
        addLayer(createEyesFeatureRenderer(this, TEXTURE))
    }

    companion object {
        val TEXTURE = Derelict.id("textures/entity/spider/spiderling.png")
        val TEXTURE_PINK = Derelict.id("textures/entity/spider/spiderling_pink.png")
    }

    override fun getTextureLocation(entity: SpiderlingEntity) = if (entity is SpiderlingEntity.Ally) TEXTURE_PINK else TEXTURE

    override fun render(mob: SpiderlingEntity, f: Float, tickDelta: Float, matrixStack: PoseStack, vertexConsumerProvider: MultiBufferSource, light: Int) {
        super.render(mob, f, tickDelta, matrixStack, vertexConsumerProvider, light)

        if (mob.anchorPosition == null) return

        val yOffset = 0.65

        val vertexConsumer = vertexConsumerProvider.getBuffer(RenderType.entityCutout(getTextureLocation(mob)))
        val anchor = mob.anchorPosition!!
        val x = anchor.x + 0.5 - Easing.lerp(mob.xo.toFloat(), mob.x.toFloat(), tickDelta)
        val y = anchor.y.toDouble() - Easing.lerp(mob.yo.toFloat(), mob.y.toFloat(), tickDelta) - yOffset
        val z = anchor.z + 0.5 - Easing.lerp(mob.zo.toFloat(), mob.z.toFloat(), tickDelta)

        matrixStack.entry {
            val distance = sqrt(x * x + y * y + z * z).toFloat()
            val roll = atan2(y, sqrt(x * x + z * z)).toFloat()
            val yaw = atan2(x, z).toFloat() - Mth.HALF_PI
            translate(0.0, yOffset, 0.0)
            matrixStack.mulPose(Axis.YP.rotation(yaw))
            matrixStack.mulPose(Axis.ZP.rotation(roll))
            matrixStack.mulPose(Axis.XP.rotationDegrees(90f))
            scale(distance, 1f, 1f)

            vertexConsumer.drawBillboard(
                matrixStack,
                light,
                Vector2f(0.0f, -1 / 32f),
                Vector2f(1.0f, 1 / 32f),
                Vector2f(16 / 32f, 0f),
                Vector2f(1f, 1 / 32f),
                255,
                255,
                255,
                255,
                true
            )
        }
    }

    override fun getFlipDegrees(entity: SpiderlingEntity) = if (entity.anchorPosition != null) 15f else 180f
}

class SpiderlingModel(
    private val root: ModelPart
) : EntityModel<SpiderlingEntity>(RenderType::entityCutout) {

    private val main = root.getChild("")
    private val rightLegs = main.getChild("rightLegs")
    private val leftLegs = main.getChild("leftLegs")
    private val neck = main.getChild("neck")
    private val head = neck.getChild("head")

    private val legs: Array<ModelPart> =
        Array(8) { i ->
            val child = if (i < 4) {
                "leftLeg"
            } else {
                "rightLeg"
            }
            main.getChild("${child}s").getChild("$child${i % 4}")
        }

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
        entity: SpiderlingEntity,
        limbAngle: Float,
        limbDistance: Float,
        animationProgress: Float,
        headYaw: Float,
        headPitch: Float
    ) {
        root.xRot = 0f
        main.xRot = entity.anchored.update(animationProgress) * 90f.radians()

        leftLegs.zRot = (25f).radians()
        rightLegs.zRot = (-25f).radians()
        val progress = limbAngle * 1.0f

        walkSpiderLegs(legs, progress, limbDistance)

        neck.yRot = 0f
        head.yRot = 0f
        head.xRot = 0f

        neck.xRot = headPitch.radians() - entity.anchored.value * 75f.radians()

        if (entity.anchorPosition == null) {
            head.xRot = headPitch.radians()
            neck.xRot = 0f
        }

        head.yRot = headYaw.radians()
    }
}

fun getSpiderlingTexturedModelData(): LayerDefinition {
    val modelData = MeshDefinition()
    val modelPartData = modelData.root
    val main = modelPartData.addOrReplaceChild(
        "", CubeListBuilder.create().texOffs(0, 0).addBox(-2.5f, -2.0f, 1.0f, 5.0f, 4.0f, 5.0f, CubeDeformation(0.0f))
            .texOffs(0, 18).addBox(-1.5f, 0.0f, -2.0f, 3.0f, 2.0f, 3.0f, CubeDeformation(0.0f)), PartPose.offset(0.0f, 19.0f, 0.0f)
    )

    val leftLegs = main.addOrReplaceChild("leftLegs", CubeListBuilder.create(), PartPose.offset(0.5f, 2.25f, -0.5f))
    leftLegs.addOrReplaceChild("leftLeg0", CubeListBuilder.create().texOffs(0, 24).addBox(-1.5f, -0.5f, -0.5f, 8.0f, 1.0f, 1.0f, CubeDeformation(0.0f)), PartPose.offset(1.0f, -0.75f, -2.0f))
    leftLegs.addOrReplaceChild("leftLeg1", CubeListBuilder.create().texOffs(0, 28).addBox(-1.5f, -0.5f, -0.5f, 8.0f, 1.0f, 1.0f, CubeDeformation(0.0f)), PartPose.offset(1.0f, -0.75f, -0.75f))
    leftLegs.addOrReplaceChild("leftLeg2", CubeListBuilder.create().texOffs(0, 24).addBox(-1.5f, -0.5f, -0.5f, 8.0f, 1.0f, 1.0f, CubeDeformation(0.0f)), PartPose.offset(1.0f, -0.75f, 0.5f))
    leftLegs.addOrReplaceChild("leftLeg3", CubeListBuilder.create().texOffs(0, 28).addBox(-1.5f, -0.5f, -0.5f, 8.0f, 1.0f, 1.0f, CubeDeformation(0.0f)), PartPose.offset(1.0f, -0.75f, 1.75f))

    val rightLegs = main.addOrReplaceChild("rightLegs", CubeListBuilder.create(), PartPose.offset(-0.5f, 2.25f, -0.5f))
    rightLegs.addOrReplaceChild("rightLeg0", CubeListBuilder.create().texOffs(0, 30).mirror().addBox(-6.5f, -0.5f, -0.5f, 8.0f, 1.0f, 1.0f, CubeDeformation(0.0f)).mirror(false), PartPose.offset(-1.0f, -0.75f, -2.0f))
    rightLegs.addOrReplaceChild("rightLeg1", CubeListBuilder.create().texOffs(0, 26).mirror().addBox(-6.5f, -0.5f, -0.5f, 8.0f, 1.0f, 1.0f, CubeDeformation(0.0f)).mirror(false), PartPose.offset(-1.0f, -0.75f, -0.75f))
    rightLegs.addOrReplaceChild("rightLeg2", CubeListBuilder.create().texOffs(0, 30).mirror().addBox(-6.5f, -0.5f, -0.5f, 8.0f, 1.0f, 1.0f, CubeDeformation(0.0f)).mirror(false), PartPose.offset(-1.0f, -0.75f, 0.5f))
    rightLegs.addOrReplaceChild("rightLeg3", CubeListBuilder.create().texOffs(0, 26).mirror().addBox(-6.5f, -0.5f, -0.5f, 8.0f, 1.0f, 1.0f, CubeDeformation(0.0f)).mirror(false), PartPose.offset(-1.0f, -0.75f, 1.75f))

    val neck = main.addOrReplaceChild("neck", CubeListBuilder.create(), PartPose.offset(0.0f, 0.5f, -2.0f))
    neck.addOrReplaceChild(
        "head", CubeListBuilder.create().texOffs(0, 9).addBox(-2.0f, -1.5f, -4.0f, 4.0f, 3.0f, 4.0f, CubeDeformation(0.0f))
            .texOffs(9, 20).addBox(-1.5f, 1.5f, -4.0f, 3.0f, 1.0f, 0.0f, CubeDeformation(0.0f)), PartPose.offset(0.0f, 0.0f, 0.0f)
    )
    return LayerDefinition.create(modelData, 32, 32)
}