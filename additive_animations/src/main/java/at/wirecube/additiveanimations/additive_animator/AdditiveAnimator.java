package at.wirecube.additiveanimations.additive_animator;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.util.Property;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.wirecube.additiveanimations.helper.EaseInOutPathInterpolator;

public class AdditiveAnimator<T extends AdditiveAnimator> {

    protected List<View> mViews = new ArrayList<>();
    private ValueAnimator mValueAnimator;

    public static AdditiveAnimator animate(View view) {
        // This is just a convenience method when you need to animate a single view.
        // All state except for the value animator is stored in AdditiveAnimationApplier.
        return new AdditiveAnimator(view);
    }

    private AdditiveAnimator(View view) {
        initValueAnimator();
        setTarget(view);
    }

    public AdditiveAnimator() {
        initValueAnimator();
    }

    private void initValueAnimator() {
        mValueAnimator = ValueAnimator.ofFloat(0f, 1f);
        mValueAnimator.setInterpolator(EaseInOutPathInterpolator.create());
    }

    private void initValueAnimatorIfNeeded() {
        if(mValueAnimator == null) {
            initValueAnimator();
        }

        if(currentAnimationApplier() != null) {
            currentAnimationApplier().setAnimationUpdater(this);
            currentAnimationApplier().setNextValueAnimator(mValueAnimator);
        }
    }

    protected AdditiveAnimationApplier currentAnimationApplier() {
        return AdditiveAnimationApplier.from(currentTarget());
    }

    /**
     * Finds the last target value of the property with the given name, or returns `property.get()`
     * if the property isn't animating at the moment.
     */
    public float getTargetPropertyValue(Property<View, Float> property) {
        return currentAnimationApplier() == null ? 0 : currentAnimationApplier().getActualPropertyValue(property);
    }

    /**
     * Finds the last target value of the property with the given name, if it was ever animated.
     * This method can return null if the value hasn't been animated or the animation is already done.
     * If you use custom properties in your subclass, you might want to override this method to return
     * the actual model value.
     */
    public Float getTargetPropertyValue(String propertyName) {
        return currentAnimationApplier().getLastTargetValue(propertyName);
    }

    public T setTarget(View v) {
        mViews.add(v);
        AdditiveAnimationApplier applier = AdditiveAnimationApplier.from(v);
        applier.setAnimationUpdater(this);
        applier.setNextValueAnimator(mValueAnimator);
        return (T) this;
    }

