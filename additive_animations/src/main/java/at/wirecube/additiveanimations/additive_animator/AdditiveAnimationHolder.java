package at.wirecube.additiveanimations.additive_animator;

import android.animation.ValueAnimator;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

class AdditiveAnimationHolder {
    private Map<AdditivelyAnimatedPropertyDescription, Float> diffs = new HashMap<>();
    private Map<AdditivelyAnimatedPropertyDescription, Float> lastValues = new HashMap<>();
    private Map<AdditivelyAnimatedPropertyDescription, Float> targets = new HashMap<>();

    private boolean shouldRequestLayout = false;
    private View mAnimationTargetView;
    private ValueAnimator animator;
    private AdditiveAnimator updater;
    private AdditiveAnimationApplier.AccumulatedProperties mTempProperties;

    AdditiveAnimationHolder(AdditivelyAnimatedPropertyDescription description, ValueAnimator animator, View animationTargetView, AdditiveAnimator animationChangeApplier, AdditiveAnimationApplier.AccumulatedProperties tempProperties) {
        addAnimatedProperty(description);
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

    void addAnimatedProperty(AdditivelyAnimatedPropertyDescription propertyDescription) {
        diffs.put(propertyDescription, propertyDescription.getTargetValue() - propertyDescription.getStartValue());
        lastValues.put(propertyDescription, new Float(0));
        targets.put(propertyDescription, propertyDescription.getTargetValue());
    }

    final float getDelta(AdditivelyAnimatedPropertyDescription tag, float progress) {
        float diff = diffs.get(tag);
        float lastVal = lastValues.get(tag);
        float newVal;
        if(tag.getCustomTypeEvaluator() != null) {
            newVal = tag.getCustomTypeEvaluator().evaluate(progress, tag.getStartValue(), tag.getTargetValue()) - tag.getStartValue();
        } else {
            newVal = diff * progress;
        }
        float delta = newVal - lastVal;
        lastValues.put(tag, newVal);
        return delta;
    }

    final void cancel() {
        animator.cancel();
    }

    void setShouldRequestLayout(boolean shouldRequestLayout) {
        this.shouldRequestLayout = shouldRequestLayout;
    }
}
