package at.wirecube.additiveanimations.additive_animator.animation_set.view

import android.view.View
import at.wirecube.additiveanimations.additive_animator.animation_set.AnimationAction
import at.wirecube.additiveanimations.additive_animator.animation_set.AnimationState

class ViewAnimationState : AnimationState<View> {

    private val mAnimations: MutableList<AnimationAction.Animation<View>> = mutableListOf()
    private val mStartAction: AnimationStartAction<View>?
    private val mEndAction: AnimationEndAction<View>?

    constructor(animations: List<ViewAnimation>) : this(animations, null, null)

    constructor(vararg animations: ViewAnimation) : this(animations.toList(), null, null)

    constructor(animations: List<ViewAnimation>, startAction: AnimationStartAction<View>?) : this(animations, startAction, null)

    constructor(animation: ViewAnimation, startAction: AnimationStartAction<View>?) : this(listOf(animation), startAction, null)

    constructor(startAction: AnimationStartAction<View>?, vararg animations: ViewAnimation) : this(animations.toList(), startAction, null)

    constructor(animation: ViewAnimation, endAction: AnimationEndAction<View>?) : this(listOf(animation), null, endAction)

    constructor(animations: List<ViewAnimation>, endAction: AnimationEndAction<View>?) : this(animations, null, endAction)

    constructor(endAction: AnimationEndAction<View>?, vararg animations: ViewAnimation) : this(animations.toList(), null, endAction)

    constructor(animation: ViewAnimation, startAction: AnimationStartAction<View>?, endAction: AnimationEndAction<View>?) : this(listOf(animation), startAction, endAction)

    constructor(startAction: AnimationStartAction<View>?, endAction: AnimationEndAction<View>?, vararg animations: ViewAnimation) : this(animations.toList(), startAction, endAction)

    constructor(
        animations: List<ViewAnimation>,
        startAction: AnimationStartAction<View>?,
        endAction: AnimationEndAction<View>?
    ) {
        mAnimations.addAll(animations)
        mStartAction = startAction
        mEndAction = endAction
    }

    override fun getAnimations(): List<AnimationAction.Animation<View>> = mAnimations

    override fun getAnimationStartAction(): AnimationStartAction<View>? = mStartAction

    override fun getAnimationEndAction(): AnimationEndAction<View>? = mEndAction
}

