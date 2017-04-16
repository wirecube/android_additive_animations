package at.wirecube.additiveanimations.additiveanimationsdemo.animation.additive_animations.base;

import android.animation.TimeInterpolator;
import android.util.Property;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

import at.wirecube.additiveanimations.additiveanimationsdemo.animation.EaseInOutPathInterpolator;

public class AdditiveAnimator<T extends AdditiveAnimator> {

    // TODO: support animating multiple views at once
    protected View mView;
    protected AdditiveAnimationApplier mAnimator;
    protected TimeInterpolator mInterpolator = EaseInOutPathInterpolator.create();
    protected int mDuration = 300;

    public AdditiveAnimator(View view) {
        mView = view;
        mAnimator = AdditiveAnimationApplier.from(view);
        mAnimator.setAnimationUpdater(this);
    }

    public final void applyChanges(Map<AdditivelyAnimatedPropertyDescription, Float> tempProperties, View targetView) {
        Map<AdditivelyAnimatedPropertyDescription, Float> unknownProperties = new HashMap<>();
        for(AdditivelyAnimatedPropertyDescription key : tempProperties.keySet()) {
            if(key.getProperty() != null) {
                key.getProperty().set(targetView, tempProperties.get(key));
            } else {
                unknownProperties.put(key, tempProperties.get(key));
            }
        }
        applyCustomProperties(unknownProperties, targetView);
    }

    protected void applyCustomProperties(Map<AdditivelyAnimatedPropertyDescription, Float> tempProperties, View targetView) {
        // Override to apply custom properties
    }

    protected AdditivelyAnimatedPropertyDescription createDescription(Property<View, Float> property, float targetValue) {
        return new AdditivelyAnimatedPropertyDescription(property, property.get(mView), targetValue);
    }

    public void start() {
        mAnimator.start();
    }

    public T x(float targetX) {
        mAnimator.addAnimation(createDescription(View.X, targetX));
        return (T)this;
    }

    public T y(float targetY) {
        mAnimator.addAnimation(createDescription(View.Y, targetY));
        return (T)this;
    }

    public T setDuration(int duration) {
        this.mDuration = duration;
        return (T)this;
    }

    public T setInterpolator(TimeInterpolator interpolator) {
        mInterpolator = interpolator;
        return (T)this;
    }

    public int getDuration() {
        return mDuration;
    }

    public TimeInterpolator getInterpolator() {
        return mInterpolator;
    }
}
