package at.wirecube.additiveanimations.additiveanimationsdemo.animation.additive_animations.base;

import android.animation.ValueAnimator;
import android.view.View;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdditiveAnimationHolder {
    private Map<AdditivelyAnimatedPropertyDescription, Float> diffs = new HashMap<>();
    private Map<AdditivelyAnimatedPropertyDescription, Float> lastValues = new HashMap<>();
    private Map<AdditivelyAnimatedPropertyDescription, Float> targets = new HashMap<>();

    private boolean shouldRequestLayout = false;
    private View mAnimationTargetView;
    private ValueAnimator animator;
    private AdditiveAnimator updater;
    private AdditiveAnimationApplier.AccumulatedProperties mTempProperties;

    AdditiveAnimationHolder(AdditivelyAnimatedPropertyDescription description, ValueAnimator animator, View animationTargetView, AdditiveAnimator animationChangeApplier, AdditiveAnimationApplier.AccumulatedProperties tempProperties) {
        diffs.put(description, description.getTargetValue() - description.getStartValue());
        lastValues.put(description, new Float(0));
        targets.put(description, description.getTargetValue());
        this.animator = animator;
        this.mAnimationTargetView = animationTargetView;
        this.updater = animationChangeApplier;
        this.mTempProperties = tempProperties;
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                AdditiveAnimationApplier.AccumulatedProperties params = mTempProperties;
                for (AdditivelyAnimatedPropertyDescription tag : targets.keySet()) {
                    params.tempProperties.put(tag, params.tempProperties.get(tag) + getDelta(tag, animation.getAnimatedFraction()));
                }
                if (shouldRequestLayout) {
                    updater.applyChanges(params.tempProperties, mAnimationTargetView);
                }
            }
        });
    }

    public void addAnimatedProperty(AdditivelyAnimatedPropertyDescription propertyDescription) {
        diffs.put(propertyDescription, propertyDescription.getTargetValue() - propertyDescription.getStartValue());
        lastValues.put(propertyDescription, new Float(0));
        targets.put(propertyDescription, propertyDescription.getTargetValue());
    }

    public final float getDelta(AdditivelyAnimatedPropertyDescription tag, float progress) {
        float diff = diffs.get(tag);
        float lastVal = lastValues.get(tag);
        float newVal = diff * progress;
        float delta = newVal - lastVal;
        lastValues.put(tag, newVal);
        return delta;
    }

    public final void cancel() {
        animator.cancel();
    }

    public final Map<AdditivelyAnimatedPropertyDescription, Float> getTargets() { return targets; }

    public final boolean hasDiff() {
        for(Float diff : diffs.values()) {
            if(diff != 0) {
                return true;
            }
        }
        return false;
    }

    public void setShouldRequestLayout(boolean shouldRequestLayout) {
        this.shouldRequestLayout = shouldRequestLayout;
    }
}
