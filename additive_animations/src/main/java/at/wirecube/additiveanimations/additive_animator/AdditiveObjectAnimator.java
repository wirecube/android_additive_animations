package at.wirecube.additiveanimations.additive_animator;

import android.animation.TypeEvaluator;

import java.util.List;

import at.wirecube.additiveanimations.helper.FloatProperty;

/**
 * This is a small utility class which can animate any kind of object using the
 * {@link #property(float, FloatProperty)} and {@link #property(float, TypeEvaluator, FloatProperty)} methods.
 * If you'd like to provide your own builder methods for creating animations, subclass {@link BaseAdditiveAnimator}.
 */
public class AdditiveObjectAnimator<V> extends BaseAdditiveAnimator<AdditiveObjectAnimator<V>, V> {

    private Runnable mAnimationApplier = null;

    @Override
    protected AdditiveObjectAnimator<V> newInstance() {
        return new AdditiveObjectAnimator<>();
    }

    public static <V> AdditiveObjectAnimator<V> animate(V target) {
        return new AdditiveObjectAnimator<V>().target(target);
    }

    public static <V> AdditiveObjectAnimator<V> animate(V target, long duration) {
        return animate(target).setDuration(duration);
    }

    public static <V> AdditiveObjectAnimator<V> create() {
        return new AdditiveObjectAnimator<V>();
    }

    public static <V> AdditiveObjectAnimator<V> create(long duration) {
        return new AdditiveObjectAnimator<V>().setDuration(duration);
    }

    @Override
    protected AdditiveObjectAnimator<V> setParent(AdditiveObjectAnimator<V> other) {
        AdditiveObjectAnimator<V>  child = super.setParent(other);
        child.setAnimationApplier(mAnimationApplier);
        return child;
    }

    public AdditiveObjectAnimator<V> setAnimationApplier(Runnable animationApplier) {
        mAnimationApplier = animationApplier;
        return this;
    }

    @Override
    void applyChanges(List<AccumulatedAnimationValue<V>> accumulatedAnimations) {
        super.applyChanges(accumulatedAnimations);
        if(mAnimationApplier != null) {
            mAnimationApplier.run();
        }
    }
}
