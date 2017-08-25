package at.wirecube.additiveanimations.additive_animator;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.Path;
import android.renderscript.Sampler;
import android.support.annotation.CallSuper;
import android.util.Property;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Delayed;

import at.wirecube.additiveanimations.helper.EaseInOutPathInterpolator;
import at.wirecube.additiveanimations.helper.FloatProperty;
import at.wirecube.additiveanimations.helper.evaluators.PathEvaluator;

/**
 * This is the base class which provides access to all non-specific animation creation methods such as
 * creation, timing and chaining of additive animations.
 * Subclasses should provide builder methods which create specific animations (see {@link SubclassableAdditiveViewAnimator} for examples).
 * @param <T> This generic should be instantiated with a concrete subclass of BaseAdditiveAnimator. It is used to access the builder methods across hierarchies.
 *           Example:<p>
 *
 *           <b><code>public class MyViewAnimator extends BaseAdditiveAnimator{@literal<}MyAnimator, View{@literal>}</code></b>
 * @param <V> The type of object to be animated.
 */
public abstract class BaseAdditiveAnimator<T extends BaseAdditiveAnimator, V extends Object> {
    protected T mParent = null; // true when this animator was queued using `then()` chaining.
    protected V mCurrentTarget = null;
    protected AdditiveAnimationStateManager<V> mCurrentStateManager = null; // only used for performance reasons
    protected AdditiveAnimationAccumulator mAnimationAccumulator; // holds temporary values that all animators add to.
    protected TimeInterpolator mCurrentCustomInterpolator = null;
    protected final List<V> mTargets = new ArrayList<>(1); // all targets that will be affected by starting the animation.

    private Map<V, List<AccumulatedAnimationValue<V>>> mUnknownProperties = new HashMap<>();
    private Set<V> mChangedTargets = new HashSet<>(1);
    private HashMap<String, Float> mChangedUnknownProperties = new HashMap<>();
    private ValueAnimatorManager mValueAnimatorManager;

    private boolean mIsValid = true; // invalid after start() has been called.


    // Metadata about the animation
    private long mDuration = sDefaultAnimationDuration;
    private long mStartDelay;
    private int mRepeatCount = 0;
    private int mRepeatMode = ValueAnimator.RESTART;

    private static long sDefaultAnimationDuration = 300;
    private static TimeInterpolator sDefaultInterpolator = EaseInOutPathInterpolator.create();

    protected T self() {
        try {
            return (T) this;
        } catch (ClassCastException e) {
            throw new RuntimeException("Could not cast to subclass. Did you forget to implement `newInstance()`?");
        }
    }

    public static void cancelAnimations(Object target) {
        AdditiveAnimationStateManager.from(target).cancelAllAnimations();
    }

    public static void cancelAnimation(Object target, String animationTag) {
        AdditiveAnimationStateManager.from(target).cancelAnimation(animationTag);
    }

    public static <T> void cancelAnimation(T target, Property<T, Float> property) {
        cancelAnimation(target, property.getName());
    }

    private void initAnimationAccumulatorIfNeeded() {
        if(mAnimationAccumulator == null) {
            mAnimationAccumulator = new AdditiveAnimationAccumulator(this);
            setDuration(mDuration);
        }
    }

    protected ValueAnimatorManager getValueAnimator() {
        if(mValueAnimatorManager == null) {
            mValueAnimatorManager = new ValueAnimatorManager();
            mValueAnimatorManager.setInterpolator(sDefaultInterpolator);
        }
        return mValueAnimatorManager;
    }

    protected AdditiveAnimationAccumulator getAnimationAccumulator() {
        initAnimationAccumulatorIfNeeded();
        return mAnimationAccumulator;
    }

    /**
     * Old API for {@link #target(V)}, which should be used instead.
     * @deprecated Use {@link #target(V)} instead.
     */
    @Deprecated
    public T addTarget(V v) {
        return target(v);
    }

