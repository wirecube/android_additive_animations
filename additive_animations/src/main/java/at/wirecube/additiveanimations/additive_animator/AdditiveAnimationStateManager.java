/*
 *  Copyright 2017 David Ganster
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

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
class AdditiveAnimationStateManager<T> {

    private class AnimationInfo {
        int numAnimations = 0;
        Float lastTargetValue = null;
        Float queuedTargetValue = null;
    }

    private static final Map<Object, AdditiveAnimationStateManager> sStateManagers = new HashMap<>();

    static final AdditiveAnimationStateManager from(Object targetView) {
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

    static final AccumulatedAnimationValueManager getAccumulatedProperties(View v) {
        return from(v).mAccumulator;
    }

    private final AccumulatedAnimationValueManager mAccumulator = new AccumulatedAnimationValueManager();

    private final T mAnimationTargetView;
    private boolean mUseHardwareLayer = false;

    final Set<AdditiveAnimationAccumulator> mAdditiveAnimationAccumulators = new HashSet<>();

    private final Map<String, AnimationInfo> mAnimationInfos = new HashMap<>();

    private AdditiveAnimationStateManager(T animationTarget) {
        mAnimationTargetView = animationTarget;
    }

    private AnimationInfo getAnimationInfo(String tag, boolean addIfNeeded) {
        AnimationInfo info = mAnimationInfos.get(tag);
        if(info == null && addIfNeeded) {
            info = new AnimationInfo();
            mAnimationInfos.put(tag, info);
        }
        return info;
    }

    void addAnimation(AdditiveAnimationAccumulator animationApplier, AdditiveAnimation animation) {
        // immediately add to our list of pending animators
        mAdditiveAnimationAccumulators.add(animationApplier);
        animationApplier.addAnimation(animation);
        getAnimationInfo(animation.getTag(), true).queuedTargetValue = animation.getTargetValue();
    }

    void onAnimationApplierEnd(AdditiveAnimationAccumulator applier, boolean didCancel) {
        if(didCancel) {
            return;
        }
        mAdditiveAnimationAccumulators.remove(applier);
        removeStateManagerIfAccumulatorSetIsEmpty();

        for(AdditiveAnimation animation : applier.getAnimations(mAnimationTargetView)) {
            AnimationInfo info = getAnimationInfo(animation.getTag(), false);
            info.numAnimations = Math.max(info.numAnimations - 1, 0);
        }
    }

    void onAnimationApplierStart(AdditiveAnimationAccumulator applier) {
        // only now are we expecting updates from this applier
        if(mUseHardwareLayer && mAnimationTargetView instanceof View) {
            if(((View)mAnimationTargetView).getLayerType() != View.LAYER_TYPE_HARDWARE) {
                ((View)mAnimationTargetView).setLayerType(View.LAYER_TYPE_HARDWARE, null);
            }
        }
    }

    /**
     * Updates {@link at.wirecube.additiveanimations.additive_animator.AdditiveAnimation#mStartValue}
     * to the last value that was specified as a target. This is only relevant when chaining or reusing animations,
     * since the state of the object might have changed since the animation was created.
     * This will also update the accumulator if it doesn't already contain an entry for this animation,
     * using the current property value (if a Property is available)
     */
    void prepareAnimationStart(AdditiveAnimation animation) {
        // TODO: can we speed up this lookup?
        AccumulatedAnimationValue av = mAccumulator.getAccumulatedAnimationValue(animation);

        AnimationInfo info = getAnimationInfo(animation.getTag(), true);
        if(getLastTargetValue(animation.getTag()) == null || info.numAnimations == 0) {
            // In case we don't currently have an animation on this property, let's make sure
            // the start value matches the current model value:
            Float currentModelValue = getActualAnimationStartValue(animation);
            if(currentModelValue != null) {
                animation.setStartValue(currentModelValue);
            }
            av.tempValue = animation.getStartValue();
        } else {
            animation.setStartValue(getLastTargetValue(animation.getTag()));
        }
        animation.setAccumulatedValue(av);
        info.numAnimations++;
        info.lastTargetValue = animation.getTargetValue();
    }

    void cancelAllAnimations() {
        for(AdditiveAnimationAccumulator additiveAnimationAccumulator : mAdditiveAnimationAccumulators) {
            additiveAnimationAccumulator.cancel(mAnimationTargetView);
        }
        mAdditiveAnimationAccumulators.clear();
        mAnimationInfos.clear();
        sStateManagers.remove(mAnimationTargetView);
        // reset hardware layer
        if(mUseHardwareLayer && mAnimationTargetView instanceof View) {
            ((View)mAnimationTargetView).setLayerType(View.LAYER_TYPE_NONE, null);
        }
    }

    void cancelAnimation(String propertyName) {
        List<AdditiveAnimationAccumulator> cancelledAppliers = new ArrayList<>();
        for(AdditiveAnimationAccumulator applier : mAdditiveAnimationAccumulators) {
            if(applier.removeAnimation(propertyName, mAnimationTargetView)) {
                cancelledAppliers.add(applier);
            }
        }
        removeStateManagerIfAccumulatorSetIsEmpty();
        mAnimationInfos.remove(propertyName);
        mAdditiveAnimationAccumulators.removeAll(cancelledAppliers);
    }

    private void removeStateManagerIfAccumulatorSetIsEmpty() {
        if (mAdditiveAnimationAccumulators.isEmpty()) {
            sStateManagers.remove(mAnimationTargetView);
            // reset hardware layer
            if(mUseHardwareLayer && mAnimationTargetView instanceof View) {
                ((View)mAnimationTargetView).setLayerType(View.LAYER_TYPE_NONE, null);
            }
        }
    }

    Float getLastTargetValue(String propertyName) {
        AnimationInfo info = getAnimationInfo(propertyName, false);
        if(info == null) {
            return null;
        }
        return info.lastTargetValue;
    }

    Float getActualPropertyValue(Property<T, Float> property) {
        Float lastTarget = getLastTargetValue(property.getName());
        if(lastTarget == null) {
            lastTarget = property.get(mAnimationTargetView);
        }
        return lastTarget;
    }

    Float getQueuedPropertyValue(String propertyName) {
        AnimationInfo info = getAnimationInfo(propertyName, false);
        if(info == null) {
            return null;
        }
        return info.queuedTargetValue;
    }

    private Float getActualAnimationStartValue(AdditiveAnimation animation) {
        if(animation.getProperty() != null) {
            return getActualPropertyValue(animation.getProperty());
        } else {
            // TODO: there should be a way for subclasses to implement the 'getting' of a custom value.
            return null;
        }
    }

    void setUseHardwareLayer(boolean useHardwareLayer) {
        mUseHardwareLayer = useHardwareLayer;
    }

}
