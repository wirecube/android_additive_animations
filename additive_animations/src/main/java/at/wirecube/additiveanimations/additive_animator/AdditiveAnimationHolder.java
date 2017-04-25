package at.wirecube.additiveanimations.additive_animator;

import android.animation.ValueAnimator;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

class AdditiveAnimationHolder {
    private Map<PropertyDescription, Float> diffs = new HashMap<>();
    private Map<PropertyDescription, Float> lastValues = new HashMap<>();

    private boolean shouldRequestLayout = false;
    private View mAnimationTargetView;
    private ValueAnimator animator;
    private AdditiveAnimator updater;
    private AdditiveAnimationApplier.AccumulatedProperties mTempProperties;

    AdditiveAnimationHolder(PropertyDescription description, ValueAnimator animator, View animationTargetView, AdditiveAnimator animationChangeApplier, AdditiveAnimationApplier.AccumulatedProperties tempProperties) {
        addAnimatedProperty(description);
        this.animator = animator;
        this.mAnimationTargetView = animationTargetView;
        this.updater = animationChangeApplier;
        this.mTempProperties = tempProperties;
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                AdditiveAnimationApplier.AccumulatedProperties params = mTempProperties;
                for (PropertyDescription tag : diffs.keySet()) {
                    params.tempProperties.put(tag, params.tempProperties.get(tag) + getDelta(tag, animation.getAnimatedFraction()));
                }
                if (shouldRequestLayout) {
                    updater.applyChanges(params.tempProperties, mAnimationTargetView);
                }
            }
        });
    }

    void addAnimatedProperty(PropertyDescription propertyDescription) {
        diffs.put(propertyDescription, propertyDescription.getTargetValue() - propertyDescription.getStartValue());
        lastValues.put(propertyDescription, propertyDescription.getStartValue());
    }

    final float getDelta(PropertyDescription tag, float progress) {
        float diff = diffs.get(tag);
        float lastVal = lastValues.get(tag);
        float newVal;
        if(tag.getCustomTypeEvaluator() != null) {
            if(tag.getPath() != null) {
                newVal = (float) tag.getCustomTypeEvaluator().evaluate(progress, tag.getPathMode(), tag.getPath());
            } else {
                newVal = (float) tag.getCustomTypeEvaluator().evaluate(progress, tag.getStartValue(), tag.getTargetValue());
            }
        } else {
            newVal = tag.getStartValue() + diff * progress;
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
