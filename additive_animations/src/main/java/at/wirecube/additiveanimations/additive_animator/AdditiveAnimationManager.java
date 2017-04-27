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

    static final PropertyAccumulator getAccumulatedProperties(View v) {
        return from(v).mAccumulator;
    }

    private final PropertyAccumulator mAccumulator = new PropertyAccumulator();

    private final View mAnimationTargetView;
    private final Set<AdditiveAnimationApplier> mAdditiveAnimationAppliers = new HashSet<>();
    private final Map<String, Float> mLastTargetValues = new HashMap<>();

    private AdditiveAnimationManager(View animationTarget) {
        mAnimationTargetView = animationTarget;
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

    void addAnimation(AdditiveAnimationApplier animationApplier, PropertyDescription property) {
        if(getLastTargetValue(property.getTag()) == null) {
            mAccumulator.getAccumulatedProperties().put(property, property.getStartValue());
        } else {
            property.setStartValue(mLastTargetValues.get(property.getTag()));
        }
        mLastTargetValues.put(property.getTag(), property.getTargetValue());
        animationApplier.addAnimatedProperty(property);
        // immediately add to our list of pending animators
        mAdditiveAnimationAppliers.add(animationApplier);
    }

    void onAnimationApplierEnd(AdditiveAnimationApplier applier) {
        mAdditiveAnimationAppliers.remove(applier);
        if (mAdditiveAnimationAppliers.isEmpty()) {
            sAnimators.remove(mAnimationTargetView);
        }
        mAccumulator.totalNumAnimationUpdaters--;
    }

    void onAnimationApplierStart(AdditiveAnimationApplier applier) {
        // only now are we expecting updates from this applier
        mAccumulator.totalNumAnimationUpdaters++;
    }

    void cancelAllAnimations() {
        for(AdditiveAnimationApplier additiveAnimationApplier : mAdditiveAnimationAppliers) {
            additiveAnimationApplier.cancel();
        }
        mAdditiveAnimationAppliers.clear();
        mLastTargetValues.clear();
        sAnimators.remove(mAnimationTargetView);
    }
}
