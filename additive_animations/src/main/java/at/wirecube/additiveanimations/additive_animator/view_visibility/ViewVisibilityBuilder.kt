package at.wirecube.additiveanimations.additive_animator.view_visibility

import android.view.View
import at.wirecube.additiveanimations.additive_animator.animation_set.AnimationState

class ViewVisibilityBuilder(visibility: Int) : AnimationState.Builder<ViewVisibilityBuilder, View>() {

    private val visibilityStartAction: AnimationState.AnimationStartAction<View>?
    private val visibilityEndAction: AnimationState.AnimationEndAction<View>?

    init {
        when (visibility) {
            View.VISIBLE -> {
                visibilityStartAction = AnimationState.AnimationStartAction { view -> view.visibility = View.VISIBLE }
                visibilityEndAction = null
            }
            View.INVISIBLE -> {
                visibilityStartAction = null
                visibilityEndAction = AnimationState.AnimationEndAction { view, _ -> view.visibility = View.INVISIBLE }
            }
            View.GONE -> {
                visibilityStartAction = null
                visibilityEndAction = AnimationState.AnimationEndAction { view, _ -> view.visibility = View.GONE }
            }
            else -> throw IllegalArgumentException(
                "Cannot instantiate a ViewVisibilityAnimation.Builder without a valid visibility (given: $visibility)."
            )
        }
        startAction = getWrappedStartAction(null)
        endAction = getWrappedEndAction(null)
    }

    private fun getWrappedStartAction(startAction: AnimationState.AnimationStartAction<View>?): AnimationState.AnimationStartAction<View> {
        return AnimationState.AnimationStartAction { view ->
            visibilityStartAction?.onStart(view)
            startAction?.onStart(view)
        }
    }

    private fun getWrappedEndAction(endAction: AnimationState.AnimationEndAction<View>?): AnimationState.AnimationEndAction<View> {
        return AnimationState.AnimationEndAction { view, wasCancelled ->
            visibilityEndAction?.onEnd(view, wasCancelled)
            endAction?.onEnd(view, wasCancelled)
        }
    }

    override fun withStartAction(startAction: AnimationState.AnimationStartAction<View>?): ViewVisibilityBuilder {
        return super.withStartAction(getWrappedStartAction(startAction))
    }

    override fun withEndAction(endAction: AnimationState.AnimationEndAction<View>?): ViewVisibilityBuilder {
        return super.withEndAction(getWrappedEndAction(endAction))
    }
}
