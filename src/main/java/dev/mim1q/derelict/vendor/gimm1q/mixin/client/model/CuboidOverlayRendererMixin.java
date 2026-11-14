package dev.mim1q.derelict.vendor.gimm1q.mixin.client.model;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.mim1q.derelict.vendor.gimm1q.client.render.overlay.ModelOverlayVertexConsumer;
import dev.mim1q.derelict.vendor.gimm1q.client.render.overlay.OverlayUvMapper;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.core.Direction;

import static net.minecraft.util.Mth.EPSILON;

@ApiStatus.Internal
@Mixin(ModelPart.Cube.class)
public class CuboidOverlayRendererMixin {
    // @formatter:off
    @Unique
    private static final ImmutableMap<Vector3f, Direction> gimm1q$DIRECTION_MAP = ImmutableMap.of(
        Direction.DOWN .step(), Direction.DOWN,
        Direction.UP   .step(), Direction.UP,
        Direction.WEST .step(), Direction.WEST,
        Direction.NORTH.step(), Direction.NORTH,
        Direction.EAST .step(), Direction.EAST,
        Direction.SOUTH.step(), Direction.SOUTH
    );

    @Unique
    private static final ImmutableMap<Direction, float[][]> gimm1q$OFFSETS = ImmutableMap.of(
        Direction.DOWN,  new float[][]{{ 1, -1,  1}, {-1, -1,  1}, {-1, -1, -1}, { 1, -1, -1}},
        Direction.UP,    new float[][]{{ 1,  1, -1}, {-1,  1, -1}, {-1,  1,  1}, { 1,  1,  1}},
        Direction.WEST,  new float[][]{{-1, -1, -1}, {-1, -1,  1}, {-1,  1,  1}, {-1,  1, -1}},
        Direction.NORTH, new float[][]{{ 1, -1, -1}, {-1, -1, -1}, {-1,  1, -1}, { 1,  1, -1}},
        Direction.EAST,  new float[][]{{ 1, -1,  1}, { 1, -1, -1}, { 1,  1, -1}, { 1,  1,  1}},
        Direction.SOUTH, new float[][]{{-1, -1,  1}, { 1, -1,  1}, { 1,  1,  1}, {-1,  1,  1}}
    );
    // @formatter:on

    @Unique
    private static final float[][] gimm1q$NO_OFFSETS = new float[][]{{0, 0, 0}, {0, 0, 0}, {0, 0, 0}, {0, 0, 0}};

    @Shadow
    @Final
    private ModelPart.Polygon[] polygons;
    @Unique
    private boolean gimm1q$isPlane = false;
    @Unique
    private float gimm1q$textureWidth = 1.0f;
    @Unique
    private float gimm1q$textureHeight = 1.0f;
    @Unique
    private boolean gimm1q$isMirrored = false;

    @Inject(
        method = "<init>",
        at = @At("RETURN")
    )
    private void gimm1q$init(
        int u, int v,
        float x, float y, float z,
        float sizeX, float sizeY, float sizeZ,
        float extraX, float extraY, float extraZ,
        boolean mirror,
        float textureWidth, float textureHeight,
        Set<Direction> set,
        CallbackInfo ci
    ) {
        if (sizeX <= EPSILON || sizeY <= EPSILON || sizeZ <= EPSILON || set.isEmpty()) {
            gimm1q$isPlane = true;
        }
        gimm1q$textureWidth = textureWidth;
        gimm1q$textureHeight = textureHeight;
        gimm1q$isMirrored = mirror;
    }

    @Inject(
        method = "compile",
        at = @At("HEAD"),
        cancellable = true
    )
    private void gimm1q$render(
        PoseStack.Pose entry, VertexConsumer vertexConsumer, int light, int overlay, int color, CallbackInfo ci
    ) {
        if (vertexConsumer instanceof ModelOverlayVertexConsumer modelOverlayVertexConsumer) {
            if (gimm1q$isPlane && modelOverlayVertexConsumer.shouldSkipPlanes()) {
                ci.cancel();
                return;
            }

            final var posMatrix = entry.pose();
            final var normalMatrix = entry.normal();

            final var normalMultiplier = modelOverlayVertexConsumer.isInverted() ? -1.0f : 1.0f;
            final var animationProgress = Minecraft.getInstance().gui.getGuiTicks() + Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);

            for (final var quad : this.polygons) {
                final var quadDir = gimm1q$isMirrored ? quad.normal.mul(-1f, 1f, 1f, new Vector3f()) : quad.normal;

                var xOffsetScalar = modelOverlayVertexConsumer.getOffset();
                var yOffsetScalar = modelOverlayVertexConsumer.getOffset();
                var zOffsetScalar = modelOverlayVertexConsumer.getOffset();

                final var normalVector = normalMatrix.transform(new Vector3f(quad.normal));
                var direction = gimm1q$DIRECTION_MAP.get(quadDir);

                var offset = gimm1q$NO_OFFSETS;
                if (direction != null) {
                    offset = gimm1q$OFFSETS.getOrDefault(direction, gimm1q$NO_OFFSETS);

                    if (gimm1q$isMirrored) {
                        xOffsetScalar = -xOffsetScalar;
                        if (direction.getAxis().isVertical()) {
                            zOffsetScalar = -zOffsetScalar;
                        } else {
                            yOffsetScalar = -yOffsetScalar;
                        }
                    }
                }

                for (var i = 0; i < 4; ++i) {
                    final var index = modelOverlayVertexConsumer.isInverted() ? 3 - i : i;
                    final var vertex = quad.vertices[index];

                    @SuppressWarnings("DataFlowIssue") final var vertexOffsets = offset[index];

                    final var x = (vertex.pos.x() + vertexOffsets[0] * xOffsetScalar) / 16.0f;
                    final var y = (vertex.pos.y() + vertexOffsets[1] * yOffsetScalar) / 16.0f;
                    final var z = (vertex.pos.z() + vertexOffsets[2] * zOffsetScalar) / 16.0f;
                    final var posVector = posMatrix.transform(new Vector4f(x, y, z, 1.0f));

                    final var uv = modelOverlayVertexConsumer.applyUvMapper(
                        vertex.u * gimm1q$textureWidth / modelOverlayVertexConsumer.getTextureSize(),
                        vertex.v * gimm1q$textureHeight / modelOverlayVertexConsumer.getTextureSize(),
                        new OverlayUvMapper.OverlayUvMapperQuadContext(
                            animationProgress,
                            index,
                            gimm1q$isMirrored
                        )
                    );

                    vertexConsumer.addVertex(
                        posVector.x(), posVector.y(), posVector.z(),
                        color,
                        uv[0], uv[1],
                        overlay,
                        light,
                        normalMultiplier * normalVector.x,
                        normalMultiplier * normalVector.y,
                        normalMultiplier * normalVector.z
                    );
                }
            }
            ci.cancel();
        }
    }
}
