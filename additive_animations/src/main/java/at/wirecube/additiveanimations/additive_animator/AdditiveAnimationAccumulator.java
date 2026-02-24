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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is the class that actually runs the ValueAnimator and adds the delta of each animation to its accumulator.
 */
public class AdditiveAnimationAccumulator {

    // Exists only for performance reasons to avoid map lookups
    private static final class AdditiveAnimationWrapper<T> {
        private final AdditiveAnimation<T> animation;
        private float previousValue;

        AdditiveAnimationWrapper(AdditiveAnimation<T> animation) {
            this.animation = animation;
            this.previousValue = 0f;
        }

        @Override
        public int hashCode() {
            return animation.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            return animation.equals(((AdditiveAnimationWrapper) obj).animation);
        }
    }

    private List<AdditiveAnimationWrapper> mAnimationWrappers = new ArrayList<>();
    private Map<Object, Set<AdditiveAnimationWrapper>> mAnimationsPerObject = new HashMap<>();
    private ValueAnimator mAnimator = null;
    private boolean mHasInformedStateManagerAboutAnimationStart = false;
    private BaseAdditiveAnimator mAdditiveAnimator;

    AdditiveAnimationAccumulator(BaseAdditiveAnimator additiveAnimator) {
        mAnimator = ValueAnimator.ofFloat(0f, 1f);
        mAdditiveAnimator = additiveAnimator;
        // it's better not to allocate once every frame, so we just create the list once and then clear() it.
        final List<AccumulatedAnimationValue> accumulatedAnimationValues = new ArrayList<>();
        mAnimator.addUpdateListener(valueAnimator -> {
            if (!mHasInformedStateManagerAboutAnimationStart) {
                notifyStateManagerAboutAnimationStartIfNeeded();
            }
            for (AdditiveAnimationWrapper animationWrapper : mAnimationWrappers) {
                AdditiveAnimation animation = animationWrapper.animation;
                AccumulatedAnimationValue tempProperties = animation.getAccumulatedValue();
                tempProperties.addDelta(getDelta(animationWrapper, valueAnimator.getAnimatedFraction()));
                accumulatedAnimationValues.add(tempProperties);
            }

            /*
             * TODO: is there some way to figure out whether or not to apply the changes?
             * Only the very last running accumulator would need to apply the changes, since the
             * accumulation happens inside the accumulatedAnimationValues.
             * This would bring a big performance gain in that the property setters would not need to
             * be called for every running animation, but only for the final accumulated value.
             * Unfortunately, it's really hard to gather a list of all accumulated values that need
             * to be set in each frame and to figure out when the last animator is done.
             */
            mAdditiveAnimator.applyChanges(accumulatedAnimationValues);

            // clear() does not resize the underlying elementData memory, so each subsequent frame will be able to reuse the previously allocated slots.
            accumulatedAnimationValues.clear();
        });

        mAnimator.addListener(new AnimatorListenerAdapter() {
            boolean animationDidCancel = false;

            @Override
            public void onAnimationCancel(Animator animation) {
                animationDidCancel = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                for (Object v : mAnimationsPerObject.keySet()) {
                    RunningAnimationsManager.from(v).onAnimationAccumulatorEnd(AdditiveAnimationAccumulator.this, animationDidCancel);
                }
            }

            @Override
            public void onAnimationStart(Animator animation) {
                notifyStateManagerAboutAnimationStartIfNeeded();
            }
        });
    }

    private void notifyStateManagerAboutAnimationStartIfNeeded() {
        if (mHasInformedStateManagerAboutAnimationStart) {
            return;
        }
        mHasInformedStateManagerAboutAnimationStart = true;

        // If any animation uses spring timing, configure the ValueAnimator accordingly.
        // Since all animations in one accumulator share the same timing, we just check the first one.
        configureForSpringTimingIfNeeded();

        Collection<Object> animationTargets = new ArrayList<>(mAnimationsPerObject.keySet());
        for (Object v : animationTargets) {
            RunningAnimationsManager manager = RunningAnimationsManager.from(v);
            manager.onAnimationAccumulatorStart(AdditiveAnimationAccumulator.this);
            for (AdditiveAnimationWrapper wrapper : getAnimationWrappers(v)) {
                manager.prepareAnimationStart(wrapper.animation);
                wrapper.previousValue = wrapper.animation.getStartValue();
            }
        }
    }

    void addAnimation(AdditiveAnimation animation) {
        // the correct value will be set when the animation actually starts instead of when we add the animation.
        AdditiveAnimationWrapper wrapper = new AdditiveAnimationWrapper(animation);
        mAnimationWrappers.add(wrapper);

        // TODO: speed this up
        addToAnimationMap(wrapper);
    }

