package dev.mim1q.derelict.vendor.gimm1q.interpolation;

import dev.mim1q.derelict.vendor.gimm1q.interpolation.AnimatedProperty.EasingFunction;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/**
 * Utility functions related to easing
 */
public class EasingUtils {
    /**
     * Interpolates the individual values of two {@link Vec3} objects and returns a new one with the given easing
     * function applied using the given {@code delta} value.
     *
     * @param start          the starting {@link Vec3}
     * @param end            the ending {@link Vec3}
     * @param delta          the delta value to use for interpolation
     * @param easingFunction the easing function to use for interpolation, see {@link EasingFunction}
     * @return a new {@link Vec3} object with the interpolated values
     * @see Easing
     * @see EasingFunction
     */
    public static Vec3 interpolateVec(Vec3 start, Vec3 end, float delta, EasingFunction easingFunction) {
        return new Vec3(
                easingFunction.ease((float) start.x(), (float) end.x(), delta),
                easingFunction.ease((float) start.y(), (float) end.y(), delta),
                easingFunction.ease((float) start.z(), (float) end.z(), delta)
        );
    }

    /**
     * Interpolates the individual values of two {@link Vector3f} objects and returns a new one with the given easing
     * function applied using the given {@code delta} value.
     *
     * @param start          the starting {@link Vector3f}
     * @param end            the ending {@link Vector3f}
     * @param delta          the delta value to use for interpolation
     * @param easingFunction the easing function to use for interpolation, see {@link EasingFunction}
     * @return a new {@link Vector3f} object with the interpolated values
     * @see Easing
     * @see EasingFunction
     */
    public static Vector3f interpolateVec(Vector3f start, Vector3f end, float delta, EasingFunction easingFunction) {
        return new Vector3f(
                easingFunction.ease(start.x, end.x, delta),
                easingFunction.ease(start.y, end.y, delta),
                easingFunction.ease(start.z, end.z, delta)
        );
    }
}
