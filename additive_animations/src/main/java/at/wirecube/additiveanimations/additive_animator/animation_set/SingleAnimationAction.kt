package at.wirecube.additiveanimations.additive_animator.animation_set

import android.animation.TypeEvaluator
import android.util.Property

class SingleAnimationAction<T> : AnimationAction<T> {

    private val animations: MutableList<AnimationAction.Animation<T>> = mutableListOf()

    constructor(property: Property<T, Float>, target: Float) {
        animations.add(AnimationAction.Animation(property, target))
    }

    constructor(property: Property<T, Float>, target: Float, evaluator: TypeEvaluator<Float>) {
        animations.add(AnimationAction.Animation(property, target, evaluator))
    }

    override fun getAnimations(): List<AnimationAction.Animation<T>> = animations
}