    /**
     * If any animation in this accumulator uses spring timing, configure the ValueAnimator
     * to use a linear interpolator and the spring's settling duration.
     * This ensures that getAnimatedFraction() returns a linear time fraction that
     * the spring solver can convert to elapsed seconds.
     */
    private void configureForSpringTimingIfNeeded() {
        if (mAnimationWrappers.isEmpty()) return;

        // All animations share the same timing, so check the first one.
        AdditiveAnimation firstAnimation = mAnimationWrappers.get(0).animation;
        long settlingDuration = firstAnimation.getSpringSettlingDurationMs();

        if (settlingDuration > 0) {
            mAnimator.setInterpolator(new LinearInterpolator());
            mAnimator.setDuration(settlingDuration);
        }
    }

    /*
     * Returns true if this removed all animations from the object, false if there are still more animations running.
     */
    boolean removeAnimation(String animatedPropertyName, Object v) {
        removeAnimationFromTarget(v, animatedPropertyName);
        Collection c = mAnimationsPerObject.get(v);
        return c == null || c.size() == 0;
    }

    private void removeTarget(Object v) {
        Set<String> animatedValues = collectAnimatedProperties(v);
        if (animatedValues == null) {
            return;
        }
        if (animatedValues.size() == mAnimationWrappers.size()) {
            cancel();
        } else {
            for (String animatedValue : animatedValues) {
                removeAnimationFromTarget(v, animatedValue);
            }
        }
    }

    private Set<String> collectAnimatedProperties(Object v) {
        Collection<AdditiveAnimationWrapper> wrappers = mAnimationsPerObject.get(v);
        if (wrappers == null) {
            return new HashSet<>();
        }
        Set<String> properties = new HashSet<>(2);
        for (AdditiveAnimationWrapper wrapper : wrappers) {
            properties.add(wrapper.animation.getTag());
        }
        return properties;
    }

    /**
     * Removes the animation with the given name from the given object.
     */
    private void removeAnimationFromTarget(Object v, String additiveAnimationName) {
        AdditiveAnimationWrapper animationToRemove = null;
        for (AdditiveAnimationWrapper anim : getAnimationWrappers(v)) {
            if (anim.animation.getTag().equals(additiveAnimationName)) {
                animationToRemove = anim;
                break;
            }
        }
        if (animationToRemove != null) {
            mAnimationWrappers.remove(animationToRemove);
            removeFromAnimationMap(animationToRemove);
        }
    }

    private void addToAnimationMap(AdditiveAnimationWrapper wrapper) {
        Set<AdditiveAnimationWrapper> animations = mAnimationsPerObject.get(wrapper.animation.getTarget());
        if (animations == null) {
            animations = new HashSet<>(1);
            mAnimationsPerObject.put(wrapper.animation.getTarget(), animations);
        }
        animations.add(wrapper);
    }

    private void removeFromAnimationMap(AdditiveAnimationWrapper wrapper) {
        Set<AdditiveAnimationWrapper> animations = mAnimationsPerObject.get(wrapper.animation.getTarget());
        if (animations == null) {
            return;
        }
        animations.remove(wrapper);
        if (animations.size() == 0) {
            mAnimationsPerObject.remove(wrapper.animation.getTarget());
        }
    }

    Collection<AdditiveAnimation> getAnimations(Object v) {
        Collection<AdditiveAnimationWrapper> wrappers = getAnimationWrappers(v);
        List<AdditiveAnimation> animations = new ArrayList<>(wrappers.size());
        for (AdditiveAnimationWrapper wrapper : wrappers) {
            animations.add(wrapper.animation);
        }
        return animations;
    }

    private Collection<AdditiveAnimationWrapper> getAnimationWrappers(Object v) {
        Set<AdditiveAnimationWrapper> wrappers = mAnimationsPerObject.get(v);
        if (wrappers == null) {
            return new HashSet<>();
        }
        return wrappers;
    }

    ValueAnimator getAnimator() {
        return mAnimator;
    }

    Collection<AdditiveAnimation> getAnimations() {
        Set<AdditiveAnimation> allAnimations = new HashSet<>(mAnimationWrappers.size());
        for (AdditiveAnimationWrapper wrapper : mAnimationWrappers) {
            allAnimations.add(wrapper.animation);
        }
        return allAnimations;
    }

    final float getDelta(AdditiveAnimationWrapper wrapper, float progress) {
        float lastVal = wrapper.previousValue;
        AdditiveAnimation animation = wrapper.animation;
        float newVal = animation.evaluateAt(progress);
        float delta = newVal - lastVal;
        wrapper.previousValue = newVal;
        return delta;
    }

    /**
     * Remove all properties belonging to `v`.
     */
    final void cancel(Object v) {
        removeTarget(v);
    }

    final void cancel() {
        mAnimator.cancel();
    }

    @Override
    public int hashCode() {
        return mAnimator.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        AdditiveAnimationAccumulator other = (AdditiveAnimationAccumulator) obj;
        return other.mAnimator == mAnimator;
    }
}
