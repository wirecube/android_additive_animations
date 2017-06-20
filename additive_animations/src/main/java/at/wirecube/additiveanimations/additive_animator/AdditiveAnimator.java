/*
 *  Copyright 2017 David Ganster
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package at.wirecube.additiveanimations.additive_animator;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.Path;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.util.Property;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.wirecube.additiveanimations.helper.AnimationUtils;
import at.wirecube.additiveanimations.helper.EaseInOutPathInterpolator;
import at.wirecube.additiveanimations.helper.FloatProperty;
import at.wirecube.additiveanimations.helper.evaluators.ArgbFloatEvaluator;
import at.wirecube.additiveanimations.helper.evaluators.PathEvaluator;
import at.wirecube.additiveanimations.helper.propertywrappers.ColorProperties;
import at.wirecube.additiveanimations.helper.propertywrappers.ElevationProperties;
import at.wirecube.additiveanimations.helper.propertywrappers.MarginProperties;
import at.wirecube.additiveanimations.helper.propertywrappers.PaddingProperties;
import at.wirecube.additiveanimations.helper.propertywrappers.ScrollProperties;
import at.wirecube.additiveanimations.helper.propertywrappers.SizeProperties;

/**
 * Additive animations are nicely explained here: http://ronnqvi.st/multiple-animations/
*/
public class AdditiveAnimator<T extends AdditiveAnimator> {

    public abstract static class AnimationEndListener {
        public abstract void onAnimationEnd(boolean wasCancelled);
    }

    protected final List<View> mViews = new ArrayList<>(); // all views that will be affected by starting the animation.
    private boolean mIsValid = true; // invalid after start() has been called.
    private T mParent = null; // true when this animator was queued using `then()` chaining.

    private static long sDefaultAnimationDuration = 300;
    private static TimeInterpolator sDefaultInterpolator = EaseInOutPathInterpolator.create();

    protected AdditiveAnimationAccumulator mAnimationAccumulator; // holds temporary values that all animators add to.
    protected TimeInterpolator mCurrentCustomInterpolator = null;

    /**
     * This is just a convenience method when you need to animate a single view.
     * No state is kept in individual AdditiveAnimator instances, so you don't need to keep a reference to it.
     * @param view The view to animate.
     * @return A new instance of AdditiveAnimator with `target` set to `view`.
     */
    public static AdditiveAnimator animate(View view) {
        return new AdditiveAnimator(view);
    }

    /**
     * This is just a convenience method when you need to animate a single view.
     * No state is kept in individual AdditiveAnimator instances, so you don't need to keep a reference to it.
     * @param view The view to animate.
     * @param duration The animation duration.
     * @return A new instance of AdditiveAnimator with the animation target set to `view` and the animationDuration set to `duration`.
     */
    public static AdditiveAnimator animate(View view, long duration) {
        return new AdditiveAnimator(view).setDuration(duration);
    }

    public static void cancelAnimations(View view) {
        AdditiveAnimationStateManager.from(view).cancelAllAnimations();
    }

    public static void cancelAnimation(View view, String animationTag) {
        AdditiveAnimationStateManager.from(view).cancelAnimation(animationTag);
    }

    public static void cancelAnimation(View view, Property<View, Float> property) {
        cancelAnimation(view, property.getName());
    }

    private T self() {
        try {
            return (T) this;
        } catch (ClassCastException e) {
            throw new RuntimeException("Could not cast to subclass. Did you forget to implement `newInstance()`?");
        }
    }

    /**
     * Factory method for creation of subclass instances.
     * Override to use all of the advanced features with your custom subclass.
     */
    protected T newInstance() {
        return (T) new AdditiveAnimator();
    }

    /**
     * Copies all relevant attributes, including (ONLY) current target from `other` to self.
     * Override if you have custom properties that need to be copied.
     */
    protected T setParent(T other) {
        target(other.currentTarget());
        setDuration(other.getValueAnimator().getDuration());
        setInterpolator(other.getValueAnimator().getInterpolator());
        mCurrentCustomInterpolator = other.mCurrentCustomInterpolator;
        mParent = other;
        return self();
    }

    protected AdditiveAnimator(View view) {
        target(view);
    }

