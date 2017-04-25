package at.wirecube.additiveanimations.additive_animator;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.util.Property;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A class that supports additive animations on PropertyDescription objects..
 * Additive animations are nicely explained here: http://ronnqvi.st/multiple-animations/
 */
class AdditiveAnimationApplier {

    private static final Map<View, AdditiveAnimationApplier> sAnimators = new HashMap<>();

    static final AdditiveAnimationApplier from(View targetView) {
        if(targetView == null) {
            return null;
        }
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
    static class AccumulatedProperties {
        Map<PropertyDescription, Float> tempProperties = new HashMap<>();
    }

    private AccumulatedProperties mAccumulatedLayoutParams = new AccumulatedProperties();

    private final View mAnimationTargetView;
    private final List<AdditiveAnimationHolder> mAdditiveAnimationHolders = new ArrayList<>();
    private final Map<String, Float> mLastTargetValues = new HashMap<>();
    private AdditiveAnimator mAnimationUpdater;

    // This must be given by AdditiveAnimator, which is also responsible for starting the animation.
    // This way, multiple AdditiveAnimators can share the same animator.
    private ValueAnimator mNextValueAnimator;
    private AdditiveAnimationHolder mNextAnimationHolder;

    private AdditiveAnimationApplier(View animationTarget) {
        mAnimationTargetView = animationTarget;
        mAccumulatedLayoutParams = new AccumulatedProperties();
    }

    void setAnimationUpdater(AdditiveAnimator animationUpdater) {
        this.mAnimationUpdater = animationUpdater;
    }

    Float getLastTargetValue(String propertyName) {
        return mLastTargetValues.get(propertyName);
    }

    Float getActualPropertyValue(Property<View, Float> property) {
        Float lastTarget = getLastTargetValue(property.getName());
        if(lastTarget == null) {
            lastTarget = property.get(mAnimationTargetView);
        }
        return lastTarget;
    }

    AdditiveAnimationApplier addAnimation(PropertyDescription propertyDescription) {
        if(mLastTargetValues.get(propertyDescription.getTag()) == null) {
            mAccumulatedLayoutParams.tempProperties.put(propertyDescription, propertyDescription.getStartValue());
        } else {
            propertyDescription.setStartValue(mLastTargetValues.get(propertyDescription.getTag()));
        }
        mLastTargetValues.put(propertyDescription.getTag(), propertyDescription.getTargetValue());

        if(mNextAnimationHolder != null) {
            mNextAnimationHolder.addAnimatedProperty(propertyDescription);
            return this;
        }

        mNextAnimationHolder = new AdditiveAnimationHolder(propertyDescription, mNextValueAnimator, mAnimationTargetView, mAnimationUpdater, mAccumulatedLayoutParams);
        final AdditiveAnimationHolder lastHolder = mNextAnimationHolder;

        for (AdditiveAnimationHolder animationHolder : mAdditiveAnimationHolders) {
            animationHolder.setShouldRequestLayout(false);
        }

        mNextValueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mAdditiveAnimationHolders.remove(lastHolder);
                if (mAdditiveAnimationHolders.isEmpty()) {
                    sAnimators.remove(mAnimationTargetView);
                } else {
                    // in case we finished before the previous animator, it must be allowed to continue updating the view:
                    mAdditiveAnimationHolders.get(mAdditiveAnimationHolders.size() - 1).setShouldRequestLayout(true);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                // This is only called when we cancel all animations, in which case we clear our animators anyway
                // By removing the listener, we ensure that our normal `onAnimationEnd` method isn't called.
                animation.removeListener(this);
            }
        });

        mNextAnimationHolder.setShouldRequestLayout(true);
        return this;
    }

    void onStart() {
        if(mNextValueAnimator != null) {
            mNextValueAnimator = null;
            mAdditiveAnimationHolders.add(mNextAnimationHolder);
            mNextAnimationHolder = null;
        }
    }

    void cancelAllAnimations() {
        for(AdditiveAnimationHolder additiveAnimationHolder : mAdditiveAnimationHolders) {
            additiveAnimationHolder.cancel();
        }
        mAdditiveAnimationHolders.clear();
        mLastTargetValues.clear();
        sAnimators.remove(mAnimationTargetView);
    }

    void setNextValueAnimator(ValueAnimator animator) {
        mNextValueAnimator = animator;
    }
}
