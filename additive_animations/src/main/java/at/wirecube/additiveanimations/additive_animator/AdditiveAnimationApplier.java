package at.wirecube.additiveanimations.additive_animator;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.view.View;

import java.util.HashMap;
import java.util.Map;

class AdditiveAnimationApplier {
    private Map<PropertyDescription, Float> mLastValues = new HashMap<>();
    private ValueAnimator mAnimator = ValueAnimator.ofFloat(0f, 1f);
    private View mTargetView;

    AdditiveAnimationApplier(final View targetView, final AdditiveAnimator additiveAnimator) {
        mTargetView = targetView;
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                PropertyAccumulator tempProperties = AdditiveAnimationManager.getAccumulatedProperties(mTargetView);
                for (PropertyDescription property : mLastValues.keySet()) {
                    tempProperties.add(property, getDelta(property, animation.getAnimatedFraction()));
                }
                tempProperties.updateCounter += 1;
                if (tempProperties.updateCounter >= tempProperties.totalNumAnimationUpdaters) {
                    additiveAnimator.applyChanges(tempProperties.getAccumulatedProperties(), targetView);
                    tempProperties.updateCounter = 0;
                }
            }
        });

        mAnimator.addListener(new AnimatorListenerAdapter() {
            boolean animationDidCancel = false;
            @Override
            public void onAnimationCancel(Animator animation) {
                animationDidCancel = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if(!animationDidCancel) {
                    AdditiveAnimationManager.from(mTargetView).onAnimationApplierEnd(AdditiveAnimationApplier.this);
                }
            }

            @Override
            public void onAnimationStart(Animator animation) {
                AdditiveAnimationManager.from(mTargetView).onAnimationApplierStart(AdditiveAnimationApplier.this);
            }
        });
    }

    void addAnimatedProperty(PropertyDescription propertyDescription) {
        mLastValues.put(propertyDescription, propertyDescription.getStartValue());
    }

    final View getView() {
        return mTargetView;
    }

    ValueAnimator getAnimator() {
        return mAnimator;
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