    /**
     * Creates a new AdditiveAnimator instance without a target view.
     * You **must** call `target(View v)` before calling one of the animation methods.
     */
    public AdditiveAnimator() {}

    /**
     * Creates a new AdditiveAnimator instance with the specified animation duration for more convenience.
     * You **must** call `target(View v)` before calling one of the animation methods.
     */
    public AdditiveAnimator(long duration) {
        setDuration(duration);
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

    protected AdditiveAnimationStateManager currentAnimationManager() {
        return AdditiveAnimationStateManager.from(currentTarget());
    }

    protected ValueAnimator getValueAnimator() {
        initValueAnimatorIfNeeded();
        return mAnimationAccumulator.getAnimator();
    }

    /**
     * Old API for {@link #target(View)}, which should be used instead.
     * @deprecated Use {@link #target(View)} instead.
     */
    @Deprecated
    public T addTarget(View v) {
        return target(v);
    }

    /**
     * Sets the current animation target. You can change the animation target multiple times before calling
     * {@link #start()}:<p/>
     * <code>
     *     new AdditiveAnimator().target(view1).x(100).target(view2).y(200).start()
     * </code>
     */
    public T target(View v) {
        mViews.add(v);
        initValueAnimatorIfNeeded();
        return self();
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

    public T setStartDelay(long startDelay) {
        getValueAnimator().setStartDelay(startDelay);
        return self();
    }

    public T setDuration(long duration) {
        getValueAnimator().setDuration(duration);
        return self();
    }

    public T setInterpolator(TimeInterpolator interpolator) {
        if(mCurrentCustomInterpolator != null) {
            switchInterpolator(interpolator);
        } else {
            getValueAnimator().setInterpolator(interpolator);
        }
        return self();
    }

    // TODO: docs for possible values (ValueAnimator.INFINITE)
    // TODO: handle parent repeat
    public T setRepeatCount(int repeatCount) {
        getValueAnimator().setRepeatCount(repeatCount);
        return self();
    }

    // TODO: investigate possible problems when repeat modes of children/parents don't match
    public T setRepeatMode(int repeatMode) {
        getValueAnimator().setRepeatMode(repeatMode);
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
        initValueAnimatorIfNeeded();
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
     * Creates a new animator configured to start after the current animator, targeting the last view
     * that was configured with this animator.
     */
    public T then() {
        T newInstance = newInstance();
        newInstance.setParent(this);
        newInstance.setStartDelay(getTotalDuration());
        return newInstance;
    }

    /**
     * Creates a new animator configured to start after <code>delay</code>, targeting the last view
     * that was configured with this animator.
     */
    public T thenWithDelay(long delay) {
        T newAnimator = then();
        newAnimator.setStartDelay(getValueAnimator().getStartDelay() + delay);
        return newAnimator;
    }

    public T thenDelayAfterEnd(long delayAfterEnd) {
        T newAnimator = then();
        newAnimator.setStartDelay(getTotalDuration() + delayAfterEnd);
        return newAnimator;
    }

    public T thenBeforeEnd(long millisBeforeEnd) {
        T newAnimator = then();
        newAnimator.setStartDelay(getTotalDuration() - millisBeforeEnd);
        return newAnimator;
    }

    private long getTotalDuration() {
        if (getValueAnimator().getRepeatCount()== ValueAnimator.INFINITE) {
            return ValueAnimator.DURATION_INFINITE;
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

    public void cancelAllAnimations() {
        for(View v : mViews) {
            AdditiveAnimationStateManager.from(v).cancelAllAnimations();
        }
    }

    public void cancelAnimation(String propertyName) {
        for(View v : mViews) {
            cancelAnimation(v, propertyName);
        }
    }

    public void cancelAnimation(Property<View, Float> property) {
        cancelAnimation(property.getName());
    }

    public static float getTargetPropertyValue(Property<View, Float> property, View v) {
        return AdditiveAnimationStateManager.from(v).getActualPropertyValue(property);
    }

    public static Float getTargetPropertyValue(String propertyName, View v) {
        return AdditiveAnimationStateManager.from(v).getLastTargetValue(propertyName);
    }

    protected static Float getQueuedPropertyValue(String propertyName, View v) {
        return AdditiveAnimationStateManager.from(v).getQueuedPropertyValue(propertyName);
    }

    /**
     * Finds the last target value of the property with the given name, or returns `property.get()`
     * if the property isn't animating at the moment.
     */
    public float getTargetPropertyValue(Property<View, Float> property) {
        return currentAnimationManager() == null ? 0 : currentAnimationManager().getActualPropertyValue(property);
    }

    /**
     * Finds the last target value of the property with the given name, if it was ever animated.
     * This method can return null if the value hasn't been animated or the animation is already done.
     * If you use custom properties in your subclass, you might want to override this method to return
     * the actual model value.
     */
    public Float getTargetPropertyValue(String propertyName) {
        return currentAnimationManager().getLastTargetValue(propertyName);
    }

    /**
     * Returns the last value that was queued for animation, but whose animation has not yet started.
     * This method is for internal use only (keeping track of chained `animateBy` calls).
     */
    protected Float getQueuedPropertyValue(String propertyName) {
        return currentAnimationManager().getQueuedPropertyValue(propertyName);
    }

    final void applyChanges(Map<AdditiveAnimation, Float> accumulatedProperties, View targetView) {
        Map<String, Float> unknownProperties = new HashMap<>();
        for(AdditiveAnimation animation : accumulatedProperties.keySet()) {
            if(animation.getProperty() != null) {
                animation.getProperty().set(targetView, accumulatedProperties.get(animation));
            } else {
                unknownProperties.put(animation.getTag(), accumulatedProperties.get(animation));
            }
        }
        applyCustomProperties(unknownProperties, targetView);
        if(!ViewCompat.isInLayout(targetView)) {
            targetView.requestLayout();
        }
    }

    protected void applyCustomProperties(Map<String, Float> tempProperties, View targetView) {
        // Override to apply custom properties
    }

    protected View currentTarget() {
        if(mViews.size() == 0) {
            return null;
        }
        return mViews.get(mViews.size() - 1);
    }

    protected final AdditiveAnimation createAnimation(Property<View, Float> property, float targetValue) {
        AdditiveAnimation animation = new AdditiveAnimation(currentTarget(), property, property.get(currentTarget()), targetValue);
        animation.setCustomInterpolator(mCurrentCustomInterpolator);
        return animation;
    }

    protected final AdditiveAnimation createAnimation(Property<View, Float> property, Path path, PathEvaluator.PathMode mode, PathEvaluator sharedEvaluator) {
        AdditiveAnimation animation = new AdditiveAnimation(currentTarget(), property, property.get(currentTarget()), path, mode, sharedEvaluator);
        animation.setCustomInterpolator(mCurrentCustomInterpolator);
        return animation;
    }

    protected final T animate(AdditiveAnimation animation) {
        initValueAnimatorIfNeeded();
        currentAnimationManager().addAnimation(mAnimationAccumulator, animation);
        return self();
    }

    protected final T animate(Property<View, Float> property, Path p, PathEvaluator.PathMode mode, PathEvaluator sharedEvaluator) {
        initValueAnimatorIfNeeded();
        return animate(createAnimation(property, p, mode, sharedEvaluator));
    }

    protected final T animate(Property<View, Float> property, float target) {
        return animate(property, target, null);
    }

    protected final T animate(Property<View, Float> property, float target, TypeEvaluator evaluator) {
        initValueAnimatorIfNeeded();
        AdditiveAnimation animation = createAnimation(property, target);
        animation.setCustomTypeEvaluator(evaluator);
        return animate(animation);
    }

    protected final T animatePropertyBy(Property<View, Float> property, float by) {
        initValueAnimatorIfNeeded();
        float currentTarget = getTargetPropertyValue(property);
        if(currentAnimationManager().getQueuedPropertyValue(property.getName()) != null) {
            currentTarget = currentAnimationManager().getQueuedPropertyValue(property.getName());
        }
        return animate(createAnimation(property, currentTarget + by));
    }

    protected final T animatePropertiesAlongPath(Property<View, Float> xProperty, Property<View, Float> yProperty, Property<View, Float> rotationProperty, Path path) {
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

    public T animateProperty(float target, TypeEvaluator evaluator, FloatProperty property) {
        AdditiveAnimation animation = createAnimation(property, target);
        animation.setCustomTypeEvaluator(evaluator);
        return animate(animation);
    }

    public T animateProperty(float target, FloatProperty customProperty) {
        return animate(customProperty, target);
    }

    public T backgroundColor(int color) {
        return animate(ColorProperties.BACKGROUND_COLOR, color, new ArgbFloatEvaluator());
    }

    public T scaleX(float scaleX) {
        return animate(View.SCALE_X, scaleX);
    }

    public T scaleXBy(float scaleXBy) {
        return animatePropertyBy(View.SCALE_X, scaleXBy);
    }

    public T scaleY(float scaleY) {
        return animate(View.SCALE_Y, scaleY);
    }

    public T scaleYBy(float scaleYBy) {
        return animatePropertyBy(View.SCALE_Y, scaleYBy);
    }

    public T scale(float scale) {
        scaleY(scale);
        scaleX(scale);
        return (T)this;
    }

    public T scaleBy(float scalesBy) {
        scaleYBy(scalesBy);
        scaleXBy(scalesBy);
        return self();
    }

    public T translationX(float translationX) {
        return animate(View.TRANSLATION_X, translationX);
    }

    public T translationXBy(float translationXBy) {
        return animatePropertyBy(View.TRANSLATION_X, translationXBy);
    }

    public T translationY(float translationY) {
        return animate(View.TRANSLATION_Y, translationY);
    }

    public T translationYBy(float translationYBy) {
        return animatePropertyBy(View.TRANSLATION_Y, translationYBy);
    }

    @SuppressLint("NewApi")
    public T translationZ(float translationZ) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            animate(View.TRANSLATION_Z, translationZ);
        }
        return self();
    }

    @SuppressLint("NewApi")
    public T translationZBy(float translationZBy) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            animatePropertyBy(View.TRANSLATION_Z, translationZBy);
        }
        return self();
    }

    public T alpha(float alpha) {
        return animate(View.ALPHA, alpha);
    }

    public T alphaBy(float alphaBy) {
        return animatePropertyBy(View.ALPHA, alphaBy);
    }

    public T rotation(float rotation) {
        float shortestDistance = AnimationUtils.shortestAngleBetween(getTargetPropertyValue(View.ROTATION), rotation);
        return animatePropertyBy(View.ROTATION, shortestDistance);
    }

    public T rotationBy(float rotationBy) {
        return animatePropertyBy(View.ROTATION, rotationBy);
    }

    public T rotationX(float rotationX) {
        float shortestDistance = AnimationUtils.shortestAngleBetween(getTargetPropertyValue(View.ROTATION_X), rotationX);
        return animatePropertyBy(View.ROTATION_X, shortestDistance);
    }

    public T rotationXBy(float rotationXBy) {
        return animatePropertyBy(View.ROTATION_X, rotationXBy);
    }

    public T rotationY(float rotationY) {
        float shortestDistance = AnimationUtils.shortestAngleBetween(getTargetPropertyValue(View.ROTATION_Y), rotationY);
        return animatePropertyBy(View.ROTATION_Y, shortestDistance);
    }

    public T rotationYBy(float rotationYBy) {
        return animatePropertyBy(View.ROTATION_Y, rotationYBy);
    }

    public T x(float x) {
        return animate(View.X, x);
    }

    public T xBy(float xBy) {
        return animatePropertyBy(View.X, xBy);
    }

    public T centerX(float centerX) {
        return animate(View.X, centerX - currentTarget().getWidth() / 2);
    }

    public T y(float y) {
        return animate(View.Y, y);
    }

    public T yBy(float yBy) {
        return animatePropertyBy(View.Y, yBy);
    }

    public T centerY(float centerY) {
        return animate(View.Y, centerY - currentTarget().getHeight() / 2);
    }

    @SuppressLint("NewApi")
    public T z(float z) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            animate(View.Z, z);
        }
        return self();
    }

