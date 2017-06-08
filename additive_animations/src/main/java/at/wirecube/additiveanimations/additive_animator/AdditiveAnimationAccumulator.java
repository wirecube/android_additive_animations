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
import android.view.View;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class AdditiveAnimationAccumulator {
    private Map<AdditiveAnimation, Float> mPreviousValues = new HashMap<>();
    private Map<View, Set<AdditiveAnimation>> mAnimationsPerView = new HashMap<>();
    private final Map<View, Set<String>> mAnimatedPropertiesPerView = new HashMap<>();
    private ValueAnimator mAnimator = ValueAnimator.ofFloat(0f, 1f);
    private boolean mHasInformedStateManagerAboutAnimationStart = false;
    private AdditiveAnimator mAdditiveAnimator;

    AdditiveAnimationAccumulator(AdditiveAnimator additiveAnimator) {
        mAdditiveAnimator = additiveAnimator;
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                notifyStateManagerAboutAnimationStartIfNeeded();
                List<View> modifiedViews = new ArrayList<>();
                for (AdditiveAnimation animation : mPreviousValues.keySet()) {
                    AccumulatedAnimationValues tempProperties = AdditiveAnimationStateManager.getAccumulatedProperties(animation.getView());
                    tempProperties.addDelta(animation, getDelta(animation, valueAnimator.getAnimatedFraction()));
                    modifiedViews.add(animation.getView());
                }
                for (View v : modifiedViews) {
                    if (!mAnimatedPropertiesPerView.containsKey(v)) {
                        continue;
                    }
                    AccumulatedAnimationValues accumulator = AdditiveAnimationStateManager.getAccumulatedProperties(v);
//                    accumulator.updateCounter += 1;
//                    if (accumulator.updateCounter >= AdditiveAnimationStateManager.from(v).mAdditiveAnimationAccumulators.size()) {
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
                    AdditiveAnimationStateManager.from(v).onAnimationApplierEnd(AdditiveAnimationAccumulator.this, animationDidCancel);
                }
            }

            @Override
            public void onAnimationStart(Animator animation) {
                notifyStateManagerAboutAnimationStartIfNeeded();
            }
        });
    }

    private void notifyStateManagerAboutAnimationStartIfNeeded() {
        if(!mHasInformedStateManagerAboutAnimationStart) {
            for (View v : mAnimatedPropertiesPerView.keySet()) {
                AdditiveAnimationStateManager manager = AdditiveAnimationStateManager.from(v);
                manager.onAnimationApplierStart(AdditiveAnimationAccumulator.this);
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
        addToAnimationMap(animation);
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
        // avoid ConcurrentModificationException
        Collection<String> animatedValues = new ArrayList<>(mAnimatedPropertiesPerView.get(v));
        for(String animatedValue : animatedValues) {
            removeTarget(v, animatedValue);
        }
    }

    /**
     * Removes the animation with the given name from the given view.
     * Returns true if this removed all animations from the view, false if there are still more animations running.
     */
    private boolean removeTarget(View v, String additiveAnimationName) {
        AdditiveAnimation animationToRemove = null;
        for(AdditiveAnimation anim : getAnimations(v)) {
            if(anim.getTag() == additiveAnimationName) {
                animationToRemove = anim;
                break;
            }
        }
        if(animationToRemove != null) {
            mPreviousValues.remove(animationToRemove);
            removeFromAnimationMap(animationToRemove);
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

    private void addToAnimationMap(AdditiveAnimation animation) {
        Set<AdditiveAnimation> animations = mAnimationsPerView.get(animation.getView());
        if(animations == null) {
            animations = new HashSet<>();
            mAnimationsPerView.put(animation.getView(), animations);
        }
        animations.add(animation);
    }

    private void removeFromAnimationMap(AdditiveAnimation animation) {
        Set<AdditiveAnimation> animations = mAnimationsPerView.get(animation.getView());
        if(animations == null) {
            return;
        }
        animations.remove(animation);
        if(animations.size() == 0) {
            mAnimationsPerView.remove(animation.getView());
        }
    }

    Collection<AdditiveAnimation> getAnimations(View v) {
        Set<AdditiveAnimation> animations = mAnimationsPerView.get(v);
        if(animations == null) {
            return new HashSet<>();
        }
        return animations;
    }

    ValueAnimator getAnimator() {
        return mAnimator;
    }

    Collection<AdditiveAnimation> getAnimations() {
        List<AdditiveAnimation> allAnimations = new ArrayList<>();
        for(Set<AdditiveAnimation> animations : mAnimationsPerView.values()) {
            allAnimations.addAll(animations);
        }
        return allAnimations;
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