    /**
     * Finds the last target value of the property with the given name, or returns `property.get()`
     * if the property isn't animating at the moment.
     */
    public float getTargetPropertyValue(Property<V, Float> property) {
        return mCurrentStateManager == null ? 0 : mCurrentStateManager.getActualPropertyValue(property);
    }

    /**
     * Finds the last target value of the property with the given name, if it was ever animated.
     * This method can return null if the value hasn't been animated or the animation is already done.
     * If you use custom properties in your subclass, you might want to override this method to return
     * the actual model value.
     */
    public Float getTargetPropertyValue(String propertyName) {
        return mCurrentStateManager == null ? null : mCurrentStateManager.getLastTargetValue(propertyName);
    }

    /**
     * Returns the last value that was queued for animation, but whose animation has not yet started.
     * This method is for internal use only (keeping track of chained `animateBy` calls).
     */
    protected Float getQueuedPropertyValue(String propertyName) {
        return mCurrentStateManager.getQueuedPropertyValue(propertyName);
    }

    void applyChanges(List<AccumulatedAnimationValue<V>> accumulatedAnimations) {
        for(AccumulatedAnimationValue<V> accumulatedAnimationValue : accumulatedAnimations) {
            V target = accumulatedAnimationValue.animation.getTarget();
            mChangedTargets.add(target);
            if(accumulatedAnimationValue.animation.getProperty() != null) {
                accumulatedAnimationValue.animation.getProperty().set(target, accumulatedAnimationValue.tempValue);
            } else {
                if(mUnknownProperties == null) {
                    mUnknownProperties = new HashMap<>();
                }
                List<AccumulatedAnimationValue<V>> accumulatedValues = mUnknownProperties.get(target);
                if(accumulatedValues == null) {
                    accumulatedValues = new ArrayList<>(1);
                    mUnknownProperties.put(target, accumulatedValues);
                }
                accumulatedValues.add(accumulatedAnimationValue);
            }
        }

        // TODO: onChangedTargets method for subclasses

        if(mUnknownProperties != null) {
            for (V v : mUnknownProperties.keySet()) {
                for(AccumulatedAnimationValue value : mUnknownProperties.get(v)) {
                    mChangedUnknownProperties.put(value.animation.getTag(), value.tempValue);
                }
                applyCustomProperties(mChangedUnknownProperties, v);
            }
        }

        // reuse the set/map/lists
        mChangedTargets.clear();
        for(Collection<AccumulatedAnimationValue<V>> properties : mUnknownProperties.values()) {
            properties.clear();
        }
        mChangedUnknownProperties.clear();
    }

    protected void applyCustomProperties(Map<String, Float> tempProperties, V target) {
        // Override to apply custom properties
    }

    protected V getCurrentTarget() {
        return mCurrentTarget;
    }

    protected final AdditiveAnimation createAnimation(Property<V, Float> property, float targetValue) {
        AdditiveAnimation animation = new AdditiveAnimation(mCurrentTarget, property, property.get(mCurrentTarget), targetValue);
        animation.setCustomInterpolator(mCurrentCustomInterpolator);
        return animation;
    }

    protected final AdditiveAnimation createAnimation(Property<V, Float> property, Path path, PathEvaluator.PathMode mode, PathEvaluator sharedEvaluator) {
        AdditiveAnimation animation = new AdditiveAnimation(mCurrentTarget, property, property.get(mCurrentTarget), path, mode, sharedEvaluator);
        animation.setCustomInterpolator(mCurrentCustomInterpolator);
        return animation;
    }

    protected final T animate(AdditiveAnimation animation) {
        initAnimationAccumulatorIfNeeded();
        mCurrentStateManager.addAnimation(mAnimationAccumulator, animation);
        return self();
    }

    protected final T animate(Property<V, Float> property, Path p, PathEvaluator.PathMode mode, PathEvaluator sharedEvaluator) {
        initAnimationAccumulatorIfNeeded();
        return animate(createAnimation(property, p, mode, sharedEvaluator));
    }

