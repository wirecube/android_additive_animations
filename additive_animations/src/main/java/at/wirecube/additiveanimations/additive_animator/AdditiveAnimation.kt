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
package at.wirecube.additiveanimations.additive_animator

import android.animation.TypeEvaluator
import android.graphics.Path
import android.util.Property
import at.wirecube.additiveanimations.additive_animator.animation_set.AnimationState
import at.wirecube.additiveanimations.helper.SpringSolver
import at.wirecube.additiveanimations.helper.evaluators.PathEvaluator

/**
 * This class is public for subclasses of AdditiveAnimator only, and should not be used outside of that.
 */
class AdditiveAnimation<T: Any> private constructor(
    val target: T,
    val property: Property<T, Float>?,
    startValue: Float,
    targetValue: Float,
    private val tag: String,
    val path: Path?,
    private val pathMode: PathEvaluator.PathMode?,
    private val sharedPathEvaluator: PathEvaluator?,
) {
    var startValue: Float = startValue
        set(value) {
            field = value
        }

    var targetValue: Float = targetValue
        set(value) {
            field = value
        }
    /**
     * The preferred constructor to use when animating properties. If you use this constructor, you
     * don't need to worry about the logic to apply the changes. This is taken care of by using the
     * Setter provided by `property`.
     */
    constructor(target: T, property: Property<T, Float>, startValue: Float, targetValue: Float) : this(
        target = target,
        property = property,
        startValue = startValue,
        targetValue = targetValue,
        tag = property.name,
        path = null,
        pathMode = null,
        sharedPathEvaluator = null,
    )

    /**
     * Use this constructor for custom properties that have no simple getter or setter.
     *
     * @param tag         Name of the animated property. Must be unique.
     * @param startValue  Start value of the animated property.
     * @param targetValue Target value of the animated property.
     */
    constructor(target: T, tag: String, startValue: Float, targetValue: Float) : this(
        target = target,
        property = null,
        startValue = startValue,
        targetValue = targetValue,
        tag = tag,
        path = null,
        pathMode = null,
        sharedPathEvaluator = null,
    )

    constructor(target: T, tag: String, startValue: Float, path: Path, pathMode: PathEvaluator.PathMode, sharedEvaluator: PathEvaluator) : this(
        target = target,
        property = null,
        startValue = startValue,
        targetValue = 0f, // will be set below
        tag = tag,
        path = path,
        pathMode = pathMode,
        sharedPathEvaluator = sharedEvaluator,
    ) {
        targetValue = evaluateAt(1f)
    }

    constructor(target: T, property: Property<T, Float>, startValue: Float, path: Path, pathMode: PathEvaluator.PathMode, sharedEvaluator: PathEvaluator) : this(
        target = target,
        property = property,
        startValue = startValue,
        targetValue = 0f, // will be set below
        tag = property.name,
        path = path,
        pathMode = pathMode,
        sharedPathEvaluator = sharedEvaluator,
    ) {
        targetValue = evaluateAt(1f)
    }

    private val hashCode: Int = tag.hashCode() * ((2 shl 17) - 1) + target.hashCode()

    var customTypeEvaluator: TypeEvaluator<Float>? = null

    var accumulatedValue: AccumulatedAnimationValue<T>? = null

    var associatedAnimationState: AnimationState<T>? = null

    /**
     * Controls how the animation progresses over time.
     *
     * - `null`: uses the ValueAnimator's default interpolator (no per-animation override).
     * - [AnimationTiming.Interpolated]: uses a per-animation custom interpolator to remap progress.
     * - [AnimationTiming.Spring]: uses spring physics to compute the value from elapsed time.
     *
     * Setting this replaces any previous timing (interpolated or spring).
     */
    var timing: AnimationTiming = AnimationTiming.Interpolated(customInterpolator = null)
        set(value) {
            field = value
            springSolver = null // reset solver so it gets re-created with new timing
        }

    private var springSolver: SpringSolver? = null

    /**
     * Determines if the `targetValue` is a 'by' value. If it is, the actual target value will be computed when the animation starts
     * (as opposed to computing just the start value when it is enqueued).
     */
    var isBy: Boolean = false
        private set

    var byValue: Float = 0f
        private set

    /**
     * Set this immediately after creating the animation. Failure to do so will result in incorrect target values.
     */
    fun setBy(by: Boolean) {
        isBy = by
        if (by) {
            byValue = targetValue
        }
    }

    /**
     * Returns the settling duration in ms if this animation uses spring timing, or -1 otherwise.
     */
    val springSettlingDurationMs: Long
        get() = (timing as? AnimationTiming.Spring)?.settlingDurationMs() ?: -1

    fun evaluateAt(progress: Float): Float {
        return when (val currentTiming = timing) {
            // Spring-based timing: progress is a linear time fraction over the settling duration.
            is AnimationTiming.Spring -> {
                val solver = springSolver ?: currentTiming.createSolver(startValue, targetValue)
                val settlingDurationSeconds = currentTiming.settlingDurationMs() / 1000f
                val elapsedSeconds = progress * settlingDurationSeconds
                solver.solve(elapsedSeconds)
            }
            // Per-animation custom interpolator: remap progress, then evaluate.
            is AnimationTiming.Interpolated -> {
                val remapped = currentTiming.customInterpolator?.getInterpolation(progress) ?: progress
                evaluateValue(remapped)
            }
        }
    }

    private fun evaluateValue(progress: Float): Float {
        if (path != null && sharedPathEvaluator != null && pathMode != null) {
            return sharedPathEvaluator.evaluate(progress, pathMode, path)
        }
        val evaluator = customTypeEvaluator
        return if (evaluator != null) {
            evaluator.evaluate(progress, startValue, targetValue)
        } else {
            startValue + (targetValue - startValue) * progress
        }
    }

    fun getTag(): String = tag

    fun cloneWithTarget(target: T, startValue: Float): AdditiveAnimation<T> {
        val animation = if (property != null) {
            if (path != null) {
                AdditiveAnimation(target, property, startValue, path, pathMode!!, sharedPathEvaluator!!)
            } else {
                AdditiveAnimation(target, property, startValue, targetValue)
            }
        } else {
            if (path != null) {
                AdditiveAnimation(target, tag, startValue, path, pathMode!!, sharedPathEvaluator!!)
            } else {
                AdditiveAnimation(target, tag, startValue, targetValue)
            }
        }
        if (isBy) {
            animation.isBy = true
            animation.byValue = byValue
            animation.targetValue = startValue + animation.byValue
        }
        val currentTiming = timing
        if (currentTiming != null) {
            animation.timing = currentTiming
        }
        val currentEvaluator = customTypeEvaluator
        if (currentEvaluator != null) {
            animation.customTypeEvaluator = currentEvaluator
        }
        val currentState = associatedAnimationState
        if (currentState != null) {
            animation.associatedAnimationState = currentState
        }
        return animation
    }

    override fun hashCode(): Int = hashCode

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AdditiveAnimation<*>) return false
        return other.tag.hashCode() == tag.hashCode() && other.target === target
    }
}