    @SuppressLint("NewApi")
    public T zBy(float zBy) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            animatePropertyBy(View.Z, zBy);
        }
        return self();
    }

    public T xyAlongPath(Path path) {
        return animatePropertiesAlongPath(View.X, View.Y, null, path);
    }

    public T translationXYAlongPath(Path path) {
        return animatePropertiesAlongPath(View.TRANSLATION_X, View.TRANSLATION_Y, null, path);
    }

    public T xyRotationAlongPath(Path path) {
        return animatePropertiesAlongPath(View.X, View.Y, View.ROTATION, path);
    }

    public T translationXYRotationAlongPath(Path path) {
        return animatePropertiesAlongPath(View.TRANSLATION_X, View.TRANSLATION_Y, View.ROTATION, path);
    }

    public T leftMargin(int leftMargin) {
        return animate(MarginProperties.MARGIN_LEFT, leftMargin);
    }

    public T leftMarginBy(int leftMarginBy) {
        return animatePropertyBy(MarginProperties.MARGIN_LEFT, leftMarginBy);
    }

    public T topMargin(int topMargin) {
        return animate(MarginProperties.MARGIN_TOP, topMargin);
    }

    public T topMarginBy(int topMarginBy) {
        return animatePropertyBy(MarginProperties.MARGIN_TOP, topMarginBy);
    }

    public T rightMargin(int rightMargin) {
        return animate(MarginProperties.MARGIN_RIGHT, rightMargin);
    }

    public T rightMarginBy(int rightMarginBy) {
        return animatePropertyBy(MarginProperties.MARGIN_RIGHT, rightMarginBy);
    }

    public T bottomMargin(int bottomMargin) {
        return animate(MarginProperties.MARGIN_BOTTOM, bottomMargin);
    }

    public T bottomMarginBy(int bottomMarginBy) {
        return animatePropertyBy(MarginProperties.MARGIN_BOTTOM, bottomMarginBy);
    }

    public T horizontalMargin(int horizontalMargin) {
        leftMargin(horizontalMargin);
        rightMargin(horizontalMargin);
        return self();
    }

    public T horizontalMarginBy(int horizontalMarginBy) {
        leftMarginBy(horizontalMarginBy);
        rightMarginBy(horizontalMarginBy);
        return self();
    }

    public T verticalMargin(int verticalMargin) {
        topMargin(verticalMargin);
        bottomMargin(verticalMargin);
        return self();
    }

    public T verticalMarginBy(int verticalMarginBy) {
        topMarginBy(verticalMarginBy);
        bottomMarginBy(verticalMarginBy);
        return self();
    }

    public T margin(int margin) {
        leftMargin(margin);
        rightMargin(margin);
        topMargin(margin);
        bottomMargin(margin);
        return self();
    }

    public T marginBy(int marginBy) {
        leftMarginBy(marginBy);
        rightMarginBy(marginBy);
        topMarginBy(marginBy);
        bottomMarginBy(marginBy);
        return self();
    }

    public T topLeftMarginAlongPath(Path path) {
        return animatePropertiesAlongPath(MarginProperties.MARGIN_LEFT, MarginProperties.MARGIN_TOP, null, path);
    }

    public T topRightMarginAlongPath(Path path) {
        return animatePropertiesAlongPath(MarginProperties.MARGIN_RIGHT, MarginProperties.MARGIN_TOP, null, path);
    }

    public T bottomRightMarginAlongPath(Path path) {
        return animatePropertiesAlongPath(MarginProperties.MARGIN_RIGHT, MarginProperties.MARGIN_BOTTOM, null, path);
    }

    public T bottomLeftMarginAlongPath(Path path) {
        return animatePropertiesAlongPath(MarginProperties.MARGIN_LEFT, MarginProperties.MARGIN_BOTTOM, null, path);
    }

    public T width(int width) {
        return animate(SizeProperties.WIDTH, width);
    }

    public T widthBy(int widthBy) {
        return animatePropertyBy(SizeProperties.WIDTH, widthBy);
    }

    public T height(int height) {
        return animate(SizeProperties.HEIGHT, height);
    }

    public T heightBy(int heightBy) {
        return animatePropertyBy(SizeProperties.HEIGHT, heightBy);
    }

    public T size(int size) {
        animate(SizeProperties.WIDTH, size);
        animate(SizeProperties.HEIGHT, size);
        return self();
    }

    public T sizeBy(int sizeBy) {
        animatePropertyBy(SizeProperties.WIDTH, sizeBy);
        animatePropertyBy(SizeProperties.HEIGHT, sizeBy);
        return self();
    }

    public T leftPadding(int leftPadding) {
        return animate(PaddingProperties.PADDING_LEFT, leftPadding);
    }

    public T leftPaddingBy(int leftPaddingBy) {
        return animatePropertyBy(PaddingProperties.PADDING_LEFT, leftPaddingBy);
    }

    public T topPadding(int topPadding) {
        return animate(PaddingProperties.PADDING_TOP, topPadding);
    }

    public T topPaddingBy(int topPaddingBy) {
        return animatePropertyBy(PaddingProperties.PADDING_TOP, topPaddingBy);
    }

    public T rightPadding(int rightPadding) {
        return animate(PaddingProperties.PADDING_RIGHT, rightPadding);
    }

    public T rightPaddingBy(int rightPaddingBy) {
        return animatePropertyBy(PaddingProperties.PADDING_RIGHT, rightPaddingBy);
    }

    public T bottomPadding(int bottomPadding) {
        return animate(PaddingProperties.PADDING_BOTTOM, bottomPadding);
    }

    public T bottomPaddingBy(int bottomPaddingBy) {
        return animatePropertyBy(PaddingProperties.PADDING_BOTTOM, bottomPaddingBy);
    }

    public T horizontalPadding(int horizontalPadding) {
        animate(PaddingProperties.PADDING_LEFT, horizontalPadding);
        animate(PaddingProperties.PADDING_RIGHT, horizontalPadding);
        return self();
    }

    public T horizontalPaddingBy(int horizontalPaddingBy) {
        animatePropertyBy(PaddingProperties.PADDING_LEFT, horizontalPaddingBy);
        animatePropertyBy(PaddingProperties.PADDING_RIGHT, horizontalPaddingBy);
        return self();
    }

    public T verticalPadding(int verticalPadding) {
        animate(PaddingProperties.PADDING_TOP, verticalPadding);
        animate(PaddingProperties.PADDING_BOTTOM, verticalPadding);
        return self();
    }

    public T verticalPaddingBy(int verticalPaddingBy) {
        animatePropertyBy(PaddingProperties.PADDING_TOP, verticalPaddingBy);
        animatePropertyBy(PaddingProperties.PADDING_BOTTOM, verticalPaddingBy);
        return self();
    }

    public T padding(int padding) {
        animate(PaddingProperties.PADDING_LEFT, padding);
        animate(PaddingProperties.PADDING_RIGHT, padding);
        animate(PaddingProperties.PADDING_BOTTOM, padding);
        animate(PaddingProperties.PADDING_TOP, padding);
        return self();
    }

    public T paddingBy(int paddingBy) {
        animatePropertyBy(PaddingProperties.PADDING_LEFT, paddingBy);
        animatePropertyBy(PaddingProperties.PADDING_RIGHT, paddingBy);
        animatePropertyBy(PaddingProperties.PADDING_BOTTOM, paddingBy);
        animatePropertyBy(PaddingProperties.PADDING_TOP, paddingBy);
        return self();
    }

    public T scrollX(int scrollX) {
        return animate(ScrollProperties.SCROLL_X, scrollX);
    }

    public T scrollXBy(int scrollXBy) {
        return animatePropertyBy(ScrollProperties.SCROLL_X, scrollXBy);
    }

    public T scrollY(int scrollY) {
        return animate(ScrollProperties.SCROLL_Y, scrollY);
    }

    public T scrollYBy(int scrollYBy) {
        return animatePropertyBy(ScrollProperties.SCROLL_Y, scrollYBy);
    }

    public T scroll(int x, int y) {
        animate(ScrollProperties.SCROLL_X, x);
        animate(ScrollProperties.SCROLL_Y, y);
        return self();
    }

    public T scrollBy(int xBy, int yBy) {
        animatePropertyBy(ScrollProperties.SCROLL_X, xBy);
        animatePropertyBy(ScrollProperties.SCROLL_Y, yBy);
        return self();
    }

    @SuppressLint("NewApi")
    public T elevation(int elevation) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            animate(ElevationProperties.ELEVATION, elevation);
        }
        return self();
    }

    @SuppressLint("NewApi")
    public T elevationBy(int elevationBy) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            animatePropertyBy(ElevationProperties.ELEVATION, elevationBy);
        }
        return self();
    }