    protected final T animate(Property<V, Float> property, float target) {
        return animate(property, target, null);
    }

    protected final T animate(Property<V, Float> property, float target, TypeEvaluator evaluator) {
        initAnimationAccumulatorIfNeeded();
        AdditiveAnimation animation = createAnimation(property, target);
        animation.setCustomTypeEvaluator(evaluator);
        return animate(animation);
    }

    protected final T animatePropertyBy(Property<V, Float> property, float by) {
        initAnimationAccumulatorIfNeeded();
        float currentTarget = getTargetPropertyValue(property);
        if(getQueuedPropertyValue(property.getName()) != null) {
            currentTarget = getQueuedPropertyValue(property.getName());
        }
        return animate(createAnimation(property, currentTarget + by));
    }

    protected final T animatePropertiesAlongPath(Property<V, Float> xProperty, Property<V, Float> yProperty, Property<V, Float> rotationProperty, Path path) {
        PathEvaluator sharedEvaluator = new PathEvaluator();
        if(xProperty != null) {
            animate(xProperty, path, PathEvaluator.PathMode.X, sharedEvaluator);
        }
        if(yProperty != null) {
            animate(yProperty, path, PathEvaluator.PathMode.Y, sharedEvaluator);
        }
        if(rotationProperty != null) {
            animate(rotationProperty, path, PathEvaluator.PathMode.ROTATION, sharedEvaluator);
        }
        return self();
    }


    /**
     * Old API for {@link #property(float, TypeEvaluator, FloatProperty)}, which should be used instead.
     * @deprecated Use {@link #property(float, TypeEvaluator, FloatProperty)} instead.
     */
    public T animateProperty(float target, TypeEvaluator evaluator, FloatProperty<V> property) {
        return property(target, evaluator, property);
    }

    public T property(float target, TypeEvaluator evaluator, FloatProperty<V> property) {
        AdditiveAnimation animation = createAnimation(property, target);
        animation.setCustomTypeEvaluator(evaluator);
        return animate(animation);
    }

    /**
     * Old API for {@link #property(float, FloatProperty)}, which should be used instead.
     * @deprecated Use {@link #property(float, FloatProperty)} instead.
     */
    public T animateProperty(float target, FloatProperty<V> customProperty) {
        return property(target, customProperty);
    }

    public T property(float target, FloatProperty<V> customProperty) {
        return animate(customProperty, target);
    }

    /**
     * Globally sets the default animation duration to use for all AdditiveAnimator instances.
     * You can override this by calling {@link #setDuration(long)} on a specific instance.
     */
    public static void setDefaultDuration(long defaultDuration) {
        sDefaultAnimationDuration = defaultDuration;
    }

    /**
     * Globally sets the default interpolator to use for all AdditiveAnimator instances.
     * You can override this by calling {@link #setInterpolator(TimeInterpolator)} on a specific instance.
     */
    public static void setsDefaultInterpolator(TimeInterpolator interpolator) {
        sDefaultInterpolator = interpolator;
    }

    /**
     * Sets the current animation target. You can change the animation target multiple times before calling
     * {@link #start()}:<p/>
     * <code>
     *     new AdditiveAnimator().target(view1).x(100).target(view2).y(200).start()
     * </code>
     */
    public T target(V v) {
        mCurrentTarget = v;
        mCurrentStateManager = AdditiveAnimationStateManager.from(v);
        initAnimationAccumulatorIfNeeded();
        return self();
    }

    public T addUpdateListener(ValueAnimator.AnimatorUpdateListener listener) {
        mValueAnimatorManager.addUpdateListener(listener);
        return self();
    }

    @SuppressLint("NewApi")
//    public T addPauseListener(Animator.AnimatorPauseListener listener) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            getValueAnimator().addPauseListener(listener);
//        }
//        return self();
//    }

    public T addListener(Animator.AnimatorListener listener) {
        getValueAnimator().addListener(listener);
        return self();
    }

