package at.wirecube.additiveanimations.additiveanimationsdemo.animation.additive_animations.base;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.os.Looper;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A class that supports additive animations on all properties, provided you create a AdditiveAnimatorUpdater.
 * Additive animations are nicely explained here: http://ronnqvi.st/multiple-animations/
 * We have to use an additive animation when moving the user location view, because its position
 * is being updated all the time, before the previous animation is allowed to finish, resulting in
 * abrupt changes in velocity and direction.
 */
public class AdditiveAnimator {

    private static final Map<View, AdditiveAnimator> sAnimators = new HashMap<>();

    public static final AdditiveAnimator animate(View targetView) {
        AdditiveAnimator animator = sAnimators.get(targetView);
        if (animator == null) {
            animator = new AdditiveAnimator(targetView);
            sAnimators.put(targetView, animator);
        }
        return animator;
    }

    /**
     * Helper class for accumulating the changes made by all of the additive animators.
     */
    private static class AccumulatedProperties {
        Map<String, Double> tempProperties = new HashMap<>();
    }

    private AccumulatedProperties mAccumulatedLayoutParams = new AccumulatedProperties();

    private class AdditiveAnimatorHolder {
        private Map<String, Double> diffs = new HashMap<>();
        private Map<String, Double> lastValues = new HashMap<>();
        private Map<String, Double> targets = new HashMap<>();
        private boolean shouldRequestLayout = false;
        private View mAnimationTargetView;
        private ValueAnimator animator;
        private AdditiveAnimatorUpdater updater;

        private AdditiveAnimatorHolder(List<AdditivelyAnimatedPropertyDescription> properties, ValueAnimator animator, View animationTargetView, AdditiveAnimatorUpdater animationChangeApplier) {
            for(AdditivelyAnimatedPropertyDescription description : properties) {
                diffs.put(description.getTag(), description.getTargetValue() - description.getStartValue());
                lastValues.put(description.getTag(), new Double(0));
                targets.put(description.getTag(), description.getTargetValue());
            }
            this.animator = animator;
            this.mAnimationTargetView = animationTargetView;
            this.updater = animationChangeApplier;
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    AccumulatedProperties params = mAccumulatedLayoutParams;
                    for(String tag : targets.keySet()) {
                        params.tempProperties.put(tag, params.tempProperties.get(tag) + getDelta(tag, animation.getAnimatedFraction()));
                    }
                    if(shouldRequestLayout) {
                        updater.applyChanges(params.tempProperties, mAnimationTargetView);
                    }
                }
            });
        }

        public final double getDelta(String tag, double progress) {
            double diff = diffs.get(tag);
            double lastVal = lastValues.get(tag);
            double newVal = diff * progress;
            double delta = newVal - lastVal;
            lastValues.put(tag, newVal);
            return delta;
        }

        public final void start() {
            animator.start();
        }
        public final void cancel() {
            animator.cancel();
        }

        private final boolean hasDiff() {
            for(Double diff : diffs.values()) {
                if(diff != 0) {
                    return true;
                }
            }
            return false;
        }
    }

    private final View mAnimationTargetView;
    private final List<AdditiveAnimatorHolder> additiveAnimatorHolders = new ArrayList<>();

    public AdditiveAnimator(View animationTarget) {
        mAnimationTargetView = animationTarget;
        mAccumulatedLayoutParams = new AccumulatedProperties();
    }

    public void addAnimation(AdditivelyAnimatedPropertyDescription propertyDescription, int animationDuration, AdditiveAnimatorUpdater updater) {
        addAnimations(Arrays.asList(propertyDescription), animationDuration, updater);
    }

    public void addAnimations(List<AdditivelyAnimatedPropertyDescription> propertyDescriptions, int animationDuration, AdditiveAnimatorUpdater updater) {
        Map<String, Double> startValues = new HashMap<>();
        for(AdditivelyAnimatedPropertyDescription propertyDescription : propertyDescriptions) {
            startValues.put(propertyDescription.getTag(), propertyDescription.getStartValue());
        }

        if(!additiveAnimatorHolders.isEmpty()) {
            Map<String, Double> lastAnimatorTargets = additiveAnimatorHolders.get(additiveAnimatorHolders.size() - 1).targets;
            for(AdditivelyAnimatedPropertyDescription property : propertyDescriptions) {
                property.setStartValue(lastAnimatorTargets.get(property.getTag()));
            }
        } else {
            mAccumulatedLayoutParams.tempProperties.putAll(startValues);
        }

        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        animator.setDuration(animationDuration);
        final AdditiveAnimatorHolder additiveAnimatorHolder = new AdditiveAnimatorHolder(propertyDescriptions, animator, mAnimationTargetView, updater);
        if(!additiveAnimatorHolder.hasDiff()) {
            return;
        }
        animator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                animation.removeAllListeners();
                additiveAnimatorHolders.remove(additiveAnimatorHolder);
                if(additiveAnimatorHolders.isEmpty()) {
                    // TODO: error correction measures if necessary (add another animation to the last target state if there is a diff between the current state and the target)
                    sAnimators.remove(mAnimationTargetView);
                }
            }
            @Override
            public void onAnimationCancel(Animator animation) {
                animation.removeAllListeners();
                // This is only called when we cancel all animations, in which case we clear our animators anyway
                // By removing the listeners, we ensure that our normal `onAnimationEnd` method isn't called.
            }
            @Override public void onAnimationStart(Animator animation) {}
            @Override public void onAnimationRepeat(Animator animation) {}
        });

        additiveAnimatorHolder.shouldRequestLayout = true;
        additiveAnimatorHolders.add(additiveAnimatorHolder);

        // TODO: use a single value animator from 0 to 1 and keep track of the progress (delta) in each additiveAnimatorHolder.
        // Restart the value animator when a new animation is added.
        // Then all animators can be updated together, thus reducing the performance overhead introduced by using different animators.
        // This way, it's much simpler to go through each animator in succession and then request a layout pass at the right moment.
        additiveAnimatorHolder.start();
    }

    public void cancelAllAnimations() {
        for(AdditiveAnimatorHolder additiveAnimatorHolder : additiveAnimatorHolders) {
            additiveAnimatorHolder.cancel();
        }
        additiveAnimatorHolders.clear();
        sAnimators.remove(mAnimationTargetView);
    }

}
