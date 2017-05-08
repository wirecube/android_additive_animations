package at.wirecube.additiveanimations.additive_animator;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.graphics.Path;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.util.Property;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.wirecube.additiveanimations.helper.AnimationUtils;
import at.wirecube.additiveanimations.helper.EaseInOutPathInterpolator;
import at.wirecube.additiveanimations.helper.evaluators.ArgbFloatEvaluator;
import at.wirecube.additiveanimations.helper.evaluators.PathEvaluator;
import at.wirecube.additiveanimations.helper.propertywrappers.MarginProperties;
import at.wirecube.additiveanimations.helper.propertywrappers.PaddingProperties;
import at.wirecube.additiveanimations.helper.propertywrappers.ScrollProperties;
import at.wirecube.additiveanimations.helper.propertywrappers.SizeProperties;

/**
 * Additive animations are nicely explained here: http://ronnqvi.st/multiple-animations/
*/
public class AdditiveAnimator<T extends AdditiveAnimator> {

    private static final String BACKGROUND_COLOR_ANIMATION_TAG = "AAA_BACKGROUND_COLOR";

    public abstract static class AnimationEndListener {
        public abstract void onAnimationEnd(boolean wasCancelled);
    }

    protected final List<View> mViews = new ArrayList<>();
    private boolean mIsValid = true; // invalid after start
    private T mParent = null;
    private static long sDefaultAnimationDuration = 300;
    private static TimeInterpolator sDefaultInterpolator = EaseInOutPathInterpolator.create();

    protected AdditiveAnimationApplier mAnimationApplier;

    /**
     * This is just a convenience method when you need to animate a single view.
     * No state is kept in individual AdditiveAnimator instances, so you don't need to keep a reference to it.
     * @param view The view to animate.
     * @return A new instance of AdditiveAnimator with `target` set to `view`.
     */
    public static AdditiveAnimator animate(View view) {
        return new AdditiveAnimator(view);
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
        addTarget(other.currentTarget());
        setDuration(other.getValueAnimator().getDuration());
        setInterpolator(other.getValueAnimator().getInterpolator());
        mParent = other;
        return self();
    }

    protected AdditiveAnimator(View view) {
        addTarget(view);
    }

