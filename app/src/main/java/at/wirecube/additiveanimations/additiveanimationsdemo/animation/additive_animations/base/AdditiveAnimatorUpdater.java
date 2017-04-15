package at.wirecube.additiveanimations.additiveanimationsdemo.animation.additive_animations.base;

import android.animation.TimeInterpolator;
import android.util.Property;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

import at.wirecube.additiveanimations.additiveanimationsdemo.animation.EaseInOutPathInterpolator;

public class AdditiveAnimatorUpdater {

    private View mView;
    private AdditiveAnimator mAnimator;
    private TimeInterpolator mInterpolator = new EaseInOutPathInterpolator();
    private int mDuration = 300;

    protected AdditiveAnimatorUpdater(View view, AdditiveAnimator animator) {
        mView = view;
        mAnimator = animator;
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

    private AdditivelyAnimatedPropertyDescription createDescription(Property<View, Float> property, float targetValue) {
        return new AdditivelyAnimatedPropertyDescription(property, property.get(mView), targetValue);
    }

    public void start() {
        mAnimator.start();
    }

    // TODO: find a way to return the subclass type here (clever generics maybe?)
    public AdditiveAnimatorUpdater x(float targetX) {
        mAnimator.addAnimation(createDescription(View.X, targetX));
        return this;
    }

    public AdditiveAnimatorUpdater y(float targetY) {
        mAnimator.addAnimation(createDescription(View.Y, targetY));
        return this;
    }

    public AdditiveAnimatorUpdater setDuration(int duration) {
        this.mDuration = duration;
        return this;
    }

    public AdditiveAnimatorUpdater setInterpolator(TimeInterpolator interpolator) {
        mInterpolator = interpolator;
        return this;
    }

    public int getDuration() {
        return mDuration;
    }

    public TimeInterpolator getInterpolator() {
        return mInterpolator;
    }
}
