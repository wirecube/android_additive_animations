package at.wirecube.additiveanimations.additive_animator;

import android.util.Property;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * A class that manages internal values about the state of all running additive animations for a single view.
 */
class AdditiveAnimationStateManager {

    private static final Map<View, AdditiveAnimationStateManager> sStateManagers = new HashMap<>();

    static final AdditiveAnimationStateManager from(View targetView) {
        if(targetView == null) {
            return null;
        }
        AdditiveAnimationStateManager animator = sStateManagers.get(targetView);
        if (animator == null) {
            animator = new AdditiveAnimationStateManager(targetView);
            sStateManagers.put(targetView, animator);
        }
        return animator;
    }

    static final AnimationAccumulator getAccumulatedProperties(View v) {
        return from(v).mAccumulator;
    }

    private final AnimationAccumulator mAccumulator = new AnimationAccumulator();

    private final View mAnimationTargetView;
    // sorted: last = newest
    final Set<AdditiveAnimationApplier> mAdditiveAnimationAppliers = new HashSet<>();
    private final Map<String, Integer> mNumAnimationsPerTag = new HashMap<>();
    private final Map<String, Float> mLastTargetValues = new HashMap<>();
    private final Map<String, Float> mQueuedTargetValues = new HashMap<>();

    private AdditiveAnimationStateManager(View animationTarget) {
        mAnimationTargetView = animationTarget;
    }

    void addAnimation(AdditiveAnimationApplier animationApplier, AdditiveAnimation animation) {
        // immediately add to our list of pending animators
        mAdditiveAnimationAppliers.add(animationApplier);
        animationApplier.addAnimation(animation);
        mQueuedTargetValues.put(animation.getTag(), animation.getTargetValue());
    }

    void onAnimationApplierEnd(AdditiveAnimationApplier applier) {
        mAdditiveAnimationAppliers.remove(applier);
        if (mAdditiveAnimationAppliers.isEmpty()) {
            sStateManagers.remove(mAnimationTargetView);
        }
        mAccumulator.totalNumAnimationUpdaters--;

        for(AdditiveAnimation animation : applier.getAnimations(mAnimationTargetView)) {
            decrementNumAnimations(animation.getTag());
        }

//        if(mAccumulator.updateCounter == mAdditiveAnimationAppliers.size()) {
//            applier.getAdditiveAnimator().applyChanges(mAccumulator.getAccumulatedProperties(), mAnimationTargetView);
//            mAccumulator.updateCounter = 0;
//        }
//        if(mAccumulator.updateCounter > mAccumulator.totalNumAnimationUpdaters) {
//            System.out.println("wat");
//        }
    }

    void onAnimationApplierStart(AdditiveAnimationApplier applier) {
        // only now are we expecting updates from this applier
        mAccumulator.totalNumAnimationUpdaters++;
    }

    /**
     * Updates {@link at.wirecube.additiveanimations.additive_animator.AdditiveAnimation#mStartValue}
     * to the last value that was specified as a target. This is only relevant when chaining or reusing animations,
     * since the state of the object might have changed since the animation was created.
     * This will also update the accumulator if it doesn't already contain an entry for this animation,
     * using the current property value (if a Property is available)
     */
    void prepareAnimationStart(AdditiveAnimation animation) {
        if(getLastTargetValue(animation.getTag()) == null || numRunningAnimations(animation.getTag()) == 0) {
            // In case we don't currently have an animation on this property, let's make sure
            // the start value matches the current model value:
            Float currentModelValue = getActualAnimationStartValue(animation);
            if(currentModelValue != null) {
                animation.setStartValue(currentModelValue);
            }
            mAccumulator.getAccumulatedProperties().put(animation, animation.getStartValue());
        } else {
            animation.setStartValue(getLastTargetValue(animation.getTag()));
        }
        incrementNumAnimations(animation.getTag());
        mLastTargetValues.put(animation.getTag(), animation.getTargetValue());
    }

    void cancelAllAnimations() {
        for(AdditiveAnimationApplier additiveAnimationApplier : mAdditiveAnimationAppliers) {
            additiveAnimationApplier.cancel(mAnimationTargetView);
        }
        mAdditiveAnimationAppliers.clear();
        mLastTargetValues.clear();
        mQueuedTargetValues.clear();
        mNumAnimationsPerTag.clear();
        sStateManagers.remove(mAnimationTargetView);
    }

    void cancelAnimation(String propertyName) {
        List<AdditiveAnimationApplier> cancelledAppliers = new ArrayList<>();
        for(AdditiveAnimationApplier applier : mAdditiveAnimationAppliers) {
            if(applier.removeAnimation(propertyName, mAnimationTargetView)) {
                cancelledAppliers.add(applier);
            }
        }
        mLastTargetValues.remove(propertyName);
        mQueuedTargetValues.remove(propertyName);
        mNumAnimationsPerTag.remove(propertyName);
        mAdditiveAnimationAppliers.removeAll(cancelledAppliers);
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

    Float getQueuedPropertyValue(String propertyName) {
        return mQueuedTargetValues.get(propertyName);
    }

    private Float getActualAnimationStartValue(AdditiveAnimation animation) {
        if(animation.getProperty() != null) {
            return getActualPropertyValue(animation.getProperty());
        } else {
            // TODO: there should be a way for subclasses to implement the 'getting' of a custom value.
            return null;
        }
    }

    private void incrementNumAnimations(String tag) {
        Integer numAnimations = mNumAnimationsPerTag.get(tag);
        if(numAnimations == null) {
            numAnimations = 1;
        } else {
            numAnimations++;
        }
        mNumAnimationsPerTag.put(tag, numAnimations);
    }

    private void decrementNumAnimations(String tag) {
        Integer numAnimations = mNumAnimationsPerTag.get(tag);
        if(numAnimations == null) {
            return;
        } else {
            numAnimations = Math.max(numAnimations - 1, 0);
        }
        mNumAnimationsPerTag.put(tag, numAnimations);
    }

    private int numRunningAnimations(String tag) {
        Integer numAnimations = mNumAnimationsPerTag.get(tag);
        if(numAnimations != null) {
            return numAnimations;
        } else {
            return 0;
        }
    }

}
