/*
 *  Copyright 2026 David Ganster
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

import android.animation.TimeInterpolator
import at.wirecube.additiveanimations.helper.SpringSolver
import kotlin.math.ln
import kotlin.math.sqrt

/**
 * Sealed hierarchy representing how an animation's timing is computed.
 *
 * - [Interpolated]: Traditional time-based animation using a [TimeInterpolator] and a fixed duration.
 * - [Spring]: Physics-based spring animation using stiffness and damping ratio.
 */
sealed class AnimationTiming {

    /**
     * Traditional time-based animation. The progress fraction (0→1) is shaped by the [interpolator].
     * This is the default mode and maps directly to how [android.animation.ValueAnimator] works.
     */
    data class Interpolated(val interpolator: TimeInterpolator?) : AnimationTiming()

    /**
     * Physics-based spring animation.
     *
     * The animation uses a damped harmonic oscillator to compute values.
     * The [stiffness] controls how "tight" the spring is (higher = faster, snappier motion).
     * The [dampingRatio] controls oscillation:
     * - `< 1.0`: underdamped (oscillates/bounces around the target)
     * - `= 1.0`: critically damped (reaches target as fast as possible without oscillating)
     * - `> 1.0`: overdamped (slowly approaches target without oscillating)
     *
     * @property stiffness The spring stiffness constant. Must be positive.
     * @property dampingRatio The damping ratio. Must be non-negative. 0 = no damping (infinite oscillation).
     */
    data class Spring(
        val stiffness: Float,
        val dampingRatio: Float
    ) : AnimationTiming() {

        init {
            require(stiffness > 0f) { "Stiffness must be positive, was $stiffness" }
            require(dampingRatio >= 0f) { "Damping ratio must be non-negative, was $dampingRatio" }
        }

        /**
         * The natural angular frequency of this spring: ω₀ = √(stiffness).
         * (Assuming mass = 1.)
         */
        val naturalFrequency: Float get() = sqrt(stiffness.toDouble()).toFloat()

        /**
         * Computes the estimated duration (in milliseconds) for the spring to settle within
         * [threshold] of the target value, as a fraction of the total animation distance.
         *
         * For example, a threshold of 0.001 means the spring is considered settled when the
         * remaining displacement is less than 0.1% of the total distance.
         */
        @JvmOverloads
        fun settlingDurationMs(threshold: Float = SpringSolver.DEFAULT_SETTLING_THRESHOLD): Long {
            val omega0 = naturalFrequency.toDouble()
            if (omega0 == 0.0) return Long.MAX_VALUE

            // The envelope of the oscillation decays as e^(-dampingRatio * omega0 * t).
            // We solve for t when the envelope equals the threshold:
            // e^(-dampingRatio * omega0 * t) = threshold
            // t = -ln(threshold) / (dampingRatio * omega0)
            val effectiveDamping = if (dampingRatio < 0.001f) 0.001 else dampingRatio.toDouble()
            val settlingTimeSeconds = -ln(threshold.toDouble()) / (effectiveDamping * omega0)

            // Add a small safety margin (10%) to ensure the ValueAnimator doesn't cut off early
            return (settlingTimeSeconds * 1.1 * 1000).toLong().coerceAtLeast(1L)
        }

        /**
         * Creates a [SpringSolver] for evaluating spring position at a given time.
         */
        fun createSolver(startValue: Float, targetValue: Float): SpringSolver {
            return SpringSolver(stiffness, dampingRatio, startValue, targetValue)
        }

        companion object {
            /**
             * Creates a [Spring] timing from a desired duration and damping ratio,
             * similar to iOS's `UIView.animate(withDuration:dampingRatio:...)`.
             *
             * The stiffness is derived so that the spring settles within approximately [durationMs].
             * The [dampingRatio] controls the amount of bounce (see [Spring] docs).
             *
             * @param durationMs Desired animation duration in milliseconds.
             * @param dampingRatio Damping ratio (0 = no damping, 1 = critical damping, > 1 = overdamped).
             */
            @JvmStatic
            fun withDuration(durationMs: Long, dampingRatio: Float): Spring {
                require(durationMs > 0) { "Duration must be positive, was $durationMs" }
                require(dampingRatio >= 0f) { "Damping ratio must be non-negative, was $dampingRatio" }

                val durationSeconds = durationMs / 1000.0
                val effectiveDamping = if (dampingRatio < 0.001f) 0.001 else dampingRatio.toDouble()

                // We want the settling time to match the desired duration.
                // From settlingTime = -ln(threshold) / (dampingRatio * omega0):
                //   omega0 = -ln(threshold) / (dampingRatio * settlingTime)
                //   stiffness = omega0^2
                val omega0 = -ln(SpringSolver.DEFAULT_SETTLING_THRESHOLD.toDouble()) / (effectiveDamping * durationSeconds)
                val stiffness = (omega0 * omega0).toFloat()

                return Spring(stiffness, dampingRatio)
            }
        }
    }
}

