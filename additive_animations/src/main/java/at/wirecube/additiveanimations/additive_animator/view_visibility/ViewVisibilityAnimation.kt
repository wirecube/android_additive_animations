package at.wirecube.additiveanimations.additive_animator.view_visibility

import android.view.View
import at.wirecube.additiveanimations.additive_animator.animation_set.AnimationAction
import at.wirecube.additiveanimations.additive_animator.animation_set.AnimationState

class ViewVisibilityAnimation(
    visibility: Int,
    private val mAnimations: List<AnimationAction.Animation<View>>
) : AnimationState<View>() {

    private var mEndAction: AnimationEndAction<View>? = null
    private var mStartAction: AnimationStartAction<View>? = null

    init {
        when (visibility) {
            View.VISIBLE -> mStartAction = AnimationStartAction { view -> view.visibility = View.VISIBLE }
            View.INVISIBLE -> mEndAction = AnimationEndAction { view, _ -> view.visibility = View.INVISIBLE }
            View.GONE -> mEndAction = AnimationEndAction { view, _ -> view.visibility = View.GONE }
        }
    }

    override fun getAnimations(): List<AnimationAction.Animation<View>> = mAnimations

    override fun getAnimationEndAction(): AnimationEndAction<View>? = mEndAction

    override fun getAnimationStartAction(): AnimationStartAction<View>? = mStartAction

    companion object {
        @JvmStatic
        fun builder(visibility: Int): ViewVisibilityBuilder = ViewVisibilityBuilder(visibility)

        @JvmStatic
        fun gone(): ViewVisibilityBuilder = ViewVisibilityBuilder(View.GONE)

        @JvmStatic
        fun visible(): ViewVisibilityBuilder = ViewVisibilityBuilder(View.VISIBLE)

        @JvmStatic
        fun invisible(): ViewVisibilityBuilder = ViewVisibilityBuilder(View.INVISIBLE)

        /**
         * Sets the visibility of the view to View.VISIBLE and fades it in.
         */
        @JvmStatic
        fun fadeIn(): AnimationState<View> {
            return visible()
                .addAnimation(AnimationAction.Animation(View.ALPHA, 1f))
                .build()
        }

        /**
         * Sets the visibility of the view to View.VISIBLE, fades it in and also sets its translationX and translationY back to 0.
         */
        @JvmStatic
        fun fadeInAndTranslateBack(): AnimationState<View> {
            return visible()
                .addAnimations(
                    AnimationAction.Animation(View.ALPHA, 1f),
                    AnimationAction.Animation(View.TRANSLATION_X, 0f),
                    AnimationAction.Animation(View.TRANSLATION_Y, 0f)
                )
                .build()
        }

        /**
         * Fades out the target and then sets its visibility to either View.INVISIBLE or GONE, depending on the gone parameter.
         */
        @JvmStatic
        fun fadeOut(gone: Boolean): AnimationState<View> {
            return ViewVisibilityBuilder(if (gone) View.GONE else View.INVISIBLE)
                .addAnimation(AnimationAction.Animation(View.ALPHA, 0f))
                .build()
        }

        /**
         * Fades out the target and then sets its visibility to either View.INVISIBLE or GONE, depending on the gone parameter.
         * Also moves the view by xTranslation and yTranslation.
         */
        @JvmStatic
        fun fadeOutAndTranslate(gone: Boolean, xTranslation: Float, yTranslation: Float): AnimationState<View> {
            return ViewVisibilityBuilder(if (gone) View.GONE else View.INVISIBLE)
                .addAnimations(
                    AnimationAction.Animation(View.ALPHA, 0f),
                    AnimationAction.Animation(View.TRANSLATION_X, xTranslation),
                    AnimationAction.Animation(View.TRANSLATION_Y, yTranslation)
                )
                .build()
        }

        /**
         * Fades out the target and then sets its visibility to either View.INVISIBLE or GONE, depending on the gone parameter.
         * Also moves the view horizontally by xTranslation.
         */
        @JvmStatic
        fun fadeOutAndTranslateX(gone: Boolean, xTranslation: Float): AnimationState<View> {
            return ViewVisibilityBuilder(if (gone) View.GONE else View.INVISIBLE)
                .addAnimations(
                    AnimationAction.Animation(View.ALPHA, 0f),
                    AnimationAction.Animation(View.TRANSLATION_X, xTranslation)
                )
                .build()
        }

        /**
         * Fades out the target and then sets its visibility to either View.INVISIBLE or GONE, depending on the gone parameter.
         * Also moves the view vertically by yTranslation.
         */
        @JvmStatic
        fun fadeOutAndTranslateY(gone: Boolean, yTranslation: Float): AnimationState<View> {
            return ViewVisibilityBuilder(if (gone) View.GONE else View.INVISIBLE)
                .addAnimations(
                    AnimationAction.Animation(View.ALPHA, 0f),
                    AnimationAction.Animation(View.TRANSLATION_Y, yTranslation)
                )
                .build()
        }
    }
}