    public final void applyChanges(Map<AdditivelyAnimatedPropertyDescription, Float> tempProperties, View targetView) {
        Map<String, Float> unknownProperties = new HashMap<>();
        for(AdditivelyAnimatedPropertyDescription key : tempProperties.keySet()) {
            if(key.getProperty() != null) {
                key.getProperty().set(targetView, tempProperties.get(key));
            } else {
                unknownProperties.put(key.getTag(), tempProperties.get(key));
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

    protected AdditivelyAnimatedPropertyDescription createDescription(Property<View, Float> property, float targetValue) {
        return new AdditivelyAnimatedPropertyDescription(property, property.get(currentTarget()), targetValue);
    }

    protected final void animateProperty(AdditivelyAnimatedPropertyDescription property) {
        initValueAnimatorIfNeeded();
        currentAnimationApplier().addAnimation(property);
    }

    protected final void animateProperty(Property<View, Float> property, float target) {
        initValueAnimatorIfNeeded();
        currentAnimationApplier().addAnimation(createDescription(property, target));
    }

    protected final void animatePropertyBy(Property<View, Float> property, float by) {
        initValueAnimatorIfNeeded();
        currentAnimationApplier().addAnimation(createDescription(property, currentAnimationApplier().getActualPropertyValue(property) + by));
    }

    public void start() {
        mValueAnimator.start();
        for(View v : mViews) {
            AdditiveAnimationApplier.from(v).onStart();
        }
        mValueAnimator = null;
    }

    public void cancelAllAnimations() {
        for(View v : mViews) {
            AdditiveAnimationApplier.from(v).cancelAllAnimations();
        }
    }

    public T scaleX(float scaleX) {
        animateProperty(View.SCALE_X, scaleX);
        return (T) this;
    }

    public T scaleXBy(float scaleXBy) {
        animatePropertyBy(View.SCALE_X, scaleXBy);
        return (T) this;
    }

    public T scaleY(float scaleY) {
        animateProperty(View.SCALE_Y, scaleY);
        return (T) this;
    }

    public T scaleYBy(float scaleYBy) {
        animatePropertyBy(View.SCALE_Y, scaleYBy);
        return (T) this;
    }

    public T scale(float scale) {
        scaleY(scale);
        scaleX(scale);
        return (T)this;
    }

    public T scaleBy(float scalesBy) {
        scaleYBy(scalesBy);
        scaleXBy(scalesBy);
        return (T) this;
    }

    public T translationX(float translationX) {
        animateProperty(View.TRANSLATION_X, translationX);
        return (T) this;
    }

    public T translationXBy(float translationXBy) {
        animatePropertyBy(View.TRANSLATION_X, translationXBy);
        return (T) this;
    }

    public T translationY(float translationY) {
        animateProperty(View.TRANSLATION_Y, translationY);
        return (T) this;
    }

    public T translationYBy(float translationYBy) {
        animatePropertyBy(View.TRANSLATION_Y, translationYBy);
        return (T) this;
    }

    @SuppressLint("NewApi")
    public T translationZ(float translationZ) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            animateProperty(View.TRANSLATION_Z, translationZ);
        }
        return (T) this;
    }

    @SuppressLint("NewApi")
    public T translationZBy(float translationZBy) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            animatePropertyBy(View.TRANSLATION_Z, translationZBy);
        }
        return (T) this;
    }

    public T alpha(float alpha) {
        animateProperty(View.ALPHA, alpha);
        return (T) this;
    }

    public T alphaBy(float alphaBy) {
        animatePropertyBy(View.ALPHA, alphaBy);
        return (T) this;
    }

    public T rotation(float rotation) {
        animateProperty(View.ROTATION, rotation);
        return (T) this;
    }

    public T rotationBy(float rotationBy) {
        animatePropertyBy(View.ROTATION, rotationBy);
        return (T) this;
    }

    public T rotationX(float rotationX) {
        animateProperty(View.ROTATION_X, rotationX);
        return (T) this;
    }

    public T rotationXBy(float rotationXBy) {
        animatePropertyBy(View.ROTATION_X, rotationXBy);
        return (T) this;
    }

    public T rotationY(float rotationY) {
        animateProperty(View.ROTATION_Y, rotationY);
        return (T) this;
    }

    public T rotationYBy(float rotationYBy) {
        animatePropertyBy(View.ROTATION_Y, rotationYBy);
        return (T) this;
    }

    public T x(float x) {
        animateProperty(View.X, x);
        return (T) this;
    }

    public T xBy(float xBy) {
        animatePropertyBy(View.X, xBy);
        return (T) this;
    }

    public T y(float y) {
        animateProperty(View.Y, y);
        return (T) this;
    }

    public T yBy(float yBy) {
        animatePropertyBy(View.Y, yBy);
        return (T) this;
    }

    @SuppressLint("NewApi")
    public T z(float z) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            animateProperty(View.Z, z);
        }
        return (T) this;
    }

    @SuppressLint("NewApi")
    public T zBy(float zBy) {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            animatePropertyBy(View.Z, zBy);
        }
        return (T) this;
    }

