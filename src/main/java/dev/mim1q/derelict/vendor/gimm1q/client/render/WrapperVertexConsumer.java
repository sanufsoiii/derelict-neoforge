package dev.mim1q.derelict.vendor.gimm1q.client.render;

import com.mojang.blaze3d.vertex.VertexConsumer;

/**
 * A wrapper for {@link VertexConsumer} that can be extended to modify the wrapped consumer's functionality,
 */
public class WrapperVertexConsumer implements VertexConsumer {
    protected VertexConsumer wrapped;

    protected WrapperVertexConsumer(VertexConsumer wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public VertexConsumer addVertex(float x, float y, float z) {
        return wrapped.addVertex(x, y, z);
    }

    @Override
    public VertexConsumer setColor(int red, int green, int blue, int alpha) {
        return wrapped.setColor(red, green, blue, alpha);
    }

    @Override
    public VertexConsumer setUv(float u, float v) {
        return wrapped.setUv(u, v);
    }

    @Override
    public VertexConsumer setUv1(int u, int v) {
        return wrapped.setUv1(u, v);
    }

    @Override
    public VertexConsumer setUv2(int u, int v) {
        return wrapped.setUv2(u, v);
    }

    @Override
    public VertexConsumer setNormal(float x, float y, float z) {
        return wrapped.setNormal(x, y, z);
    }
}
