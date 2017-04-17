package at.wirecube.additiveanimations.additiveanimationsdemo.animation.additive_animations.base;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A class that supports additive animations on all properties, provided you create a AdditiveAnimator.
 * Additive animations are nicely explained here: http://ronnqvi.st/multiple-animations/
 * We have to use an additive animation when moving the user location view, because its position
 * is being updated all the time, before the previous animation is allowed to finish, resulting in
 * abrupt changes in velocity and direction.
 */
public class AdditiveAnimationApplier {

    private static final Map<View, AdditiveAnimationApplier> sAnimators = new HashMap<>();

    public static final AdditiveAnimationApplier from(View targetView) {
        AdditiveAnimationApplier animator = sAnimators.get(targetView);
        if (animator == null) {
            animator = new AdditiveAnimationApplier(targetView);
            sAnimators.put(targetView, animator);
        }
        return animator;
    }

    /**
     * Helper class for accumulating the changes made by all of the additive animators.
     */
    public static class AccumulatedProperties {
        Map<AdditivelyAnimatedPropertyDescription, Float> tempProperties = new HashMap<>();
    }

    private AccumulatedProperties mAccumulatedLayoutParams = new AccumulatedProperties();

    private final View mAnimationTargetView;
    private final List<AdditiveAnimationHolder> mAdditiveAnimationHolders = new ArrayList<>();
    private final Map<String, Float> mLastTargetValues = new HashMap<>();
    private AdditiveAnimator mAnimationUpdater;

    private ValueAnimator mUnstartedAnimator;
    private AdditiveAnimationHolder mUnstartedAnimationHolder;

    private AdditiveAnimationApplier(View animationTarget) {
        mAnimationTargetView = animationTarget;
        mAccumulatedLayoutParams = new AccumulatedProperties();
    }

    public void setAnimationUpdater(AdditiveAnimator animationUpdater) {
        this.mAnimationUpdater = animationUpdater;
    }

    public Float getLastTargetValue(String propertyName) {
        return mLastTargetValues.get(propertyName);
    }

    public AdditiveAnimationApplier addAnimation(AdditivelyAnimatedPropertyDescription propertyDescription) {
        if(mLastTargetValues.get(propertyDescription.getTag()) == null) {
            mAccumulatedLayoutParams.tempProperties.put(propertyDescription, propertyDescription.getStartValue());
        } else {
            propertyDescription.setStartValue(mLastTargetValues.get(propertyDescription.getTag()));
        }
        mLastTargetValues.put(propertyDescription.getTag(), propertyDescription.getTargetValue());

        if(mUnstartedAnimator == null) {
            mUnstartedAnimator = ValueAnimator.ofFloat(0, 1);
        }
        if(mUnstartedAnimationHolder != null) {
            mUnstartedAnimationHolder.addAnimatedProperty(propertyDescription);
            return this;
        }

        mUnstartedAnimationHolder = new AdditiveAnimationHolder(propertyDescription, mUnstartedAnimator, mAnimationTargetView, mAnimationUpdater, mAccumulatedLayoutParams);
        final AdditiveAnimationHolder lastHolder = mUnstartedAnimationHolder;

        for (AdditiveAnimationHolder animationHolder : mAdditiveAnimationHolders) {
            animationHolder.setShouldRequestLayout(false);
        }

        mUnstartedAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                animation.removeAllListeners();
                mAdditiveAnimationHolders.remove(lastHolder);
                if (mAdditiveAnimationHolders.isEmpty()) {
                    sAnimators.remove(mAnimationTargetView);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                // This is only called when we cancel all animations, in which case we clear our animators anyway
                // By removing the listeners, we ensure that our normal `onAnimationEnd` method isn't called.
                animation.removeAllListeners();
            }

            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });

        mUnstartedAnimationHolder.setShouldRequestLayout(true);
        return this;
    }

    public void start() {
        if(mUnstartedAnimator != null) {
            mUnstartedAnimator.setDuration(mAnimationUpdater.getDuration());
            mUnstartedAnimator.setInterpolator(mAnimationUpdater.getInterpolator());
            mUnstartedAnimator.start();
            mUnstartedAnimator = null;
            mAdditiveAnimationHolders.add(mUnstartedAnimationHolder);
            mUnstartedAnimationHolder = null;
        }
    }

    public void cancelAllAnimations() {
        for(AdditiveAnimationHolder additiveAnimationHolder : mAdditiveAnimationHolders) {
            additiveAnimationHolder.cancel();
        }
        mAdditiveAnimationHolders.clear();
        sAnimators.remove(mAnimationTargetView);
    }

}
