package at.wirecube.additiveanimations.additiveanimationsdemo.animation.additive_animations.base;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


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
    private final List<AdditiveAnimatorHolder> additiveAnimatorHolders = new ArrayList<>();
    private AdditiveAnimator mAnimationUpdater;

    private ValueAnimator mUnstartedAnimator;

    private AdditiveAnimationApplier(View animationTarget) {
        mAnimationTargetView = animationTarget;
        mAccumulatedLayoutParams = new AccumulatedProperties();
    }

    public void setAnimationUpdater(AdditiveAnimator animationUpdater) {
        this.mAnimationUpdater = animationUpdater;
    }

    public AdditiveAnimationApplier addAnimation(AdditivelyAnimatedPropertyDescription propertyDescription) {
        addAnimations(Arrays.asList(propertyDescription));
        return this;
    }

    public AdditiveAnimationApplier addAnimations(List<AdditivelyAnimatedPropertyDescription> propertyDescriptions) {
        // TODO: refactor start value computation
        Map<AdditivelyAnimatedPropertyDescription, Float> startValues = new HashMap<>();
        for(AdditivelyAnimatedPropertyDescription propertyDescription : propertyDescriptions) {
            startValues.put(propertyDescription, propertyDescription.getStartValue());
        }

        // collect correct start values:
        // TODO: this is very inefficient and ugly
        Set<AdditivelyAnimatedPropertyDescription> propertiesWithPreviousAnimation = new HashSet<>(propertyDescriptions);
        for(AdditivelyAnimatedPropertyDescription property : propertyDescriptions) {
            for(int i = additiveAnimatorHolders.size() - 1; i >= 0; i--) {
                if(propertiesWithPreviousAnimation.contains(property) && additiveAnimatorHolders.get(i).getTargets().containsKey(property)) {
                    property.setStartValue(additiveAnimatorHolders.get(i).getTargets().get(property));
                    propertiesWithPreviousAnimation.remove(property);
                }
            }
        }
        for(AdditivelyAnimatedPropertyDescription p : propertiesWithPreviousAnimation) {
            mAccumulatedLayoutParams.tempProperties.put(p, p.getStartValue());
        }

        if(mUnstartedAnimator == null) {
            mUnstartedAnimator = ValueAnimator.ofFloat(0, 1);
        }
        final AdditiveAnimatorHolder additiveAnimatorHolder = new AdditiveAnimatorHolder(propertyDescriptions, mUnstartedAnimator, mAnimationTargetView, mAnimationUpdater, mAccumulatedLayoutParams);
        if(!additiveAnimatorHolder.hasDiff()) {
            return this;
        }
        mUnstartedAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                animation.removeAllListeners();
                additiveAnimatorHolders.remove(additiveAnimatorHolder);
                if (additiveAnimatorHolders.isEmpty()) {
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

            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }
        });

        additiveAnimatorHolder.setShouldRequestLayout(true);
        additiveAnimatorHolders.add(additiveAnimatorHolder);

        // TODO: use a single value animator from 0 to 1 and keep track of the progress (delta) in each additiveAnimatorHolder.
        // Restart the value animator when a new animation is added.
        // Then all animators can be updated together, thus reducing the performance overhead introduced by using different animators.
        // This way, it's much simpler to go through each animator in succession and then request a layout pass at the right moment.
        return this;
    }

    public void start() {
        if(mUnstartedAnimator != null) {
            mUnstartedAnimator.setDuration(mAnimationUpdater.getDuration());
            mUnstartedAnimator.setInterpolator(mAnimationUpdater.getInterpolator());
            mUnstartedAnimator.start();
            mUnstartedAnimator = null;
        }
    }

    public void cancelAllAnimations() {
        for(AdditiveAnimatorHolder additiveAnimatorHolder : additiveAnimatorHolders) {
            additiveAnimatorHolder.cancel();
        }
        additiveAnimatorHolders.clear();
        sAnimators.remove(mAnimationTargetView);
    }

}
