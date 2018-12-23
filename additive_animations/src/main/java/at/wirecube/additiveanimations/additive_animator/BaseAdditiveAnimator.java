package at.wirecube.additiveanimations.additive_animator;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.Path;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Property;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
 *           <b><code>public class MyViewAnimator extends BaseAdditiveAnimator{@literal <}MyViewAnimator, View{@literal >}</code></b>
 * @param <V> The type of object to be animated.
 */
public abstract class BaseAdditiveAnimator<T extends BaseAdditiveAnimator, V extends Object> {

    protected T mParent = null; // not null when this animator was queued using `then()` chaining.
    protected V mCurrentTarget = null;
    protected AdditiveAnimationStateManager<V> mCurrentStateManager = null; // only used for performance reasons
    protected AdditiveAnimationAccumulator mAnimationAccumulator; // holds temporary values that all animators add to.
    protected TimeInterpolator mCurrentCustomInterpolator = null;
    protected final List<V> mTargets = new ArrayList<>(1); // all targets that will be affected by starting the animation.

    private Map<V, List<AccumulatedAnimationValue<V>>> mUnknownProperties = new HashMap<>();
    private Set<V> mChangedTargets = new HashSet<>(1);
    private HashMap<String, Float> mChangedUnknownProperties = new HashMap<>();

    /**
     * Indicates which animation group this animator belongs to.
     * An animation group is a set of animators which have different targets, but share the same animations.
     * An example would be: new AdditiveAnimator().targets(v1, v2).alpha(0).start();
     * In this case, v1 and v2 have different AdditiveAnimator instances, but share the same animations (alpha = 0).
     * Animation groups are inherited with then() chaining.
     * All animators in the group can have different starting offsets when using the targets(views, stagger) method.
     */
    protected AdditiveAnimatorGroup mAnimatorGroup = null;

    private boolean mIsValid = true; // invalid after start() has been called.

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

    public static void cancelAnimations(Object... targets) {
        if(targets == null) { return; }
        for(Object target : targets) {
            cancelAnimations(target);
        }
    }

    public static void cancelAnimations(List<Object> targets) {
        if(targets == null) { return; }
        for(Object target : targets) {
            cancelAnimations(target);
        }
    }

    public static void cancelAnimation(Object target, String animationTag) {
        AdditiveAnimationStateManager.from(target).cancelAnimation(animationTag);
    }

    public static void cancelAnimation(List<Object> targets, String animationTag) {
        if(targets == null) { return; }
        for(Object target : targets) {
            AdditiveAnimationStateManager.from(target).cancelAnimation(animationTag);
        }
    }

    public static <T> void cancelAnimation(T target, Property<T, Float> property) {
        cancelAnimation(target, property.getName());
    }

    private void initValueAnimatorIfNeeded() {
        if(!mIsValid) {
            throw new RuntimeException("AdditiveAnimator instances cannot be reused.");
        }
        if(mAnimationAccumulator == null) {
            mAnimationAccumulator = new AdditiveAnimationAccumulator(this);
            getValueAnimator().setInterpolator(sDefaultInterpolator);
            getValueAnimator().setDuration(sDefaultAnimationDuration);
        }
    }

    protected ValueAnimator getValueAnimator() {
        initValueAnimatorIfNeeded();
        return mAnimationAccumulator.getAnimator();
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
     */
    public Float getTargetPropertyValue(String propertyName) {
        if(mCurrentStateManager != null && mCurrentStateManager.getLastTargetValue(propertyName) != null) {
            return mCurrentStateManager.getLastTargetValue(propertyName);
        } else {
            return getCurrentPropertyValue(propertyName);
        }
    }

    /**
     * Returns the actual value of the animation target with the given name.
     * @apiNote If you use custom tags in your subclass WITHOUT PROPERTIES, you MUST override this method to return
     * the actual model value, otherwise some features will crash.
     */
    abstract public Float getCurrentPropertyValue(String propertyName);

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

        this.onApplyChanges();
    }

    /**
     * This method will be called when the current frame has been calculated.
     * Override this method in a subclass to trigger a layout of your view/canvas/custom object.
     */
    public abstract void onApplyChanges();

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

    protected final T animate(final AdditiveAnimation animation) {
        initValueAnimatorIfNeeded();
        mCurrentStateManager.addAnimation(mAnimationAccumulator, animation);
        runIfParentIsInSameAnimationGroup(new Runnable() {
            @Override
            public void run() {
                final Float startValue;
                if (animation.getProperty() != null) {
                    startValue = (Float) animation.getProperty().get(mParent.getCurrentTarget());
                } else {
                    startValue = BaseAdditiveAnimator.this.getTargetPropertyValue(animation.getTag());
                }
                mParent.animate(animation.cloneWithTarget(mParent.getCurrentTarget(), startValue));
            }
        });
        return self();
    }

