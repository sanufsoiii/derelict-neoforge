package dev.mim1q.derelict.util.extensions

import net.minecraft.world.entity.Entity
import net.minecraft.util.Mth
import net.minecraft.world.phys.Vec3

fun Float.radians() = this * Mth.DEG_TO_RAD
fun Float.degrees() = this * Mth.RAD_TO_DEG
fun Double.radians() = this * Mth.DEG_TO_RAD.toDouble()
fun Double.degrees() = this * Mth.RAD_TO_DEG.toDouble()

fun Entity.getLocallyOffsetPos(offset: Vec3) = position().add(offset.yRot(-visualRotationYInDegrees.radians()))