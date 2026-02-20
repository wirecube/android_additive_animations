package at.wirecube.additiveanimations.additive_animator.animation_set.view

import android.animation.TypeEvaluator
import android.util.Property
import android.view.View
import at.wirecube.additiveanimations.additive_animator.animation_set.AnimationAction

class ViewAnimation : AnimationAction.Animation<View> {

    constructor(property: Property<View, Float>, targetValue: Float) : super(property, targetValue)

    constructor(property: Property<View, Float>, targetValue: Float, evaluator: TypeEvaluator<Float>) : super(property, targetValue, evaluator)
}

