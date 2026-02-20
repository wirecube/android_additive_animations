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

import android.animation.TimeInterpolator;
import android.animation.TypeEvaluator;
import android.graphics.Path;
import android.util.Property;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import at.wirecube.additiveanimations.additive_animator.animation_set.AnimationState;
import at.wirecube.additiveanimations.helper.evaluators.PathEvaluator;

/**
 * This class is public for subclasses of AdditiveAnimator only, and should not be used outside of that.
 */
public class AdditiveAnimation<T> {

    private String tag;
    private float startValue;
    private float targetValue;
    @NonNull
    private Property<T, Float> property;
    // The path to animate along.
    @Nullable
    private Path path;
    // Which property of the path animation to use (X, Y, ROTATION).
    @Nullable
    private PathEvaluator.PathMode pathMode;
    // For animations that all use the same path, the path evaluator will be shared for performance reasons.
    @Nullable
    private PathEvaluator sharedPathEvaluator;
    @Nullable
    private TypeEvaluator<Float> customTypeEvaluator;
    @NonNull
    private final T target;
    private int cachedHashCode;
    @Nullable
    private TimeInterpolator customInterpolator; // each animation can have its own interpolator
    @Nullable
    private AccumulatedAnimationValue<T> accumulatedValue;

    /**
     * The "state" that this animation is associated with.
     * If another animation with a different state is enqueued for the same target, the already enqueued animation will continue running,
     * but the start/end actions associated with the state won't run.
     */
    private AnimationState<T> associatedAnimationState;

    /**
     * Determines if the `targetValue` is a 'by' value. If it is, the actual target value will be computed when the animation starts
     * (as opposed to computing just the start value when it is enqueued).
     */
    private boolean isByAnimation = false;
    private float byValue; // used for storing the by-value when the animation is a "by"-animation so that the animation can be copied correctly.

    /**
     * The preferred constructor to use when animating properties. If you use this constructor, you
     * don't need to worry about the logic to apply the changes. This is taken care of by using the
     * Setter provided by `property`.
     */
    public AdditiveAnimation(@NonNull T target, @NonNull Property<T, Float> property, float startValue, float targetValue) {
        this.target = target;
        this.property = property;
        this.targetValue = targetValue;
        this.startValue = startValue;
        setTag(property.getName());
    }

    /**
     * Use this constructor for custom properties that have no simple getter or setter.
     *
     * @param tag         Name of the animated property. Must be unique.
     * @param startValue  Start value of the animated property.
     * @param targetValue Target value of the animated property.
     */
    public AdditiveAnimation(@NonNull T target, @NonNull String tag, float startValue, float targetValue) {
        this.target = target;
        this.startValue = startValue;
        this.targetValue = targetValue;
        setTag(tag);
    }

    public AdditiveAnimation(@NonNull T target, @NonNull String tag, float startValue, @NonNull Path path, @NonNull PathEvaluator.PathMode pathMode, @NonNull PathEvaluator sharedEvaluator) {
        this.target = target;
        this.startValue = startValue;
        this.path = path;
        sharedPathEvaluator = sharedEvaluator;
        this.pathMode = pathMode;
        targetValue = evaluateAt(1f);
        setTag(tag);
    }

    public AdditiveAnimation(@NonNull T target, @NonNull Property<T, Float> property, float startValue, @NonNull Path path, @NonNull PathEvaluator.PathMode pathMode, @NonNull PathEvaluator sharedEvaluator) {
        this.target = target;
        this.property = property;
        this.startValue = startValue;
        this.path = path;
        sharedPathEvaluator = sharedEvaluator;
        this.pathMode = pathMode;
        targetValue = evaluateAt(1f);
        setTag(property.getName());
    }

    void setAccumulatedValue(@NonNull AccumulatedAnimationValue<T> av) {
        accumulatedValue = av;
    }

    private void setTag(@NonNull String tag) {
        this.tag = tag;
        // TODO: find a good hash code that doesn't collide often
        cachedHashCode = this.tag.hashCode() * ((2 << 17) - 1) + target.hashCode();
    }

    public String getTag() {
        return tag;
    }

    public float getStartValue() {
        return startValue;
    }

    public float getTargetValue() {
        return targetValue;
    }

    public void setStartValue(float startValue) {
        this.startValue = startValue;
    }

    public void setTargetValue(float targetValue) {
        this.targetValue = targetValue;
    }

    public void setCustomTypeEvaluator(@Nullable TypeEvaluator<Float> evaluator) {
        customTypeEvaluator = evaluator;
    }

    @Nullable
    public TypeEvaluator<Float> getCustomTypeEvaluator() {
        return customTypeEvaluator;
    }

    @NonNull
    public T getTarget() {
        return target;
    }

    /**
     * Set this immediately after creating the animation. Failure to do so will result in incorrect target values.
     */
    public void setByAnimation(boolean byAnimation) {
        this.isByAnimation = byAnimation;
        if (byAnimation) {
            byValue = targetValue;
        }
    }

    public boolean isByAnimation() {
        return isByAnimation;
    }

    public float getByValue() {
        return byValue;
    }

    @NonNull
    public Property<T, Float> getProperty() {
        return property;
    }

    @Nullable
    public Path getPath() {
        return path;
    }

    public void setCustomInterpolator(@Nullable TimeInterpolator customInterpolator) {
        this.customInterpolator = customInterpolator;
    }

    public float evaluateAt(float progress) {
        if (customInterpolator != null) {
            progress = customInterpolator.getInterpolation(progress);
        }
        if (path != null) {
            return sharedPathEvaluator.evaluate(progress, pathMode, path);
        } else {
            if (customTypeEvaluator != null) {
                return customTypeEvaluator.evaluate(progress, startValue, targetValue);
            } else {
                return startValue + (targetValue - startValue) * progress;
            }
        }
    }

    @Nullable
    AccumulatedAnimationValue<T> getAccumulatedValue() {
        return accumulatedValue;
    }

    public AdditiveAnimation<T> cloneWithTarget(T target, Float startValue) {
        final AdditiveAnimation<T> animation;
        if (this.getProperty() != null) {
            if (this.getPath() != null) {
                animation = new AdditiveAnimation<>(target, property, startValue, getPath(), pathMode, sharedPathEvaluator);
            } else {
                animation = new AdditiveAnimation<>(target, property, startValue, targetValue);
            }
        } else {
            if (this.getPath() != null) {
                animation = new AdditiveAnimation<>(target, tag, startValue, getPath(), pathMode, sharedPathEvaluator);
            } else {
                animation = new AdditiveAnimation<>(target, tag, startValue, targetValue);
            }
        }
        if (isByAnimation) {
            animation.isByAnimation = isByAnimation;
            animation.byValue = byValue;
            animation.targetValue = startValue + animation.byValue;
        }
        if (customInterpolator != null) {
            animation.setCustomInterpolator(customInterpolator);
        }
        if (customTypeEvaluator != null) {
            animation.setCustomTypeEvaluator(customTypeEvaluator);
        }
        if (associatedAnimationState != null) {
            animation.setAssociatedAnimationState(associatedAnimationState);
        }
        return animation;
    }

    public void setAssociatedAnimationState(AnimationState<T> associatedAnimationStateId) {
        this.associatedAnimationState = associatedAnimationStateId;
    }

    public AnimationState<T> getAssociatedAnimationState() {
        return associatedAnimationState;
    }

    @Override
    public int hashCode() {
        return cachedHashCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AdditiveAnimation)) {
            return false;
        }
        AdditiveAnimation<?> other = (AdditiveAnimation<?>) o;
        return other.tag.hashCode() == tag.hashCode() && other.target == target;
    }
}
