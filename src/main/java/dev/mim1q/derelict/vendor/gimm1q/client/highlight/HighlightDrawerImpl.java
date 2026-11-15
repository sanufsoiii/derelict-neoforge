package dev.mim1q.derelict.vendor.gimm1q.client.highlight;


import dev.mim1q.derelict.vendor.gimm1q.Gimm1q;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.ApiStatus;

import static net.minecraft.util.Mth.sign;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import com.mojang.blaze3d.vertex.VertexConsumer;

@ApiStatus.Internal

public class HighlightDrawerImpl implements HighlightDrawer {
    public static final HighlightDrawerImpl INSTANCE = new HighlightDrawerImpl();
    private static final ResourceLocation TEXTURE = Gimm1q.id("textures/block/white.png");

    private PoseStack matrixStack = null;
    private MultiBufferSource consumers = null;
    private Camera camera = null;

    private HighlightDrawerImpl() {
    }

    public static void setRenderContext(PoseStack matrixStack, MultiBufferSource consumers, Camera camera) {
        INSTANCE.matrixStack = matrixStack;
        INSTANCE.consumers = consumers;
        INSTANCE.camera = camera;
    }

    @Override
    public void drawHighlight(AABB box, int colorArgb, int outlineArgb) {
        if (camera == null) return;

        var matrices = matrixStack;
        var vertexConsumers = consumers;
        if (matrices == null || vertexConsumers == null) return;

        var vertices = new float[][]{
            new float[]{(float) box.minX, (float) box.minY, (float) box.minZ}, // 000 [0]
            new float[]{(float) box.minX, (float) box.minY, (float) box.maxZ}, // 001 [1]
            new float[]{(float) box.minX, (float) box.maxY, (float) box.minZ}, // 010 [2]
            new float[]{(float) box.minX, (float) box.maxY, (float) box.maxZ}, // 011 [3]
            new float[]{(float) box.maxX, (float) box.minY, (float) box.minZ}, // 100 [4]
            new float[]{(float) box.maxX, (float) box.minY, (float) box.maxZ}, // 101 [5]
            new float[]{(float) box.maxX, (float) box.maxY, (float) box.minZ}, // 110 [6]
            new float[]{(float) box.maxX, (float) box.maxY, (float) box.maxZ}, // 111 [7]
        };

        matrices.pushPose();
        {
            matrices.translate(-camera.getPosition().x, -camera.getPosition().y, -camera.getPosition().z);
            drawFace(matrices, vertexConsumers, new float[][]{vertices[3], vertices[2], vertices[0], vertices[1]}, colorArgb, outlineArgb);
            drawFace(matrices, vertexConsumers, new float[][]{vertices[6], vertices[7], vertices[5], vertices[4]}, colorArgb, outlineArgb);
            drawFace(matrices, vertexConsumers, new float[][]{vertices[2], vertices[6], vertices[4], vertices[0]}, colorArgb, outlineArgb);
            drawFace(matrices, vertexConsumers, new float[][]{vertices[7], vertices[3], vertices[1], vertices[5]}, colorArgb, outlineArgb);
            drawFace(matrices, vertexConsumers, new float[][]{vertices[4], vertices[5], vertices[1], vertices[0]}, colorArgb, outlineArgb);
            drawFace(matrices, vertexConsumers, new float[][]{vertices[3], vertices[7], vertices[6], vertices[2]}, colorArgb, outlineArgb);
        }
        matrices.popPose();
    }

    private void drawVertex(PoseStack.Pose entry, VertexConsumer buffer, float[] vertex, int argb, float u, float v) {
        buffer
            .addVertex(entry, vertex[0], vertex[1], vertex[2])
            .setColor(argb)
            .setUv(u, v)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(0x0000F0)
            .setNormal(entry, 0.0F, 1.0F, 0.0F);
    }

    private void drawFace(PoseStack matrices, MultiBufferSource vertexConsumerProvider, float[][] vertices, int argb, int outlineArgb) {
        var entry = matrices.last();
        var buffer = vertexConsumerProvider.getBuffer(RenderType.entityTranslucent(TEXTURE));

        var drawOutline = (outlineArgb & 0xFF000000) != 0;
        var innerVertices = vertices;

        if (drawOutline) {
            innerVertices = getInsetFace(vertices, 1 / 16f);
            var outlineBuffer = vertexConsumerProvider.getBuffer(RenderType.entityTranslucent(TEXTURE));
            drawInsetFace(entry, outlineBuffer, vertices, innerVertices, outlineArgb);
        }

        var drawInner = (argb & 0xFF000000) != 0;
        if (!drawInner) return;

        for (var i = 0; i <= 3; ++i) {
            var u = i & 1;
            var v = (i >> 1) & 1;
            drawVertex(entry, buffer, innerVertices[i], argb, u, v);
        }
    }

    private void drawInsetFace(PoseStack.Pose entry, VertexConsumer buffer, float[][] vertices, float[][] insetVertices, int argb) {
        for (var i = 0; i <= 3; ++i) {
            drawVertex(entry, buffer, vertices[i], argb, 0, 0);
            drawVertex(entry, buffer, insetVertices[i], argb, 1, 0);
            drawVertex(entry, buffer, insetVertices[(i + 1) % 4], argb, 1, 1);
            drawVertex(entry, buffer, vertices[(i + 1) % 4], argb, 0, 1);
        }
    }

    private float[][] getInsetFace(float[][] vertices, float inset) {
        float dx = sign(vertices[2][0] - vertices[0][0]);
        float dy = sign(vertices[2][1] - vertices[0][1]);
        float dz = sign(vertices[2][2] - vertices[0][2]);

        if (dy == 0) {
            if (dz > 0) {
                return new float[][]{
                    new float[]{vertices[0][0] + dx * inset, vertices[0][1], vertices[0][2] + dz * inset},
                    new float[]{vertices[1][0] + dx * inset, vertices[1][1], vertices[1][2] - dz * inset},
                    new float[]{vertices[2][0] - dx * inset, vertices[2][1], vertices[2][2] - dz * inset},
                    new float[]{vertices[3][0] - dx * inset, vertices[3][1], vertices[3][2] + dz * inset},
                };
            }
            return new float[][]{
                new float[]{vertices[0][0] + dx * inset, vertices[0][1], vertices[0][2] + dz * inset},
                new float[]{vertices[1][0] - dx * inset, vertices[1][1], vertices[1][2] + dz * inset},
                new float[]{vertices[2][0] - dx * inset, vertices[2][1], vertices[2][2] - dz * inset},
                new float[]{vertices[3][0] + dx * inset, vertices[3][1], vertices[3][2] - dz * inset},
            };
        }

        return new float[][]{
            new float[]{vertices[0][0] + dx * inset, vertices[0][1] + dy * inset, vertices[0][2] + dz * inset},
            new float[]{vertices[1][0] - dx * inset, vertices[1][1] + dy * inset, vertices[1][2] - dz * inset},
            new float[]{vertices[2][0] - dx * inset, vertices[2][1] - dy * inset, vertices[2][2] - dz * inset},
            new float[]{vertices[3][0] + dx * inset, vertices[3][1] - dy * inset, vertices[3][2] + dz * inset},
        };
    }
}