//    public T widthPercent(float widthPercent) {
//        if (initPercentListener()){
//            mPercentListener.widthPercent(widthPercent);
//        }
//        return self();
//    }
//
//    public T widthPercentBy(float widthPercentBy) {
//        if (initPercentListener()){
//            mPercentListener.widthPercentBy(widthPercentBy);
//        }
//        return self();
//    }
//
//    public T heightPercent(float heightPercent) {
//        if (initPercentListener()){
//            mPercentListener.heightPercent(heightPercent);
//        }
//        return self();
//    }
//
//    public T heightPercentBy(float heightPercentBy) {
//        if (initPercentListener()){
//            mPercentListener.heightPercentBy(heightPercentBy);
//        }
//        return self();
//    }
//
//    public T sizePercent(float sizePercent) {
//        if (initPercentListener()){
//            mPercentListener.sizePercent(sizePercent);
//        }
//        return self();
//    }
//
//    public T sizePercentBy(float sizePercentBy) {
//        if (initPercentListener()){
//            mPercentListener.sizePercentBy(sizePercentBy);
//        }
//        return self();
//    }
//
//    public T leftMarginPercent(float marginPercent) {
//        if (initPercentListener()){
//            mPercentListener.leftMarginPercent(marginPercent);
//        }
//        return self();
//    }
//
//    public T leftMarginPercentBy(float marginPercentBy) {
//        if (initPercentListener()){
//            mPercentListener.leftMarginPercentBy(marginPercentBy);
//        }
//        return self();
//    }
//
//    public T topMarginPercent(float marginPercent) {
//        if (initPercentListener()){
//            mPercentListener.topMarginPercent(marginPercent);
//        }
//        return self();
//    }
//
//    public T topMarginPercentBy(float marginPercentBy) {
//        if (initPercentListener()){
//            mPercentListener.topMarginPercentBy(marginPercentBy);
//        }
//        return self();
//    }
//
//    public T bottomMarginPercent(float marginPercent) {
//        if (initPercentListener()){
//            mPercentListener.bottomMarginPercent(marginPercent);
//        }
//        return self();
//    }
//
//    public T bottomMarginPercentBy(float marginPercentBy) {
//        if (initPercentListener()){
//            mPercentListener.bottomMarginPercentBy(marginPercentBy);
//        }
//        return self();
//    }
//
//    public T rightMarginPercent(float marginPercent) {
//        if (initPercentListener()){
//            mPercentListener.rightMarginPercent(marginPercent);
//        }
//        return self();
//    }
//
//    public T rightMarginPercentBy(float marginPercentBy) {
//        if (initPercentListener()){
//            mPercentListener.rightMarginPercentBy(marginPercentBy);
//        }
//        return self();
//    }
//
//    public T horizontalMarginPercent(float marginPercent) {
//        if (initPercentListener()){
//            mPercentListener.horizontalMarginPercent(marginPercent);
//        }
//        return self();
//    }
//
//    public T horizontalMarginPercentBy(float marginPercentBy) {
//        if (initPercentListener()){
//            mPercentListener.horizontalMarginPercentBy(marginPercentBy);
//        }
//        return self();
//    }
//
//    public T verticalMarginPercent(float marginPercent) {
//        if (initPercentListener()){
//            mPercentListener.verticalMarginPercent(marginPercent);
//        }
//        return self();
//    }
//
//    public T verticalMarginPercentBy(float marginPercentBy) {
//        if (initPercentListener()){
//            mPercentListener.verticalMarginPercentBy(marginPercentBy);
//        }
//        return self();
//    }
//
//    public T marginPercent(float marginPercent) {
//        if (initPercentListener()){
//            mPercentListener.marginPercent(marginPercent);
//        }
//        return self();
//    }
//
//    public T marginPercentBy(float marginPercentBy) {
//        if (initPercentListener()){
//            mPercentListener.marginPercentBy(marginPercentBy);
//        }
//        return self();
//    }
//
//    public T aspectRatio(float aspectRatio) {
//        if (initPercentListener()){
//            mPercentListener.aspectRatio(aspectRatio);
//        }
//        return self();
//    }
//
//    public T aspectRatioBy(float aspectRatioBy) {
//        if (initPercentListener()){
//            mPercentListener.aspectRatioBy(aspectRatioBy);
//        }
//        return self();
//    }
}
