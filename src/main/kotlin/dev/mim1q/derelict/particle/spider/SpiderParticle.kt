package dev.mim1q.derelict.particle.spider

import dev.mim1q.derelict.util.extensions.radians
import dev.mim1q.derelict.util.render.entry
import net.minecraft.client.particle.Particle
import net.minecraft.client.particle.ParticleProvider
import net.minecraft.client.particle.ParticleRenderType
import net.minecraft.client.particle.SpriteSet
import net.minecraft.client.particle.TextureSheetParticle
import net.minecraft.client.Camera
import com.mojang.blaze3d.vertex.VertexConsumer
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.multiplayer.ClientLevel
import net.minecraft.core.Direction
import net.minecraft.util.Mth
import com.mojang.math.Axis
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import kotlin.math.abs

class SpiderParticle(
    world: ClientLevel,
    x: Double,
    y: Double,
    z: Double,
    vx: Double,
    vz: Double,
    private val direction: Direction
) : TextureSheetParticle(world, x, y, z, 0.0, 0.0, 0.0) {
    companion object {
        val matrices = PoseStack()
    }

    init {
        lifetime = 40 + random.nextInt(40)
        alpha = 0.0F
        quadSize = random.nextFloat() * 0.05F + 0.1F
        friction = Mth.sqrt((vx * vx + vz * vz).toFloat())
    }

    private var rotation = Mth.atan2(vz, vx).toFloat()

    private val vx
        get() = Mth.sin(Mth.HALF_PI - rotation) * friction.toDouble()
    private val vz
        get() = Mth.cos(Mth.HALF_PI - rotation) * friction.toDouble()

    override fun getRenderType(): ParticleRenderType = ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT

    override fun render(vertexConsumer: VertexConsumer, camera: Camera, tickDelta: Float) {
        matrices.entry {
            val x = Mth.lerp(tickDelta.toDouble(), xo, x)
            val y = Mth.lerp(tickDelta.toDouble(), yo, y)
            val z = Mth.lerp(tickDelta.toDouble(), zo, z)

            val light = getLightColor(0f)
            translate(x - camera.position.x, y - camera.position.y, z - camera.position.z)
            val scale = getQuadSize(tickDelta) / 8f
            scale(scale, scale, scale)
            rotateMatrices(matrices, direction, Mth.lerp(tickDelta, oRoll, roll))
            translate(0.0, 0.0, -0.01)

            matrices.entry {
                translate(-1.5, 0.0, -0.2)
                repeat(3) {
                    drawBillboard(
                        vertexConsumer,
                        matrices,
                        light,
                        3f,
                        8f
                    )
                    translate(0.0, 0.0, -0.3)
                }
            }

            val time = (age + tickDelta) * 0.67f

            val angle1 = Mth.abs(Mth.sin(time))
            val angle2 = Mth.abs(Mth.sin(time + 90f.radians()))
            val angle3 = Mth.abs(Mth.sin(time + 45f.radians()))
            val angle4 = Mth.abs(Mth.sin(time + 135f.radians()))

            drawLeg(matrices, vertexConsumer, light, -1.0, 5.0, 60f - angle1 * 50f)
            drawLeg(matrices, vertexConsumer, light, -0.5, 4.5, 100f - angle2 * 50f)
            drawLeg(matrices, vertexConsumer, light, -0.5, 3.5, 140f - angle3 * 40f)
            drawLeg(matrices, vertexConsumer, light, -1.25, 2.5, 180f - angle4 * 50f)

            drawLeg(matrices, vertexConsumer, light, 1.0, 5.0, -60f + angle4 * 50f)
            drawLeg(matrices, vertexConsumer, light, 0.5, 4.5, -100f + angle1 * 50f)
            drawLeg(matrices, vertexConsumer, light, 0.5, 3.5, -140f + angle2 * 40f)
            drawLeg(matrices, vertexConsumer, light, 1.25, 2.5, -180f + angle3 * 50f)

        }
    }

    private fun drawLeg(
        matrices: PoseStack,
        vertexConsumer: VertexConsumer,
        light: Int,
        xOffset: Double,
        zOffset: Double,
        angle: Float
    ) {
        matrices.entry {
            translate(xOffset, zOffset, 0.005)
            mulPose(Axis.ZP.rotationDegrees(angle))
            translate(-0.5, 0.0, 0.0)
            drawBillboard(
                vertexConsumer,
                matrices,
                light,
                1f,
                6f,
                4f / 16f,
            )
        }
    }

    private fun rotateMatrices(matrices: PoseStack, direction: Direction, angle: Float) =
        when (direction) {
            Direction.UP -> {
                matrices.mulPose(Axis.XN.rotationDegrees(90.0F))
                matrices.mulPose(Axis.ZP.rotation(angle + Mth.HALF_PI))
            }

            Direction.DOWN -> {
                matrices.mulPose(Axis.XP.rotationDegrees(90.0F))
                matrices.mulPose(Axis.ZP.rotation(angle - Mth.HALF_PI))
            }

            else -> {
                matrices.mulPose(Axis.YN.rotationDegrees(direction.toYRot()))
                matrices.mulPose(Axis.ZP.rotation(angle))
            }
        }


    override fun tick() {
        rotation += (random.nextFloat() - 0.5F) * if (random.nextFloat() < 0.1f) 1.0f else 0.2f
        setAngle()
        setParticleSpeed(vx, 0.0, vz)
        setSize(0.01F, 0.01F)

        val offset = Vec3(xd, yd, zd).scale(1.5).add(Vec3.atLowerCornerOf(direction.normal).scale(0.5))
        val xVel = abs(x - xo) < Mth.EPSILON
        val yVel = abs(y - yo) < Mth.EPSILON
        val zVel = abs(z - zo) < Mth.EPSILON

        if (
            (age > 2 && (xVel xor yVel xor zVel xor true))
            || level.getBlockCollisions(null, boundingBox.move(offset.x, offset.y, offset.z)).none()
        ) {
            remove()
            return
        }

        super.tick()

        if (age <= 4) {
            alpha = (age / 4.0F)
        }
        val remaining = lifetime - age
        if (remaining <= 5) {
            alpha = (remaining / 5.0F)
        }
    }

    override fun getQuadSize(tickDelta: Float): Float {
        val multiplier = when {
            age <= 4 -> (age + tickDelta) / 4.0F
            lifetime - age - tickDelta <= 5 -> (lifetime - age - tickDelta) / 5.0F
            else -> 1f
        }
        return quadSize * multiplier
    }

    override fun setParticleSpeed(x: Double, y: Double, z: Double) {
        when (direction) {
            Direction.DOWN, Direction.UP -> super.setParticleSpeed(x, 0.0, z)
            Direction.NORTH, Direction.SOUTH -> super.setParticleSpeed(x, z, 0.0)
            Direction.EAST, Direction.WEST -> super.setParticleSpeed(0.0, x, z)
        }
    }

    private fun setAngle() {
        oRoll = roll
        roll = when (direction) {
            Direction.DOWN -> rotation
            Direction.UP -> Mth.PI - rotation
            Direction.NORTH -> Mth.HALF_PI - rotation
            Direction.SOUTH -> rotation - Mth.HALF_PI
            Direction.EAST -> rotation
            Direction.WEST -> -rotation
        }
    }

    class Factory(private val spriteProvider: SpriteSet) : ParticleProvider<SpiderParticleEffect> {
        override fun createParticle(
            parameters: SpiderParticleEffect,
            world: ClientLevel,
            x: Double,
            y: Double,
            z: Double,
            velocityX: Double,
            velocityY: Double,
            velocityZ: Double
        ): Particle {
            val particle = SpiderParticle(world, x, y, z, velocityX, velocityZ, parameters.direction)
            particle.pickSprite(spriteProvider)
            return particle
        }
    }

    private fun drawBillboard(
        vertexConsumer: VertexConsumer,
        matrices: PoseStack,
        light: Int,
        width: Float = 1f,
        height: Float = 1f,
        u: Float = 0f,
    ) {
        val vertices = arrayOf(
            Vector3f(0f, 0f, 0.0f),
            Vector3f(0f, height, 0.0f),
            Vector3f(width, height, 0.0f),
            Vector3f(width, 0f, 0.0f)
        )

        val uOffset = (u1 - u0) * u
        val maxU = Mth.lerp(width / 16f, this.u0, u1) + uOffset
        val maxV = Mth.lerp(height / 16f, this.v0, v1)
        val minU = u0 + uOffset
        val minV = v0

        val posMatrix = matrices.last().pose()

        vertexConsumer
            .addVertex(posMatrix, vertices[0].x, vertices[0].y, vertices[0].z)
            .setUv(maxU, maxV)
            .setColor(this.rCol, this.gCol, this.bCol, this.alpha)
            .setLight(light)
        vertexConsumer
            .addVertex(posMatrix, vertices[1].x, vertices[1].y, vertices[1].z)
            .setUv(maxU, minV)
            .setColor(this.rCol, this.gCol, this.bCol, this.alpha)
            .setLight(light)
        vertexConsumer
            .addVertex(posMatrix, vertices[2].x, vertices[2].y, vertices[2].z)
            .setUv(minU, minV)
            .setColor(this.rCol, this.gCol, this.bCol, this.alpha)
            .setLight(light)
        vertexConsumer
            .addVertex(posMatrix, vertices[3].x, vertices[3].y, vertices[3].z)
            .setUv(minU, maxV)
            .setColor(this.rCol, this.gCol, this.bCol, this.alpha)
            .setLight(light)
    }
}