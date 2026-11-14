package dev.mim1q.derelict.util

import net.minecraft.util.Mth

object Easing {
    fun easeInOutQuad(a: Float, b: Float, delta: Float): Float {
        val d =
            if (delta < 0.5f) 2.0f * delta * delta else 1.0f - (-2.0f * delta + 2.0f) * (-2.0f * delta + 2.0f) * 0.5f
        return Mth.lerp(d, a, b)
    }

    fun smoothStep(value: Float, min: Float = 0F, max: Float = 1F): Float {
        val x = Mth.clamp((value - min) / (max - min), 0F, 1F)
        return x * x * (3.0f - 2.0f * x)
    }
}