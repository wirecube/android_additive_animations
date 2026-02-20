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

import at.wirecube.additiveanimations.additive_animator.animation_set.AnimationState;
import at.wirecube.additiveanimations.helper.evaluators.PathEvaluator;

/**
 * This class is public for subclasses of AdditiveAnimator only, and should not be used outside of that.
 */
public class AdditiveAnimation<T> {

    private String mTag;
    private float mStartValue;
    private float mTargetValue;
    private Property<T, Float> mProperty;
    private Path mPath;
    private PathEvaluator.PathMode mPathMode;
    private PathEvaluator mSharedPathEvaluator;
    private TypeEvaluator<Float> mCustomTypeEvaluator;
    private final T mTarget;
    private int mHashCode;
    private TimeInterpolator mCustomInterpolator; // each animation can have its own interpolator
    private AccumulatedAnimationValue<T> mAccumulatedValue;
    private AnimationState<T> mAssociatedAnimationState;

    /**
     * Determines if the `targetValue` is a 'by' value. If it is, the actual target value will be computed when the animation starts
     * (as opposed to computing just the start value when it is enqueued).
     */
    private boolean mBy = false;
    private float mByValue; // used for storing the by-value when the animation is a "by"-animation so that the animation can be copied correctly.

    /**
     * The preferred constructor to use when animating properties. If you use this constructor, you
     * don't need to worry about the logic to apply the changes. This is taken care of by using the
     * Setter provided by `property`.
     */
    public AdditiveAnimation(T target, Property<T, Float> property, float startValue, float targetValue) {
        mTarget = target;
        mProperty = property;
        mTargetValue = targetValue;
        mStartValue = startValue;
        setTag(property.getName());
    }

    /**
     * Use this constructor for custom properties that have no simple getter or setter.
     *
     * @param tag         Name of the animated property. Must be unique.
     * @param startValue  Start value of the animated property.
     * @param targetValue Target value of the animated property.
     */
    public AdditiveAnimation(T target, String tag, float startValue, float targetValue) {
        mTarget = target;
        mStartValue = startValue;
        mTargetValue = targetValue;
        setTag(tag);
    }

    public AdditiveAnimation(T target, String tag, float startValue, Path path, PathEvaluator.PathMode pathMode, PathEvaluator sharedEvaluator) {
        mTarget = target;
        mStartValue = startValue;
        mPath = path;
        mSharedPathEvaluator = sharedEvaluator;
        mPathMode = pathMode;
        mTargetValue = evaluateAt(1f);
        setTag(tag);
    }

    public AdditiveAnimation(T target, Property<T, Float> property, float startValue, Path path, PathEvaluator.PathMode pathMode, PathEvaluator sharedEvaluator) {
        mTarget = target;
        mProperty = property;
        mStartValue = startValue;
        mPath = path;
        mSharedPathEvaluator = sharedEvaluator;
        mPathMode = pathMode;
        mTargetValue = evaluateAt(1f);
        setTag(property.getName());
    }

    public void setAccumulatedValue(AccumulatedAnimationValue<T> av) {
        mAccumulatedValue = av;
    }

    private void setTag(String tag) {
        mTag = tag;
        // TODO: find a good hash code that doesn't collide often
        mHashCode = mTag.hashCode() * ((2 << 17) - 1) + mTarget.hashCode();
    }

    public String getTag() {
        return mTag;
    }

    public float getStartValue() {
        return mStartValue;
    }

    public float getTargetValue() {
        return mTargetValue;
    }

    public void setStartValue(float startValue) {
        this.mStartValue = startValue;
    }

    public void setTargetValue(float targetValue) {
        mTargetValue = targetValue;
    }

    public void setCustomTypeEvaluator(TypeEvaluator<Float> evaluator) {
        mCustomTypeEvaluator = evaluator;
    }

    public TypeEvaluator<Float> getCustomTypeEvaluator() {
        return mCustomTypeEvaluator;
    }

    public T getTarget() {
        return mTarget;
    }

    /**
     * Set this immediately after creating the animation. Failure to do so will result in incorrect target values.
     */
    public void setBy(boolean by) {
        mBy = by;
        if (by) {
            mByValue = mTargetValue;
        }
    }

    public boolean isBy() {
        return mBy;
    }

    public float getByValue() {
        return mByValue;
    }

    public Property<T, Float> getProperty() {
        return mProperty;
    }

    public Path getPath() {
        return mPath;
    }

    public void setCustomInterpolator(TimeInterpolator customInterpolator) {
        mCustomInterpolator = customInterpolator;
    }

    public float evaluateAt(float progress) {
        if (mCustomInterpolator != null) {
            progress = mCustomInterpolator.getInterpolation(progress);
        }
        if (mPath != null) {
            return mSharedPathEvaluator.evaluate(progress, mPathMode, mPath);
        } else {
            if (mCustomTypeEvaluator != null) {
                return mCustomTypeEvaluator.evaluate(progress, mStartValue, mTargetValue);
            } else {
                return mStartValue + (mTargetValue - mStartValue) * progress;
            }
        }
    }

    public AccumulatedAnimationValue<T> getAccumulatedValue() {
        return mAccumulatedValue;
    }

    public AdditiveAnimation<T> cloneWithTarget(T target, Float startValue) {
        final AdditiveAnimation<T> animation;
        if (this.getProperty() != null) {
            if (this.getPath() != null) {
                animation = new AdditiveAnimation<>(target, mProperty, startValue, getPath(), mPathMode, mSharedPathEvaluator);
            } else {
                animation = new AdditiveAnimation<>(target, mProperty, startValue, mTargetValue);
            }
        } else {
            if (this.getPath() != null) {
                animation = new AdditiveAnimation<>(target, mTag, startValue, getPath(), mPathMode, mSharedPathEvaluator);
            } else {
                animation = new AdditiveAnimation<>(target, mTag, startValue, mTargetValue);
            }
        }
        if (mBy) {
            animation.mBy = mBy;
            animation.mByValue = mByValue;
            animation.mTargetValue = startValue + animation.mByValue;
        }
        if (mCustomInterpolator != null) {
            animation.setCustomInterpolator(mCustomInterpolator);
        }
        if (mCustomTypeEvaluator != null) {
            animation.setCustomTypeEvaluator(mCustomTypeEvaluator);
        }
        if (mAssociatedAnimationState != null) {
            animation.setAssociatedAnimationState(mAssociatedAnimationState);
        }
        return animation;
    }

    public void setAssociatedAnimationState(AnimationState<T> associatedAnimationStateId) {
        this.mAssociatedAnimationState = associatedAnimationStateId;
    }

    public AnimationState<T> getAssociatedAnimationState() {
        return mAssociatedAnimationState;
    }

    @Override
    public int hashCode() {
        return mHashCode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AdditiveAnimation)) {
            return false;
        }
        AdditiveAnimation<T> other = (AdditiveAnimation<T>) o;
        return other.mTag.hashCode() == mTag.hashCode() && other.mTarget == mTarget;
    }
}