//    public T leftMargin(int leftMargin) {
//        if (initMarginListener()) {
//            mMarginListener.leftMargin(leftMargin);
//        }
//        return (T) this;
//    }
//
//    public T leftMarginBy(int leftMarginBy) {
//        if (initMarginListener()) {
//            mMarginListener.leftMarginBy(leftMarginBy);
//        }
//        return (T) this;
//    }
//
//    public T topMargin(int topMargin) {
//        if (initMarginListener()) {
//            mMarginListener.topMargin(topMargin);
//        }
//        return (T) this;
//    }
//
//    public T topMarginBy(int topMarginBy) {
//        if (initMarginListener()) {
//            mMarginListener.topMarginBy(topMarginBy);
//        }
//        return (T) this;
//    }
//
//    public T rightMargin(int rightMargin) {
//        if (initMarginListener()) {
//            mMarginListener.rightMargin(rightMargin);
//        }
//        return (T) this;
//    }
//
//    public T rightMarginBy(int rightMarginBy) {
//        if (initMarginListener()) {
//            mMarginListener.rightMarginBy(rightMarginBy);
//        }
//        return (T) this;
//    }
//
//    public T bottomMargin(int bottomMargin) {
//        if (initMarginListener()) {
//            mMarginListener.bottomMargin(bottomMargin);
//        }
//        return (T) this;
//    }
//
//    public T bottomMarginBy(int bottomMarginBy) {
//        if (initMarginListener()) {
//            mMarginListener.bottomMarginBy(bottomMarginBy);
//        }
//        return (T) this;
//    }
//
//    public T horizontalMargin(int horizontalMargin) {
//        if (initMarginListener()) {
//            mMarginListener.horizontalMargin(horizontalMargin);
//        }
//        return (T) this;
//    }
//
//    public T horizontalMarginBy(int horizontalMarginBy) {
//        if (initMarginListener()) {
//            mMarginListener.horizontalMarginBy(horizontalMarginBy);
//        }
//        return (T) this;
//    }
//
//    public T verticalMargin(int verticalMargin) {
//        if (initMarginListener()) {
//            mMarginListener.verticalMargin(verticalMargin);
//        }
//        return (T) this;
//    }
//
//    public T verticalMarginBy(int verticalMarginBy) {
//        if (initMarginListener()) {
//            mMarginListener.verticalMarginBy(verticalMarginBy);
//        }
//        return (T) this;
//    }
//
//    public T margin(int margin) {
//        if (initMarginListener()) {
//            mMarginListener.margin(margin);
//        }
//        return (T) this;
//    }
//
//    public T marginBy(int marginBy) {
//        if (initMarginListener()) {
//            mMarginListener.marginBy(marginBy);
//        }
//        return (T) this;
//    }
//
//    public T width(int width) {
//        if (initDimensionListener()) {
//            mDimensionListener.width(width);
//        }
//        return (T) this;
//    }
//
//    public T widthBy(int widthBy) {
//        if (initDimensionListener()) {
//            mDimensionListener.widthBy(widthBy);
//        }
//        return (T) this;
//    }
//
//    public T height(int height) {
//        if (initDimensionListener()) {
//            mDimensionListener.height(height);
//        }
//        return (T) this;
//    }
//
//    public T heightBy(int heightBy) {
//        if (initDimensionListener()) {
//            mDimensionListener.heightBy(heightBy);
//        }
//        return (T) this;
//    }
//
//    public T size(int size) {
//        if (initDimensionListener()) {
//            mDimensionListener.size(size);
//        }
//        return (T) this;
//    }
//
//    public T sizeBy(int sizeBy) {
//        if (initDimensionListener()) {
//            mDimensionListener.sizeBy(sizeBy);
//        }
//        return (T) this;
//    }
//
//    public T leftPadding(int leftPadding) {
//        if (initPaddingListener()) {
//            mPaddingListener.leftPadding(leftPadding);
//        }
//        return (T) this;
//    }
//
//    public T leftPaddingBy(int leftPaddingBy) {
//        if (initPaddingListener()) {
//            mPaddingListener.leftPaddingBy(leftPaddingBy);
//        }
//        return (T) this;
//    }
//
//    public T topPadding(int topPadding) {
//        if (initPaddingListener()) {
//            mPaddingListener.topPadding(topPadding);
//        }
//        return (T) this;
//    }
//
//    public T topPaddingBy(int topPaddingBy) {
//        if (initPaddingListener()) {
//            mPaddingListener.topPaddingBy(topPaddingBy);
//        }
//        return (T) this;
//    }
//
//    public T rightPadding(int rightPadding) {
//        if (initPaddingListener()) {
//            mPaddingListener.rightPadding(rightPadding);
//        }
//        return (T) this;
//    }
//
//    public T rightPaddingBy(int rightPaddingBy) {
//        if (initPaddingListener()) {
//            mPaddingListener.rightPaddingBy(rightPaddingBy);
//        }
//        return (T) this;
//    }
//
//    public T bottomPadding(int bottomPadding) {
//        if (initPaddingListener()) {
//            mPaddingListener.bottomPadding(bottomPadding);
//        }
//        return (T) this;
//    }
//
//    public T bottomPaddingBy(int bottomPaddingBy) {
//        if (initPaddingListener()) {
//            mPaddingListener.bottomPaddingBy(bottomPaddingBy);
//        }
//        return (T) this;
//    }
//
//    public T horizontalPadding(int horizontalPadding) {
//        if (initPaddingListener()) {
//            mPaddingListener.horizontalPadding(horizontalPadding);
//        }
//        return (T) this;
//    }
//
//    public T horizontalPaddingBy(int horizontalPaddingBy) {
//        if (initPaddingListener()) {
//            mPaddingListener.horizontalPaddingBy(horizontalPaddingBy);
//        }
//        return (T) this;
//    }
//
//    public T verticalPadding(int verticalPadding) {
//        if (initPaddingListener()) {
//            mPaddingListener.verticalPadding(verticalPadding);
//        }
//        return (T) this;
//    }
//
//    public T verticalPaddingBy(int verticalPaddingBy) {
//        if (initPaddingListener()) {
//            mPaddingListener.verticalPaddingBy(verticalPaddingBy);
//        }
//        return (T) this;
//    }
//
//    public T padding(int padding) {
//        if (initPaddingListener()) {
//            mPaddingListener.padding(padding);
//        }
//        return (T) this;
//    }
//
//    public T paddingBy(int paddingBy) {
//        if (initPaddingListener()) {
//            mPaddingListener.paddingBy(paddingBy);
//        }
//        return (T) this;
//    }
//
//    public T scrollX(int scrollX) {
//        if (initScrollListener()) {
//            mScrollListener.scrollX(scrollX);
//        }
//        return (T) this;
//    }
//
//    public T scrollXBy(int scrollXBy) {
//        if (initScrollListener()) {
//            mScrollListener.scrollXBy(scrollXBy);
//        }
//        return (T) this;
//    }
//
//    public T scrollY(int scrollY) {
//        if (initScrollListener()) {
//            mScrollListener.scrollY(scrollY);
//        }
//        return (T) this;
//    }
//
//    public T scrollYBy(int scrollYBy) {
//        if (initScrollListener()) {
//            mScrollListener.scrollYBy(scrollYBy);
//        }
//        return (T) this;
//    }
//
//    public T widthPercent(float widthPercent) {
//        if (initPercentListener()){
//            mPercentListener.widthPercent(widthPercent);
//        }
//        return (T) this;
//    }
//
//    public T widthPercentBy(float widthPercentBy) {
//        if (initPercentListener()){
//            mPercentListener.widthPercentBy(widthPercentBy);
//        }
//        return (T) this;
//    }
//
//    public T heightPercent(float heightPercent) {
//        if (initPercentListener()){
//            mPercentListener.heightPercent(heightPercent);
//        }
//        return (T) this;
//    }
//
//    public T heightPercentBy(float heightPercentBy) {
//        if (initPercentListener()){
//            mPercentListener.heightPercentBy(heightPercentBy);
//        }
//        return (T) this;
//    }
//
//    public T sizePercent(float sizePercent) {
//        if (initPercentListener()){
//            mPercentListener.sizePercent(sizePercent);
//        }
//        return (T) this;
//    }
//
//    public T sizePercentBy(float sizePercentBy) {
//        if (initPercentListener()){
//            mPercentListener.sizePercentBy(sizePercentBy);
//        }
//        return (T) this;
//    }
//
//    public T leftMarginPercent(float marginPercent) {
//        if (initPercentListener()){
//            mPercentListener.leftMarginPercent(marginPercent);
//        }
//        return (T) this;
//    }
//
//    public T leftMarginPercentBy(float marginPercentBy) {
//        if (initPercentListener()){
//            mPercentListener.leftMarginPercentBy(marginPercentBy);
//        }
//        return (T) this;
//    }
//
//    public T topMarginPercent(float marginPercent) {
//        if (initPercentListener()){
//            mPercentListener.topMarginPercent(marginPercent);
//        }
//        return (T) this;
//    }
//
//    public T topMarginPercentBy(float marginPercentBy) {
//        if (initPercentListener()){
//            mPercentListener.topMarginPercentBy(marginPercentBy);
//        }
//        return (T) this;
//    }
//
//    public T bottomMarginPercent(float marginPercent) {
//        if (initPercentListener()){
//            mPercentListener.bottomMarginPercent(marginPercent);
//        }
//        return (T) this;
//    }
//
//    public T bottomMarginPercentBy(float marginPercentBy) {
//        if (initPercentListener()){
//            mPercentListener.bottomMarginPercentBy(marginPercentBy);
//        }
//        return (T) this;
//    }
//
//    public T rightMarginPercent(float marginPercent) {
//        if (initPercentListener()){
//            mPercentListener.rightMarginPercent(marginPercent);
//        }
//        return (T) this;
//    }
//
//    public T rightMarginPercentBy(float marginPercentBy) {
//        if (initPercentListener()){
//            mPercentListener.rightMarginPercentBy(marginPercentBy);
//        }
//        return (T) this;
//    }
//
//    public T horizontalMarginPercent(float marginPercent) {
//        if (initPercentListener()){
//            mPercentListener.horizontalMarginPercent(marginPercent);
//        }
//        return (T) this;
//    }
//
//    public T horizontalMarginPercentBy(float marginPercentBy) {
//        if (initPercentListener()){
//            mPercentListener.horizontalMarginPercentBy(marginPercentBy);
//        }
//        return (T) this;
//    }
//
//    public T verticalMarginPercent(float marginPercent) {
//        if (initPercentListener()){
//            mPercentListener.verticalMarginPercent(marginPercent);
//        }
//        return (T) this;
//    }
//
//    public T verticalMarginPercentBy(float marginPercentBy) {
//        if (initPercentListener()){
//            mPercentListener.verticalMarginPercentBy(marginPercentBy);
//        }
//        return (T) this;
//    }
//
//    public T marginPercent(float marginPercent) {
//        if (initPercentListener()){
//            mPercentListener.marginPercent(marginPercent);
//        }
//        return (T) this;
//    }
//
//    public T marginPercentBy(float marginPercentBy) {
//        if (initPercentListener()){
//            mPercentListener.marginPercentBy(marginPercentBy);
//        }
//        return (T) this;
//    }
//
//    public T aspectRatio(float aspectRatio) {
//        if (initPercentListener()){
//            mPercentListener.aspectRatio(aspectRatio);
//        }
//        return (T) this;
//    }
//
//    public T aspectRatioBy(float aspectRatioBy) {
//        if (initPercentListener()){
//            mPercentListener.aspectRatioBy(aspectRatioBy);
//        }
//        return (T) this;
//    }

    public T setDuration(long duration) {
        initValueAnimatorIfNeeded();
        mValueAnimator.setDuration(duration);
        return (T)this;
    }

    public T setInterpolator(TimeInterpolator interpolator) {
        initValueAnimatorIfNeeded();
        mValueAnimator.setInterpolator(interpolator);
        return (T)this;
    }

    public long getDuration() {
        initValueAnimatorIfNeeded();
        return mValueAnimator.getDuration();
    }

    public TimeInterpolator getInterpolator() {
        initValueAnimatorIfNeeded();
        return mValueAnimator.getInterpolator();
    }
}