    /**
     * Creates a new AdditiveAnimator instance without a target view.
     * You **must** call `addTarget(View v)` before calling one of the animation methods.
     */
    public AdditiveAnimator() {}

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
        if(mAnimationApplier == null) {
            mAnimationApplier = new AdditiveAnimationApplier(this);
            getValueAnimator().setInterpolator(sDefaultInterpolator);
            getValueAnimator().setDuration(sDefaultAnimationDuration);
        }
    }

    protected AdditiveAnimationStateManager currentAnimationManager() {
        return AdditiveAnimationStateManager.from(currentTarget());
    }

    protected ValueAnimator getValueAnimator() {
        initValueAnimatorIfNeeded();
        return mAnimationApplier.getAnimator();
    }

    /**
     * Sets the current animation target. You can change the animation target multiple times before calling
     * {@link #start()}:<p/>
     * <code>
     *     new AdditiveAnimator().addTarget(view1).x(100).addTarget(view2).y(200).start()
     * </code>
     */
    public T addTarget(View v) {
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
        getValueAnimator().setInterpolator(interpolator);
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

        getValueAnimator().start();

        // invalidate this animator to prevent incorrect usage:
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
                if(animation.getTag().equals(BACKGROUND_COLOR_ANIMATION_TAG)) {
                    targetView.setBackgroundColor(accumulatedProperties.get(animation).intValue());
                } else {
                    unknownProperties.put(animation.getTag(), accumulatedProperties.get(animation));
                }
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

    protected AdditiveAnimation createDescription(Property<View, Float> property, float targetValue) {
        return new AdditiveAnimation(currentTarget(), property, property.get(currentTarget()), targetValue);
    }

    protected AdditiveAnimation createDescription(Property<View, Float> property, Path path, PathEvaluator.PathMode mode, PathEvaluator sharedEvaluator) {
        return new AdditiveAnimation(currentTarget(), property, property.get(currentTarget()), path, mode, sharedEvaluator);
    }

    protected final void animateProperty(AdditiveAnimation property) {
        initValueAnimatorIfNeeded();
        currentAnimationManager().addAnimation(mAnimationApplier, property);
    }

    protected final void animateProperty(Property<View, Float> property, Path p, PathEvaluator.PathMode mode, PathEvaluator sharedEvaluator) {
        initValueAnimatorIfNeeded();
        currentAnimationManager().addAnimation(mAnimationApplier, createDescription(property, p, mode, sharedEvaluator));
    }

    protected final void animateProperty(Property<View, Float> property, float target) {
        initValueAnimatorIfNeeded();
        currentAnimationManager().addAnimation(mAnimationApplier, createDescription(property, target));
    }

    protected final void animatePropertyBy(Property<View, Float> property, float by) {
        initValueAnimatorIfNeeded();
        float currentTarget = getTargetPropertyValue(property);
        if(currentAnimationManager().getQueuedPropertyValue(property.getName()) != null) {
            currentTarget = currentAnimationManager().getQueuedPropertyValue(property.getName());
        }
        currentAnimationManager().addAnimation(mAnimationApplier, createDescription(property, currentTarget + by));
    }

    public T backgroundColor(int color) {
        try {
            int startVal = ((ColorDrawable)currentTarget().getBackground()).getColor();
            AdditiveAnimation desc = new AdditiveAnimation(currentTarget(), BACKGROUND_COLOR_ANIMATION_TAG, startVal, color);
            desc.setCustomTypeEvaluator(new ArgbFloatEvaluator());
            animateProperty(desc);
        } catch (ClassCastException ex) {
            ex.printStackTrace();
        }
        return self();
    }

    public T scaleX(float scaleX) {
        animateProperty(View.SCALE_X, scaleX);
        return self();
    }

    public T scaleXBy(float scaleXBy) {
        animatePropertyBy(View.SCALE_X, scaleXBy);
        return self();
    }

    public T scaleY(float scaleY) {
        animateProperty(View.SCALE_Y, scaleY);
        return self();
    }

    public T scaleYBy(float scaleYBy) {
        animatePropertyBy(View.SCALE_Y, scaleYBy);
        return self();
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
        animateProperty(View.TRANSLATION_X, translationX);
        return self();
    }

    public T translationXBy(float translationXBy) {
        animatePropertyBy(View.TRANSLATION_X, translationXBy);
        return self();
    }

    public T translationY(float translationY) {
        animateProperty(View.TRANSLATION_Y, translationY);
        return self();
    }

    public T translationYBy(float translationYBy) {
        animatePropertyBy(View.TRANSLATION_Y, translationYBy);
        return self();
    }

    @SuppressLint("NewApi")
    public T translationZ(float translationZ) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            animateProperty(View.TRANSLATION_Z, translationZ);
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
        animateProperty(View.ALPHA, alpha);
        return self();
    }

    public T alphaBy(float alphaBy) {
        animatePropertyBy(View.ALPHA, alphaBy);
        return self();
    }

    public T rotation(float rotation) {
        float shortestDistance = AnimationUtils.shortestAngleBetween(getTargetPropertyValue(View.ROTATION), rotation);
        animatePropertyBy(View.ROTATION, shortestDistance);
        return self();
    }

    public T rotationBy(float rotationBy) {
        animatePropertyBy(View.ROTATION, rotationBy);
        return self();
    }

    public T rotationX(float rotationX) {
        float shortestDistance = AnimationUtils.shortestAngleBetween(getTargetPropertyValue(View.ROTATION_X), rotationX);
        animatePropertyBy(View.ROTATION_X, shortestDistance);
        return self();
    }

    public T rotationXBy(float rotationXBy) {
        animatePropertyBy(View.ROTATION_X, rotationXBy);
        return self();
    }

    public T rotationY(float rotationY) {
        float shortestDistance = AnimationUtils.shortestAngleBetween(getTargetPropertyValue(View.ROTATION_Y), rotationY);
        animatePropertyBy(View.ROTATION_Y, shortestDistance);
        return self();
    }

    public T rotationYBy(float rotationYBy) {
        animatePropertyBy(View.ROTATION_Y, rotationYBy);
        return self();
    }

    public T x(float x) {
        animateProperty(View.X, x);
        return self();
    }

    public T xBy(float xBy) {
        animatePropertyBy(View.X, xBy);
        return self();
    }

    public T centerX(float centerX) {
        animateProperty(View.X, centerX - currentTarget().getWidth() / 2);
        return self();
    }

    public T y(float y) {
        animateProperty(View.Y, y);
        return self();
    }

    public T yBy(float yBy) {
        animatePropertyBy(View.Y, yBy);
        return self();
    }

    public T centerY(float centerY) {
        animateProperty(View.Y, centerY - currentTarget().getHeight() / 2);
        return self();
    }

    @SuppressLint("NewApi")
    public T z(float z) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            animateProperty(View.Z, z);
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
        PathEvaluator sharedEvaluator = new PathEvaluator();
        animateProperty(View.X, path, PathEvaluator.PathMode.X, sharedEvaluator);
        animateProperty(View.Y, path, PathEvaluator.PathMode.Y, sharedEvaluator);
        return self();
    }

    public T xyRotationAlongPath(Path path) {
        PathEvaluator sharedEvaluator = new PathEvaluator();
        animateProperty(View.X, path, PathEvaluator.PathMode.X, sharedEvaluator);
        animateProperty(View.Y, path, PathEvaluator.PathMode.Y, sharedEvaluator);
        animateProperty(View.ROTATION, path, PathEvaluator.PathMode.ROTATION, sharedEvaluator);
        return self();
    }

    public T leftMargin(int leftMargin) {
        animateProperty(MarginProperties.MARGIN_LEFT, leftMargin);
        return self();
    }

    public T leftMarginBy(int leftMarginBy) {
        animatePropertyBy(MarginProperties.MARGIN_LEFT, leftMarginBy);
        return self();
    }

    public T topMargin(int topMargin) {
        animateProperty(MarginProperties.MARGIN_TOP, topMargin);
        return self();
    }

    public T topMarginBy(int topMarginBy) {
        animatePropertyBy(MarginProperties.MARGIN_TOP, topMarginBy);
        return self();
    }

    public T rightMargin(int rightMargin) {
        animateProperty(MarginProperties.MARGIN_RIGHT, rightMargin);
        return self();
    }

    public T rightMarginBy(int rightMarginBy) {
        animatePropertyBy(MarginProperties.MARGIN_RIGHT, rightMarginBy);
        return self();
    }

    public T bottomMargin(int bottomMargin) {
        animateProperty(MarginProperties.MARGIN_BOTTOM, bottomMargin);
        return self();
    }

    public T bottomMarginBy(int bottomMarginBy) {
        animatePropertyBy(MarginProperties.MARGIN_BOTTOM, bottomMarginBy);
        return self();
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

    public T width(int width) {
        animateProperty(SizeProperties.WIDTH, width);
        return self();
    }

    public T widthBy(int widthBy) {
        animatePropertyBy(SizeProperties.WIDTH, widthBy);
        return self();
    }

    public T height(int height) {
        animateProperty(SizeProperties.HEIGHT, height);
        return self();
    }

    public T heightBy(int heightBy) {
        animatePropertyBy(SizeProperties.HEIGHT, heightBy);
        return self();
    }

    public T size(int size) {
        animateProperty(SizeProperties.WIDTH, size);
        animateProperty(SizeProperties.HEIGHT, size);
        return self();
    }

    public T sizeBy(int sizeBy) {
        animatePropertyBy(SizeProperties.WIDTH, sizeBy);
        animatePropertyBy(SizeProperties.HEIGHT, sizeBy);
        return self();
    }

    public T leftPadding(int leftPadding) {
        animateProperty(PaddingProperties.PADDING_LEFT, leftPadding);
        return self();
    }

    public T leftPaddingBy(int leftPaddingBy) {
        animatePropertyBy(PaddingProperties.PADDING_LEFT, leftPaddingBy);
        return self();
    }

    public T topPadding(int topPadding) {
        animateProperty(PaddingProperties.PADDING_TOP, topPadding);
        return self();
    }

    public T topPaddingBy(int topPaddingBy) {
        animatePropertyBy(PaddingProperties.PADDING_TOP, topPaddingBy);
        return self();
    }

    public T rightPadding(int rightPadding) {
        animateProperty(PaddingProperties.PADDING_RIGHT, rightPadding);
        return self();
    }

    public T rightPaddingBy(int rightPaddingBy) {
        animatePropertyBy(PaddingProperties.PADDING_RIGHT, rightPaddingBy);
        return self();
    }

    public T bottomPadding(int bottomPadding) {
        animateProperty(PaddingProperties.PADDING_BOTTOM, bottomPadding);
        return self();
    }

    public T bottomPaddingBy(int bottomPaddingBy) {
        animatePropertyBy(PaddingProperties.PADDING_BOTTOM, bottomPaddingBy);
        return self();
    }

    public T horizontalPadding(int horizontalPadding) {
        animateProperty(PaddingProperties.PADDING_LEFT, horizontalPadding);
        animateProperty(PaddingProperties.PADDING_RIGHT, horizontalPadding);
        return self();
    }

    public T horizontalPaddingBy(int horizontalPaddingBy) {
        animatePropertyBy(PaddingProperties.PADDING_LEFT, horizontalPaddingBy);
        animatePropertyBy(PaddingProperties.PADDING_RIGHT, horizontalPaddingBy);
        return self();
    }

    public T verticalPadding(int verticalPadding) {
        animateProperty(PaddingProperties.PADDING_TOP, verticalPadding);
        animateProperty(PaddingProperties.PADDING_BOTTOM, verticalPadding);
        return self();
    }

    public T verticalPaddingBy(int verticalPaddingBy) {
        animatePropertyBy(PaddingProperties.PADDING_TOP, verticalPaddingBy);
        animatePropertyBy(PaddingProperties.PADDING_BOTTOM, verticalPaddingBy);
        return self();
    }

    public T padding(int padding) {
        animateProperty(PaddingProperties.PADDING_LEFT, padding);
        animateProperty(PaddingProperties.PADDING_RIGHT, padding);
        animateProperty(PaddingProperties.PADDING_BOTTOM, padding);
        animateProperty(PaddingProperties.PADDING_TOP, padding);
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
        animateProperty(ScrollProperties.SCROLL_X, scrollX);
        return self();
    }

    public T scrollXBy(int scrollXBy) {
        animatePropertyBy(ScrollProperties.SCROLL_X, scrollXBy);
        return self();
    }

    public T scrollY(int scrollY) {
        animateProperty(ScrollProperties.SCROLL_Y, scrollY);
        return self();
    }

    public T scrollYBy(int scrollYBy) {
        animatePropertyBy(ScrollProperties.SCROLL_Y, scrollYBy);
        return self();
    }

    public T scroll(int x, int y) {
        animateProperty(ScrollProperties.SCROLL_X, x);
        animateProperty(ScrollProperties.SCROLL_Y, y);
        return self();
    }

    public T scrollBy(int xBy, int yBy) {
        animatePropertyBy(ScrollProperties.SCROLL_X, xBy);
        animatePropertyBy(ScrollProperties.SCROLL_Y, yBy);
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