    protected final T animate(Property<V, Float> property, Path p, PathEvaluator.PathMode mode, PathEvaluator sharedEvaluator) {
        initValueAnimatorIfNeeded();
        return animate(createAnimation(property, p, mode, sharedEvaluator));
    }

    protected final T animate(Property<V, Float> property, float target) {
        return animate(property, target, null);
    }

    protected final T animate(Property<V, Float> property, float target, TypeEvaluator evaluator) {
        initValueAnimatorIfNeeded();
        AdditiveAnimation animation = createAnimation(property, target);
        animation.setCustomTypeEvaluator(evaluator);
        return animate(animation);
    }

    /**
     * TODO: documentation of byValueCanBeUsedByParentAnimators
     */
    protected final T animatePropertyBy(final Property<V, Float> property, final float by, final boolean byValueCanBeUsedByParentAnimators) {
        initValueAnimatorIfNeeded();
        float currentTarget = getTargetPropertyValue(property);
        if(getQueuedPropertyValue(property.getName()) != null) {
            currentTarget = getQueuedPropertyValue(property.getName());
        }
        AdditiveAnimation animation = createAnimation(property, currentTarget + by);
        initValueAnimatorIfNeeded();
        mCurrentStateManager.addAnimation(mAnimationAccumulator, animation);
        if(byValueCanBeUsedByParentAnimators) {
            runIfParentIsInSameAnimationGroup(new Runnable() {
                @Override
                public void run() {
                    mParent.animatePropertyBy(property, by, byValueCanBeUsedByParentAnimators);
                }
            });
        }
        return self();
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
    public T animateProperty(float target, TypeEvaluator<Float> evaluator, FloatProperty<V> property) {
        return property(target, evaluator, property);
    }

    public T property(float target, TypeEvaluator<Float> evaluator, FloatProperty<V> property) {
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
     * @deprecated This method name contains a typo. Use {@link #setDefaultInterpolator(TimeInterpolator)} instead.
     */
    @Deprecated
    public static void setsDefaultInterpolator(TimeInterpolator interpolator) {
        sDefaultInterpolator = interpolator;
    }

    /**
     * Globally sets the default interpolator to use for all AdditiveAnimator instances.
     * You can override this by calling {@link #setInterpolator(TimeInterpolator)} on a specific instance.
     */
    public static void setDefaultInterpolator(TimeInterpolator interpolator) {
        sDefaultInterpolator = interpolator;
    }

    /**
     * Sets the current animation target. You can change the animation target multiple times before calling
     * {@link #start()}:<p/>
     * <code>
     *     new AdditiveAnimator().target(view1).x(100).target(view2).y(200).start()
     * </code>
     * <p/>
     * If you want to animate the same property of multiple views, use {@link #targets(Object[])} or {@link #targets(List, long)}
     */
    public T target(V v) {
        if(mAnimatorGroup != null) {
            // Changes to animation duration, interpolator etc. always affect the whole animation group.
            // After changing target, we don't want to mess with the current animation group, because you would want something like this to be possible:
            // new AdditiveAnimator().targets(v1, v2).setDuration(200).x(50).target(v3).x(100).setDuration(100).start();
            // It's clear from that example that the animation duration for v1 and v2 should be the same (200), but different for v3 (100).
            return (T) createChildWithDelayAfterParentStart(0).target(v);
        }
        mCurrentTarget = v;
        mCurrentStateManager = AdditiveAnimationStateManager.from(v);
        initValueAnimatorIfNeeded();
        return self();
    }

    /**
     * Used to animate the same property of multiple views.
     * This is a convenience method which simply creates a series of animators which will start simultaneously.
     * Example: <p/>
     * <code>new AdditiveAnimator().targets(textView, button).alpha(0).start()</code>
     */
    public T targets(@NonNull V... vs) {
        return targets(Arrays.asList(vs), 0);
    }

    /**
     * Used to animate the same property of multiple views.
     * This is a convenience method which simply creates a series of animators which will start simultaneously.
     * Example: <p/>
     * <code>new AdditiveAnimator().targets(myViewList).alpha(0).start()</code>
     */
    public T targets(@NonNull List<V> vs) {
        return targets(vs, 0);
    }

    /**
     * Used to animate the same property of multiple views, with a delay before each element.
     * This is a convenience method which simply creates a series of animators which will start with `stagger` offset after each other.
     * Example: <p/>
     * <code>new AdditiveAnimator().targets(Arrays.asList(textView, button), 100).translationYBy(100).alpha(0).start()</code>
     */
    public T targets(@NonNull List<V> vs, long stagger) {
        if(vs.isEmpty()) {
            throw new IllegalArgumentException("You passed a list containing 0 views to BaseAdditiveAnimator.targets(). This would cause buggy animations, so it's probably more desirable to crash instead.");
        }

        if(mAnimatorGroup != null) {
            // if we are already part of an animation group, we create a new animator:
            return (T) createChildWithDelayAfterParentStart(0).targets(vs, stagger);
        }

        AdditiveAnimatorGroup group = new AdditiveAnimatorGroup();
        // Call order is important here:
        // We must call target() before setting the animator group, otherwise we create a new animator
        this.target(vs.get(0));
        group.add(this);

        T animator = self();
        for(int i = 1; i < vs.size(); i++) {
            animator = (T) animator.createChildWithDelayAfterParentStart(stagger);
            // Same as above: call order (first target(), then adding to animator group) is important.
            animator.target(vs.get(i));
            group.add(animator);
        }
        return animator;
    }

    public T addUpdateListener(ValueAnimator.AnimatorUpdateListener listener) {
        getValueAnimator().addUpdateListener(listener);
        return self();
    }

    @SuppressLint("NewApi")
    public T addPauseListener(Animator.AnimatorPauseListener listener) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getValueAnimator().addPauseListener(listener);
        }
        return self();
    }

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

    public T setStartDelay(final long startDelay) {
        getValueAnimator().setStartDelay(startDelay);
        runIfParentIsInSameAnimationGroup(new Runnable() {
            @Override
            public void run() {
                mParent.setStartDelay(startDelay);
            }
        });
        return self();
    }

    public T setDuration(final long duration) {
        getValueAnimator().setDuration(duration);
        runIfParentIsInSameAnimationGroup(new Runnable() {
            @Override
            public void run() {
                mParent.setDuration(duration);
            }
        });
        return self();
    }

    public T switchDuration(long durationMillis) {
        T child = thenWithDelay(0);
        child.setDuration(durationMillis);
        return child;
    }

    public T setInterpolator(final TimeInterpolator interpolator) {
        if(mCurrentCustomInterpolator != null) {
            return switchInterpolator(interpolator);
        }
        getValueAnimator().setInterpolator(interpolator);
        runIfParentIsInSameAnimationGroup(new Runnable() {
            @Override
            public void run() {
                mParent.setInterpolator(interpolator);
            }
        });
        return self();
    }

    public T switchToDefaultInterpolator() {
        return switchInterpolator(sDefaultInterpolator);
    }

    // TODO: docs for possible values (ValueAnimator.INFINITE)
    // TODO: handle parent repeat
    public T setRepeatCount(final int repeatCount) {
        getValueAnimator().setRepeatCount(repeatCount);
        runIfParentIsInSameAnimationGroup(new Runnable() {
            @Override
            public void run() {
                mParent.setRepeatCount(repeatCount);
            }
        });
        return self();
    }

    // TODO: investigate possible problems when repeat modes of children/parents don't match
    public T setRepeatMode(final int repeatMode) {
        getValueAnimator().setRepeatMode(repeatMode);
        runIfParentIsInSameAnimationGroup(new Runnable() {
            @Override
            public void run() {
                mParent.setRepeatMode(repeatMode);
            }
        });
        return self();
    }

    /**
     * Switches to the given interpolator only for all following animations.
     * This is different from `setInterpolator` in that it doesn't apply to animations that were created
     * before calling this method.
     * Calling `setInterpolator` after calling this method at least once will behave the same as calling `switchInterpolator`
     * to prevent accidentally overriding the effects of `switchInterpolator`.
     */
    public T switchInterpolator(final TimeInterpolator newInterpolator) {
        initValueAnimatorIfNeeded();
        // set custom interpolator for all animations so far
        Collection<AdditiveAnimation> animations = mAnimationAccumulator.getAnimations();
        for(AdditiveAnimation animation : animations) {
            animation.setCustomInterpolator(getValueAnimator().getInterpolator());
        }

        mCurrentCustomInterpolator = newInterpolator;
        // now we want to animate linearly, all animations are going to map to the current value themselves
        getValueAnimator().setInterpolator(new LinearInterpolator());

        runIfParentIsInSameAnimationGroup(new Runnable() {
            @Override
            public void run() {
                mParent.switchInterpolator(newInterpolator);
            }
        });
        return self();
    }

    /**
     * Factory method for creation of subclass instances.
     * Override to use all of the advanced features with your custom subclass.
     */
    protected abstract T newInstance();

    /**
     * Creates a new animator configured to start after the current animator with the current target
     * that was configured with this animator.
     */
    public T then() {
        if(mAnimatorGroup != null) {
            return (T)mAnimatorGroup.copyAndChain(new AdditiveAnimatorGroup.StartDelayProvider() {
                @Override
                public long getStartDelay(BaseAdditiveAnimator parent) {
                    return parent.getTotalDuration();
                }
            }).outermostChildAnimator();
        }
        return createChildWithRawDelay(getTotalDuration());
    }

    /**
     * Creates a new animator configured to start after <code>delay</code> milliseconds from now
     * with the last used target and interpolator.
     */
    public T thenWithDelay(final long delay) {
        if(mAnimatorGroup != null) {
            return (T) mAnimatorGroup.copyAndChain(new AdditiveAnimatorGroup.StartDelayProvider() {
                @Override
                public long getStartDelay(BaseAdditiveAnimator parent) {
                    return parent.getValueAnimator().getStartDelay() + delay;
                }
            }).outermostChildAnimator();
        }
        return createChildWithDelayAfterParentStart(delay);
    }

    /**
     * Creates a new animator configured to start after <code>delayAfterEnd</code> milliseconds after the previous animation has ended
     * with the last used target and interpolator.
     */
    public T thenDelayAfterEnd(final long delayAfterEnd) {
        if(mAnimatorGroup != null) {
            return (T) mAnimatorGroup.copyAndChain(new AdditiveAnimatorGroup.StartDelayProvider() {
                @Override
                public long getStartDelay(BaseAdditiveAnimator parent) {
                    return parent.getTotalDuration() + delayAfterEnd;
                }
            }).outermostChildAnimator();
        }
        return createChildWithRawDelay(getTotalDuration() + delayAfterEnd);
    }

    /**
     * Creates a new animator configured to start after <code>delayBeforeEnd</code> milliseconds before the previous animation finishes
     * with the last used target and interpolator.
     */
    public T thenBeforeEnd(final long millisBeforeEnd) {
        if(mAnimatorGroup != null) {
            return (T)mAnimatorGroup.copyAndChain(new AdditiveAnimatorGroup.StartDelayProvider() {
                @Override
                public long getStartDelay(BaseAdditiveAnimator parent) {
                    return parent.getTotalDuration() - millisBeforeEnd;
                }
            }).outermostChildAnimator();
        }
        return createChildWithRawDelay(getTotalDuration() - millisBeforeEnd);
    }

    protected T createChildWithRawDelay(long delay) {
        T newInstance = newInstance();
        newInstance.setParent(self());
        newInstance.setStartDelay(delay);
        return newInstance;
    }

    protected T createChildWithDelayAfterParentStart(long delay) {
        T newInstance = createChildWithRawDelay(0);
        newInstance.setStartDelay(getValueAnimator().getStartDelay() + delay);
        return newInstance;
    }

    // package-private on purpose
    long getTotalDuration() {
        if (getValueAnimator().getRepeatCount() == ValueAnimator.INFINITE) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                return ValueAnimator.DURATION_INFINITE;
            } else {
                return -1;
            }
        } else {
            return getValueAnimator().getStartDelay() + (getValueAnimator().getDuration() * (getValueAnimator().getRepeatCount() + 1));
        }
    }

    public void start() {
        if(mParent != null) {
            mParent.start();
        }

        // TODO: don't start a ValueAnimator if getTotalDuration() == 0
        getValueAnimator().start();

        // invalidate this animator to prevent incorrect usage:
        // TODO: get rid of this flag. Animators should simply not become invalid.
        mIsValid = false;
    }

    void setAnimationGroup(AdditiveAnimatorGroup group) {
        mAnimatorGroup = group;
    }

    /**
     * Copies all relevant attributes, including (ONLY) current target from `other` to self.
     * Override if you have custom properties that need to be copied.
     */
    protected T setParent(T other) {
        target((V) other.getCurrentTarget());
        setDuration(other.getValueAnimator().getDuration());
        setInterpolator(other.getValueAnimator().getInterpolator());
        setRepeatCount(other.getValueAnimator().getRepeatCount());
        setRepeatMode(other.getValueAnimator().getRepeatMode());
        mCurrentCustomInterpolator = other.mCurrentCustomInterpolator;
        mParent = other;
        return self();
    }

    protected void runIfParentIsInSameAnimationGroup(Runnable r) {
        if(mAnimatorGroup != null && mParent != null && mParent.mAnimatorGroup == mAnimatorGroup) {
            r.run();
        }
    }
}
