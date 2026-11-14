package dev.mim1q.derelict.util.extensions

import net.minecraft.client.model.geom.ModelPart
import net.minecraft.util.Mth

fun ModelPart.setPartialAngles(
    pitch: Float = this.xRot,
    roll: Float = this.zRot,
    yaw: Float = this.yRot,
    delta: Float
) {
    setPartialPitch(pitch, delta)
    setPartialRoll(roll, delta)
    setPartialYaw(yaw, delta)
}

fun ModelPart.setPartialAnglesDegrees(
    pitch: Float = this.xRot,
    roll: Float = this.zRot,
    yaw: Float = this.yRot,
    delta: Float
) {
    setPartialAngles(pitch.radians(), roll.radians(), yaw.radians(), delta)
}

fun ModelPart.setPartialPitch(
    pitch: Float = this.xRot,
    delta: Float
) {
    this.xRot = Mth.lerp(delta, this.xRot, pitch)
}

fun ModelPart.setPartialRoll(
    roll: Float = this.zRot,
    delta: Float
) {
    this.zRot = Mth.lerp(delta, this.zRot, roll)
}

fun ModelPart.setPartialYaw(
    yaw: Float = this.yRot,
    delta: Float
) {
    this.yRot = Mth.lerp(delta, this.yRot, yaw)
}