package dev.mim1q.derelict.vendor.gimm1q.particle.electric;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Math;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class ElectricityParticle extends TextureSheetParticle {
    private final SpriteSet spriteProvider;
    private final Vec3 direction;
    private final float pitch;
    private final float yaw;
    private final float roll;
    private final int length;
    private final int color;
    private final float size;
    private final boolean isMainBranch;

    private ElectricityParticle(
        SpriteSet spriteProvider,
        ClientLevel clientWorld,
        double x,
        double y,
        double z,
        double velocityX,
        double velocityY,
        double velocityZ,
        Vec3 direction,
        int length,
        int color,
        float size,
        boolean isMainBranch
    ) {
        super(clientWorld, x, y, z);
        this.spriteProvider = spriteProvider;
        this.direction = direction.normalize();
        this.length = length;
        this.color = color;
        this.lifetime = 5 + length;
        this.size = size;
        this.isMainBranch = isMainBranch;

        this.yaw = (float) Math.asin(-this.direction.y);
        // Math checks out but IDEA throws a naming warning ;P
        // noinspection SuspiciousNameCombination
        this.pitch = (float) Math.atan2(this.direction.x, this.direction.z);
        this.roll = clientWorld.random.nextFloat() * 2 * Mth.PI;

        this.setParticleSpeed(velocityX, velocityY, velocityZ);
        this.pickSprite(spriteProvider);

        if (length > 0) {
            for (int i = 0; i < 3; i++) {
                if (level.random.nextFloat() < 0.7f) this.addSideBranch();
            }
            this.addNextMainBranch();
        }
    }

    @Override
    public void tick() {
        this.setPos(this.x + this.xd, this.y + this.yd, this.z + this.zd);
        if (age % 2 == 0) {
            this.pickSprite(spriteProvider);
        }

        if (this.age++ >= this.lifetime) {
            this.remove();
        }
    }

    private void addSideBranch() {
        final var delta = level.random.nextFloat();
        final var newPos = new Vec3(this.x, this.y, this.z).add(this.direction.scale(delta * size));
        final var randomDirection = this.direction.offsetRandom(level.random, 2.0f);
        final var randomLength = Math.min(this.length - 1, level.random.nextInt(3));

        final var particle = new ElectricityParticleEffect(randomDirection, randomLength, this.color, this.size * 0.5f, false);

        level.addParticle(
            particle,
            newPos.x,
            newPos.y,
            newPos.z,
            xd,
            yd,
            zd
        );
    }

    private void addNextMainBranch() {
        final var newDirection = this.direction.offsetRandom(level.getRandom(), isMainBranch ? 0.1f : 2.0f);

        final var newPos = new Vec3(this.x, this.y, this.z).add(this.direction.scale(size));


        level.addParticle(
            new ElectricityParticleEffect(newDirection, this.length - 1, this.color, this.size, this.isMainBranch),
            newPos.x,
            newPos.y,
            newPos.z,
            xd,
            yd,
            zd
        );
    }

    @Override
    public void render(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
        final var lifetimeDelta = (this.age + tickDelta) / this.lifetime;
        var alpha = 255;

        if (lifetimeDelta < 0.2) {
            alpha = (int) (lifetimeDelta / 0.2 * 255);
        } else if (lifetimeDelta > 0.8) {
            alpha = (int) ((1 - lifetimeDelta) / 0.2 * 255);
        }

        alpha = Mth.clamp(alpha, 0, 255);

        final var vector3fs = getParticleQuad(camera, tickDelta);

        float k = this.getU0();
        float l = this.getU1();
        float m = this.getV0();
        float n = this.getV1();

        final var u = new float[]{l, l, k, k};
        final var v = new float[]{n, m, m, n};

        final int o = 0xF000F0;

        final var r = this.color >> 16 & 0xFF;
        final var g = this.color >> 8 & 0xFF;
        final var b = this.color & 0xFF;

        for (int i = 0; i <= 3; ++i) {
            vertexConsumer.addVertex(vector3fs[i].x(), vector3fs[i].y(), vector3fs[i].z()).setUv(u[i], v[i]).setColor(r, g, b, alpha).setLight(o);
        }
        for (int i = 3; i >= 0; --i) {
            vertexConsumer.addVertex(vector3fs[i].x(), vector3fs[i].y(), vector3fs[i].z()).setUv(u[i], v[i]).setColor(r, g, b, alpha).setLight(o);
        }

    }

    private Vector3f[] getParticleQuad(Camera camera, float tickDelta) {
        final var px = (float) (Mth.lerp(tickDelta, xo, x) - camera.getPosition().x);
        final var py = (float) (Mth.lerp(tickDelta, yo, y) - camera.getPosition().y);
        final var pz = (float) (Mth.lerp(tickDelta, zo, z) - camera.getPosition().z);

        final var quaternionf = new Quaternionf();
        quaternionf.rotateY(pitch);
        quaternionf.rotateX(yaw);
        quaternionf.rotateZ(roll);

        final var quad = new Vector3f[]{
            new Vector3f(-0.5f, 0f, 0.0f),
            new Vector3f(-0.5f, 0f, 1.0f),
            new Vector3f(0.5f, 0f, 1.0f),
            new Vector3f(0.5f, 0f, 0.0f)
        };

        for (int j = 0; j < 4; ++j) {
            final var vector3f = quad[j];
            vector3f.rotate(quaternionf);
            vector3f.mul(size);
            vector3f.add(px, py, pz);
        }

        return quad;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public record Factory(
        SpriteSet spriteProvider
    ) implements ParticleProvider<ElectricityParticleEffect> {
        @Override
        public Particle createParticle(
            ElectricityParticleEffect parameters,
            ClientLevel world,
            double x, double y, double z,
            double velocityX, double velocityY, double velocityZ
        ) {
            return new ElectricityParticle(
                spriteProvider,
                world,
                x,
                y,
                z,
                velocityX,
                velocityY,
                velocityZ,
                parameters.direction(),
                parameters.length(),
                parameters.color(),
                parameters.size(),
                parameters.isMainBranch()
            );
        }
    }
}
