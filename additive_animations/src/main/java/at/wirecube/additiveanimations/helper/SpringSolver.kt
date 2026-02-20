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

package at.wirecube.additiveanimations.helper

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Analytical solver for a damped harmonic oscillator (spring).
 *
 * Solves the equation: x''(t) + 2ζω₀x'(t) + ω₀²x(t) = ω₀²·target
 *
 * where:
 * - ω₀ = √stiffness (natural angular frequency, assuming mass = 1)
 * - ζ  = dampingRatio
 * - target = the equilibrium (rest) position
 *
 * Supports all three damping regimes: underdamped (ζ < 1), critically damped (ζ = 1), and overdamped (ζ > 1).
 *
 * @param stiffness    Spring stiffness constant (must be > 0).
 * @param dampingRatio Damping ratio: 0 = undamped, < 1 = underdamped, 1 = critical, > 1 = overdamped.
 * @param startValue   The initial position (value at t=0).
 * @param targetValue  The equilibrium position the spring pulls towards.
 * @param initialVelocity The initial velocity at t=0 (default 0). Can be used for velocity handoff.
 */
class SpringSolver(
    val stiffness: Float,
    val dampingRatio: Float,
    val startValue: Float,
    val targetValue: Float,
    val initialVelocity: Float = 0f
) {
    private val omega0: Double = sqrt(stiffness.toDouble()) // natural frequency
    private val displacement: Double = (startValue - targetValue).toDouble() // x₀ - target

    companion object {
        /**
         * Default threshold for considering the spring "settled".
         * The spring is settled when the remaining displacement is less than this fraction
         * of the total animation distance.
         */
        const val DEFAULT_SETTLING_THRESHOLD: Float = 0.001f
    }

    /**
     * Computes the spring position at the given elapsed time.
     *
     * @param elapsedSeconds Time in seconds since the animation started.
     * @return The spring position at the given time.
     */
    fun solve(elapsedSeconds: Float): Float {
        val t = elapsedSeconds.toDouble()
        if (t <= 0.0) return startValue

        val zeta = dampingRatio.toDouble()
        val v0 = initialVelocity.toDouble()

        val position: Double = when {
            // Underdamped: ζ < 1 — oscillates around target
            zeta < 1.0 -> {
                val omegaD = omega0 * sqrt(1.0 - zeta * zeta) // damped frequency
                val gamma = zeta * omega0
                // x(t) = target + e^(-γt) * (A·cos(ωd·t) + B·sin(ωd·t))
                // where A = x₀ - target, B = (v₀ + γ·A) / ωd
                val a = displacement
                val b = (v0 + gamma * a) / omegaD
                val envelope = exp(-gamma * t)
                targetValue + envelope * (a * cos(omegaD * t) + b * sin(omegaD * t))
            }

            // Critically damped: ζ = 1 — fastest approach without oscillation
            zeta == 1.0 -> {
                // x(t) = target + (A + B·t) · e^(-ω₀·t)
                // where A = x₀ - target, B = v₀ + ω₀·A
                val a = displacement
                val b = v0 + omega0 * a
                targetValue + (a + b * t) * exp(-omega0 * t)
            }

            // Overdamped: ζ > 1 — slow exponential approach
            else -> {
                val sqrtTerm = sqrt(zeta * zeta - 1.0)
                val r1 = -omega0 * (zeta - sqrtTerm) // less negative root
                val r2 = -omega0 * (zeta + sqrtTerm) // more negative root
                // x(t) = target + c1·e^(r1·t) + c2·e^(r2·t)
                // Solve initial conditions: c1 + c2 = displacement, c1·r1 + c2·r2 = v0
                val c2 = (v0 - r1 * displacement) / (r2 - r1)
                val c1 = displacement - c2
                targetValue + c1 * exp(r1 * t) + c2 * exp(r2 * t)
            }
        }

        return position.toFloat()
    }

    /**
     * Computes the spring velocity at the given elapsed time.
     *
     * @param elapsedSeconds Time in seconds since the animation started.
     * @return The velocity (units per second) at the given time.
     */
    fun velocity(elapsedSeconds: Float): Float {
        val t = elapsedSeconds.toDouble()
        if (t <= 0.0) return initialVelocity

        val zeta = dampingRatio.toDouble()
        val v0 = initialVelocity.toDouble()

        val vel: Double = when {
            zeta < 1.0 -> {
                val omegaD = omega0 * sqrt(1.0 - zeta * zeta)
                val gamma = zeta * omega0
                val a = displacement
                val b = (v0 + gamma * a) / omegaD
                val envelope = exp(-gamma * t)
                val cosVal = cos(omegaD * t)
                val sinVal = sin(omegaD * t)
                // d/dt [e^(-γt) * (A·cos(ωd·t) + B·sin(ωd·t))]
                envelope * ((-gamma) * (a * cosVal + b * sinVal) + omegaD * (-a * sinVal + b * cosVal))
            }

            zeta == 1.0 -> {
                val a = displacement
                val b = v0 + omega0 * a
                // d/dt [(A + B·t) · e^(-ω₀·t)] = (B - ω₀·(A + B·t)) · e^(-ω₀·t)
                (b - omega0 * (a + b * t)) * exp(-omega0 * t)
            }

            else -> {
                val sqrtTerm = sqrt(zeta * zeta - 1.0)
                val r1 = -omega0 * (zeta - sqrtTerm)
                val r2 = -omega0 * (zeta + sqrtTerm)
                val c2 = (v0 - r1 * displacement) / (r2 - r1)
                val c1 = displacement - c2
                c1 * r1 * exp(r1 * t) + c2 * r2 * exp(r2 * t)
            }
        }

        return vel.toFloat()
    }

    /**
     * Returns whether the spring has settled — i.e. both the displacement and velocity
     * are below the given thresholds.
     *
     * @param elapsedSeconds Time in seconds since the animation started.
     * @param positionThreshold Fraction of total distance below which the spring is "close enough" (default 0.001).
     */
    fun isSettled(
        elapsedSeconds: Float,
        positionThreshold: Float = DEFAULT_SETTLING_THRESHOLD
    ): Boolean {
        val totalDistance = abs(targetValue - startValue)
        if (totalDistance == 0f) return true
        val currentPos = solve(elapsedSeconds)
        val currentVel = velocity(elapsedSeconds)
        val absThreshold = positionThreshold * totalDistance
        return abs(currentPos - targetValue) < absThreshold && abs(currentVel) < absThreshold
    }
}

