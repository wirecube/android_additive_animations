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
 * A class that manages additive animations on PropertyDescription objects for a single view.
 * Additive animations are nicely explained here: http://ronnqvi.st/multiple-animations/
 */
class AdditiveAnimationManager {

    private static final Map<View, AdditiveAnimationManager> sAnimators = new HashMap<>();

    static final AdditiveAnimationManager from(View targetView) {
        if(targetView == null) {
            return null;
        }
        AdditiveAnimationManager animator = sAnimators.get(targetView);
        if (animator == null) {
            animator = new AdditiveAnimationManager(targetView);
            sAnimators.put(targetView, animator);
        }
        return animator;
    }

    /**
     * Helper class for accumulating the changes made by all of the additive animators.
     */
    static class AccumulatedProperties {
        Map<PropertyDescription, Float> tempProperties = new HashMap<>();
        int totalNumAnimationUpdaters = 0;
        int updateCounter = 0;
    }

    private AccumulatedProperties mAccumulatedLayoutParams = new AccumulatedProperties();

    private final View mAnimationTargetView;
    private final List<AdditiveAnimationApplier> mAdditiveAnimationAppliers = new ArrayList<>();
    private final Map<String, Float> mLastTargetValues = new HashMap<>();
    private AdditiveAnimator mAnimationUpdater;

    // This must be given by AdditiveAnimator, which is also responsible for starting the animation.
    // This way, multiple AdditiveAnimators can share the same AnimationManager (and vice versa).
    private ValueAnimator mNextValueAnimator;
    private AdditiveAnimationApplier mNextAnimationApplier;

    private AdditiveAnimationManager(View animationTarget) {
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

    void addAnimation(PropertyDescription property) {
        if(mLastTargetValues.get(property.getTag()) == null) {
            mAccumulatedLayoutParams.tempProperties.put(property, property.getStartValue());
        } else {
            property.setStartValue(mLastTargetValues.get(property.getTag()));
        }
        mLastTargetValues.put(property.getTag(), property.getTargetValue());

        if(mNextAnimationApplier != null) {
            mNextAnimationApplier.addAnimatedProperty(property);
        } else {
            mNextAnimationApplier = new AdditiveAnimationApplier(property, mNextValueAnimator, mAnimationTargetView, mAnimationUpdater, mAccumulatedLayoutParams);
            final AdditiveAnimationApplier lastHolder = mNextAnimationApplier;

            mNextValueAnimator.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mAdditiveAnimationAppliers.remove(lastHolder);
                    if (mAdditiveAnimationAppliers.isEmpty()) {
                        sAnimators.remove(mAnimationTargetView);
                    }
                    mAccumulatedLayoutParams.totalNumAnimationUpdaters--;
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                    // This is only called when we cancel all animations, in which case we clear our animators anyway
                    // By removing the listener, we ensure that our normal `onAnimationEnd` method isn't called.
                    animation.removeListener(this);
                }
            });
        }
    }

    /**
     * Returns the current animation applier object, resetting the builder state.
     * @return
     */
    AdditiveAnimationApplier getAnimationApplier() {
        AdditiveAnimationApplier applier = mNextAnimationApplier;
        mNextAnimationApplier = null;
        mNextValueAnimator = null;
        return applier;
    }

    void startAnimationApplier(AdditiveAnimationApplier applier) {
        mAdditiveAnimationAppliers.add(applier);
    }

    void onAnimationStart() {
        if(mNextValueAnimator != null && mNextAnimationApplier != null) {
            mAdditiveAnimationAppliers.add(mNextAnimationApplier);
            mNextValueAnimator = null;
            mNextAnimationApplier = null;
            mAnimationUpdater = null;
        }
    }

    void cancelAllAnimations() {
        for(AdditiveAnimationApplier additiveAnimationApplier : mAdditiveAnimationAppliers) {
            additiveAnimationApplier.cancel();
        }
        mAdditiveAnimationAppliers.clear();
        mLastTargetValues.clear();
        sAnimators.remove(mAnimationTargetView);
    }

    void setNextValueAnimator(ValueAnimator animator) {
        mNextValueAnimator = animator;
    }
}
