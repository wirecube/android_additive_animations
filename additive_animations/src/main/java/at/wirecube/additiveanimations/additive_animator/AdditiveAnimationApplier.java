package at.wirecube.additiveanimations.additive_animator;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class AdditiveAnimationApplier {
    private Map<PropertyDescription, Float> mLastValues = new HashMap<>();
    private ValueAnimator mAnimator = ValueAnimator.ofFloat(0f, 1f);
    private final Set<View> mTargetViews = new HashSet<>();

    AdditiveAnimationApplier(final AdditiveAnimator additiveAnimator) {
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                List<View> modifiedViews = new ArrayList<>();
                for (PropertyDescription property : mLastValues.keySet()) {
                    if(property.getView() != null) {
                        PropertyAccumulator tempProperties = AdditiveAnimationManager.getAccumulatedProperties(property.getView());
                        tempProperties.add(property, getDelta(property, animation.getAnimatedFraction()));
                        modifiedViews.add(property.getView());
                    }
                }
                for(View v : modifiedViews) {
                    PropertyAccumulator tempProperties = AdditiveAnimationManager.getAccumulatedProperties(v);
                    tempProperties.updateCounter += 1;
                    if (tempProperties.updateCounter >= tempProperties.totalNumAnimationUpdaters) {
                        additiveAnimator.applyChanges(tempProperties.getAccumulatedProperties(), v);
                        tempProperties.updateCounter = 0;
                    }
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
                if (!animationDidCancel) {
                    for(View v : mTargetViews) {
                        AdditiveAnimationManager.from(v).onAnimationApplierEnd(AdditiveAnimationApplier.this);
                    }
                }
            }

            @Override
            public void onAnimationStart(Animator animation) {
                for(View v : mTargetViews) {
                    AdditiveAnimationManager.from(v).onAnimationApplierStart(AdditiveAnimationApplier.this);
                }
            }
        });
    }

    void addAnimatedProperty(PropertyDescription property) {
        mLastValues.put(property, property.getStartValue());
        mTargetViews.add(property.getView());
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
