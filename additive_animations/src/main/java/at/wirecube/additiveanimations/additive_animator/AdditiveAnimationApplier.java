package at.wirecube.additiveanimations.additive_animator;

import android.animation.ValueAnimator;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

class AdditiveAnimationApplier {
    private Map<PropertyDescription, Float> mLastValues = new HashMap<>();
    private ValueAnimator mAnimator;
    private View mTargetView;

    AdditiveAnimationApplier(PropertyDescription description, ValueAnimator animator, final View animationTargetView, final AdditiveAnimator additiveAnimator, final AdditiveAnimationManager.AccumulatedProperties tempProperties) {
        addAnimatedProperty(description);
        mAnimator = animator;
        mTargetView = animationTargetView;
        tempProperties.totalNumAnimationUpdaters++;
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                for (PropertyDescription property : mLastValues.keySet()) {
                    tempProperties.tempProperties.put(property, tempProperties.tempProperties.get(property) + getDelta(property, animation.getAnimatedFraction()));
                }
                tempProperties.updateCounter += 1;
                if (tempProperties.updateCounter >= tempProperties.totalNumAnimationUpdaters) {
                    additiveAnimator.applyChanges(tempProperties.tempProperties, animationTargetView);
                    tempProperties.updateCounter = 0;
                }
            }
        });
    }

    void addAnimatedProperty(PropertyDescription propertyDescription) {
        mLastValues.put(propertyDescription, propertyDescription.getStartValue());
    }

    final View getView() {
        return mTargetView;
    }

    final float getDelta(PropertyDescription tag, float progress) {
        float lastVal = mLastValues.get(tag);
        float newVal = tag.evaluateAt(progress);
        float delta = newVal - lastVal;
        mLastValues.put(tag, newVal);
        return delta;
    }

    final void cancel() {
        mAnimator.cancel();
    }
}
