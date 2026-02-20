package at.wirecube.additiveanimations.helper;

import android.view.animation.Interpolator;

/**
 * A time-based spring interpolator that approximates spring-like motion using a damped cosine curve.
 * <p>
 * <b>Note:</b> This is a simple curve approximation, not real spring physics. It always runs for
 * a fixed duration and doesn't model true stiffness/damping behavior.
 *
 * @deprecated Use {@link at.wirecube.additiveanimations.additive_animator.BaseAdditiveAnimator#setSpring(float, float)}
 * or {@link at.wirecube.additiveanimations.additive_animator.BaseAdditiveAnimator#setSpringWithDuration(long, float)}
 * instead, which use real spring physics with proper stiffness and damping ratio parameters.
 */
@Deprecated
public class SpringInterpolator implements Interpolator {
    // curve parameters generated with https://www.desmos.com/calculator/6gbvrm5i0s
    private static final double BOUNCY_AMPLITUDE = 0.13;
    private static final double BOUNCY_FREQUENCY = 13.5;

    private static final double SOFT_AMPLITUDE = 0.2;
    private static final double SOFT_FREQUENCY = 6.8;

    double mAmplitude = BOUNCY_AMPLITUDE;
    double mFrequency = BOUNCY_FREQUENCY;

    public SpringInterpolator() {}

    public SpringInterpolator(double amplitude, double frequency) {
        mAmplitude = amplitude;
        mFrequency = frequency;
    }

    public static SpringInterpolator bouncySpring() {
        return new SpringInterpolator(BOUNCY_AMPLITUDE, BOUNCY_FREQUENCY);
    }

    public static SpringInterpolator softSpring() {
        return new SpringInterpolator(SOFT_AMPLITUDE, SOFT_FREQUENCY);
    }

    public float getInterpolation(float time) {
        return (float) (-1 * Math.pow(Math.E, -time/ mAmplitude) * Math.cos(mFrequency * time) + 1);
    }
}