    public T addEndAction(final AnimationEndListener r) {
        getValueAnimator().addListener(new AnimatorListenerAdapter() {
            boolean wasCancelled = false;

            @Override
            public void onAnimationEnd(Animator animation) {
                r.onAnimationEnd(wasCancelled);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                wasCancelled = true;
            }
        });
        return self();
    }

    public T addStartAction(final Runnable r) {
        getValueAnimator().addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                r.run();
            }
        });
        return self();
    }

    T setRawStartDelay(long startDelay) {
        if(mParent == null || mParent.getValueAnimator() != getValueAnimator()) {
            getValueAnimator().setStartDelay(startDelay);
        } else {
            mStartDelay = startDelay;
        }
        return self();
    }

    public T setStartDelay(long startDelay) {
        startDelay = mParent != null ? mParent.getStartDelay() + startDelay : startDelay;
        return setRawStartDelay(startDelay);
    }

    public T setDuration(long duration) {
        mDuration = duration;
        return self();
    }

    public T setInterpolator(TimeInterpolator interpolator) {
        if(mCurrentCustomInterpolator != null || (mParent != null && mParent.getValueAnimator() == getValueAnimator())) {
            switchInterpolator(interpolator);
        } else {
            getValueAnimator().setInterpolator(interpolator);
        }
        return self();
    }

    // TODO: docs for possible values (ValueAnimator.INFINITE)
    // TODO: handle parent repeat
    public T setRepeatCount(int repeatCount) {
        mRepeatCount = repeatCount;
        if(repeatCount == ValueAnimator.INFINITE && mParent != null && mParent.getValueAnimator() == getValueAnimator()) {
            mValueAnimatorManager = new ValueAnimatorManager();
            if(mCurrentCustomInterpolator != null) {
                mValueAnimatorManager.setInterpolator(mCurrentCustomInterpolator);
                mValueAnimatorManager.setStartDelay(getStartDelay());
                mStartDelay = 0;
                mValueAnimatorManager.setRepeatCount(ValueAnimator.INFINITE);
            }
        }
        return self();
    }

    /**
     * Defines what this animation should do when it reaches the end. This
     * setting is applied only when the repeat count is either greater than
     * 0 or {@link android.animation.ValueAnimator#INFINITE}. Defaults to {@link android.animation.ValueAnimator#RESTART}.
     *
     * @param repeatMode {@link android.animation.ValueAnimator#RESTART} or {@link android.animation.ValueAnimator#REVERSE}
     */
    public T setRepeatMode(int repeatMode) {
        mRepeatMode = repeatMode;
        return self();
    }

    /**
     * Switches to the given interpolator only for all following animations.
     * This is different from `setInterpolator` in that it doesn't apply to animations that were created
     * before calling this method.
     * Calling `setInterpolator` after calling this method at least once will behave the same as calling `switchInterpolator`
     * to prevent accidentally overriding the effects of `switchInterpolator`.
     */
    public T switchInterpolator(TimeInterpolator newInterpolator) {
        initAnimationAccumulatorIfNeeded();
        // set custom interpolator for all animations so far
        Collection<AdditiveAnimation> animations = mAnimationAccumulator.getAnimations();
        for(AdditiveAnimation animation : animations) {
            animation.setCustomInterpolator(getValueAnimator().getInterpolator());
        }

        mCurrentCustomInterpolator = newInterpolator;
        // now we want to animate linearly, all animations are going to map to the current value themselves
        getValueAnimator().setInterpolator(new LinearInterpolator());
        return self();
    }


    /**
     * Factory method for creation of subclass instances.
     * Override to use all of the advanced features with your custom subclass.
     */
    protected abstract T newInstance();

    @CallSuper
    protected void setParent(T parent) {
        mParent = parent;
        // implement in subclass if needed
    }

    /**
     * Creates a new animator configured to start after the current animator with the current target
     * that was configured with this animator.
     */
    public T then() {
        T newInstance = newInstance();
        if(getRepeatCount() != ValueAnimator.INFINITE) {
            ((BaseAdditiveAnimator) newInstance).mValueAnimatorManager = getValueAnimator();
        }
        newInstance.setParent(this);

        newInstance.mCurrentCustomInterpolator = mCurrentCustomInterpolator;

        // switch to linear interpolation if necessary
        if(!(getValueAnimator().getInterpolator() instanceof LinearInterpolator) && getRepeatCount() != ValueAnimator.INFINITE) {
            TimeInterpolator myInterpolator = getValueAnimator().getInterpolator();
            switchInterpolator(myInterpolator);
            newInstance.mCurrentCustomInterpolator = mCurrentCustomInterpolator;
        }

        // value animator shouldn't have start delay when there are multiple accumulators
        if(getStartDelay() == 0 && getValueAnimator().getStartDelay() != 0 && getValueAnimator() == newInstance.getValueAnimator()) {
            setRawStartDelay(mValueAnimatorManager.getStartDelay());
            mValueAnimatorManager.setStartDelay(0);
        }

        newInstance.target(getCurrentTarget());
        newInstance.setDuration(getDuration());

        // The order here is important: setRepeatCount() creates a new ValueAnimatorManager if mRepeatCount == ValueAnimator.INFINITE
        newInstance.setRepeatCount(getRepeatCount());
        newInstance.setRepeatMode(getRepeatMode());
        newInstance.setRawStartDelay(Math.max(0, getTotalDuration())); // getTotalDuration() returns -1 for ValueAnimator.DURATION_INFINITE

        return newInstance;
    }

    /**
     * Creates a new animator configured to start after <code>delay</code> with the current target
     * that was configured with this animator.
     */
    public T thenWithDelay(long delay) {
        T newAnimator = then();
        newAnimator.setRawStartDelay(getStartDelay() + delay);
        return newAnimator;
    }

    public T thenDelayAfterEnd(long delayAfterEnd) {
        T newAnimator = then();
        newAnimator.setRawStartDelay(getTotalDuration() + delayAfterEnd);
        return newAnimator;
    }

    public T thenBeforeEnd(long millisBeforeEnd) {
        T newAnimator = then();
        newAnimator.setRawStartDelay(getTotalDuration() - millisBeforeEnd);
        return newAnimator;
    }

    private long getTotalDuration() {
        if (getRepeatCount() == ValueAnimator.INFINITE) {
            return ValueAnimator.DURATION_INFINITE;
        } else {
            return getStartDelay() + (getDuration() * (getRepeatCount() + 1));
        }
    }

    public void start() {
        getValueAnimator().addAnimator(this);

        if(mParent != null) {
            mParent.start();
        }

        if(mParent == null || mParent.getValueAnimator() != getValueAnimator()) {
            getValueAnimator().start();
        }

        // invalidate this animator to prevent incorrect usage:
        // TODO: get rid of this flag. Animators should simply not become invalid.
        mIsValid = false;
    }

    public void cancelAllAnimations() {
        for(V v : mTargets) {
            AdditiveAnimationStateManager.from(v).cancelAllAnimations();
        }
    }

    public void cancelAnimation(String propertyName) {
        for(V v : mTargets) {
            cancelAnimation(v, propertyName);
        }
    }

    public void cancelAnimation(Property<V, Float> property) {
        cancelAnimation(property.getName());
    }

    public long getDuration() {
        return mDuration;
    }

    public long getStartDelay() {
        return Math.max(mStartDelay, getValueAnimator().getStartDelay());
    }

    public int getRepeatCount() {
        return mRepeatCount;
    }

    /**
     * @return {@link android.animation.ValueAnimator#RESTART} or {@link android.animation.ValueAnimator#REVERSE}
     */
    public int getRepeatMode() {
        return mRepeatMode;
    }


    public T repeatAll(int repeatCount) {
        getValueAnimator().setRepeatCount(repeatCount);
        return self();
    }

    public T repeatModeAll(int repeatMode) {
        getValueAnimator().setRepeatMode(repeatMode);
        return self();
    }

}
