package at.wirecube.additiveanimations.additive_animator;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.view.View;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class AdditiveAnimationApplier {
    private Map<AdditiveAnimation, Float> mPreviousValues = new HashMap<>();
    private ValueAnimator mAnimator = ValueAnimator.ofFloat(0f, 1f);
    private final Map<View, Set<String>> mAnimatedPropertiesPerView = new HashMap<>();
    private boolean mHasInformedStateManagerAboutAnimationStart = false;
    private AdditiveAnimator mAdditiveAnimator;

    AdditiveAnimationApplier(AdditiveAnimator additiveAnimator) {
        mAdditiveAnimator = additiveAnimator;
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                notifyStateManagerAboutAnimationStartIfNeeded();
                List<View> modifiedViews = new ArrayList<>();
                for (AdditiveAnimation animation : mPreviousValues.keySet()) {
                    if (animation.getView() != null) {
                        AnimationAccumulator tempProperties = AdditiveAnimationStateManager.getAccumulatedProperties(animation.getView());
                        tempProperties.add(animation, getDelta(animation, valueAnimator.getAnimatedFraction()));
                        modifiedViews.add(animation.getView());
                    }
                }
                for (View v : modifiedViews) {
                    if (!mAnimatedPropertiesPerView.containsKey(v)) {
                        continue;
                    }
                    AnimationAccumulator accumulator = AdditiveAnimationStateManager.getAccumulatedProperties(v);
//                    accumulator.updateCounter += 1;
//                    if (accumulator.updateCounter >= AdditiveAnimationStateManager.from(v).mAdditiveAnimationAppliers.size()) {
                    // TODO: figure out a way to make this smoother
                        mAdditiveAnimator.applyChanges(accumulator.getAccumulatedProperties(), v);
//                        accumulator.updateCounter = 0;
//                    }
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
                // now we are actually done
                for (View v : mAnimatedPropertiesPerView.keySet()) {
                    AdditiveAnimationStateManager.from(v).onAnimationApplierEnd(AdditiveAnimationApplier.this);
                }
            }

            @Override
            public void onAnimationStart(Animator animation) {
                notifyStateManagerAboutAnimationStartIfNeeded();
            }
        });
    }

    AdditiveAnimator getAdditiveAnimator() {
        return mAdditiveAnimator;
    }

    private void notifyStateManagerAboutAnimationStartIfNeeded() {
        if(!mHasInformedStateManagerAboutAnimationStart) {
            for (View v : mAnimatedPropertiesPerView.keySet()) {
                AdditiveAnimationStateManager manager = AdditiveAnimationStateManager.from(v);
                manager.onAnimationApplierStart(AdditiveAnimationApplier.this);
                for(AdditiveAnimation animation : getAnimations(v)) {
                    manager.prepareAnimationStart(animation);
                    mPreviousValues.put(animation, animation.getStartValue());
                }
            }
            mHasInformedStateManagerAboutAnimationStart = true;
        }
    }

    void addAnimation(AdditiveAnimation animation) {
        // the correct value will be set when the animation actually starts instead of when we add the animation.
        mPreviousValues.put(animation, 0f);
        addTarget(animation.getView(), animation.getTag());
    }

    boolean removeAnimation(String animatedPropertyName, View v) {
        return removeTarget(v, animatedPropertyName);
    }

    private void addTarget(View v, String animationTag) {
        Set<String> animations = mAnimatedPropertiesPerView.get(v);
        if(animations == null) {
            animations = new HashSet<>();
            mAnimatedPropertiesPerView.put(v, animations);
        }
        animations.add(animationTag);
    }

    private void removeTarget(View v) {
        if(mAnimatedPropertiesPerView.get(v) == null) {
            return;
        }
        for(String animatedValue : mAnimatedPropertiesPerView.get(v)) {
            removeTarget(v, animatedValue);
        }
    }

    /**
     * Removes the animation with the given name from the given view.
     * Returns true if this removed all animations from the view, false if there are still more animations running.
     */
    private boolean removeTarget(View v, String additiveAnimationName) {
        AdditiveAnimation animationToRemove = null;
        for(AdditiveAnimation anim : mPreviousValues.keySet()) {
            if(anim.getView() == v && anim.getTag() == additiveAnimationName) {
                animationToRemove = anim;
                break;
            }
        }
        if(animationToRemove != null) {
            mPreviousValues.remove(animationToRemove);
        }

        Set<String> animations = mAnimatedPropertiesPerView.get(v);
        if(animations == null) {
            return true;
        }
        animations.remove(additiveAnimationName);
        if(animations.isEmpty()) {
            mAnimatedPropertiesPerView.remove(v);
            return true;
        }
        return false;
    }

    Collection<AdditiveAnimation> getAnimations(View v) {
        List<AdditiveAnimation> animations = new ArrayList<>();
        for(AdditiveAnimation animation : mPreviousValues.keySet()) {
            if(animation.getView() == v) {
                animations.add(animation);
            }
        }
        return animations;
    }

    ValueAnimator getAnimator() {
        return mAnimator;
    }

    final float getDelta(AdditiveAnimation animation, float progress) {
        float lastVal = mPreviousValues.get(animation);
        float newVal = animation.evaluateAt(progress);
        float delta = newVal - lastVal;
        mPreviousValues.put(animation, newVal);
        return delta;
    }

    /**
     * Remove all properties belonging to `v`.
     */
    final void cancel(View v) {
        if(mAnimatedPropertiesPerView.containsKey(v) && mAnimatedPropertiesPerView.size() == 1) {
            cancel();
        } else {
            removeTarget(v);
        }
    }

    final void cancel() {
        mAnimator.cancel();
    }
}
