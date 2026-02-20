package at.wirecube.additiveanimations.additive_animator.view_visibility;

import android.view.View;

import java.util.List;

import at.wirecube.additiveanimations.additive_animator.animation_set.AnimationAction;
import at.wirecube.additiveanimations.additive_animator.animation_set.AnimationState;

public class ViewVisibilityAnimation extends AnimationState<View> {

    public static ViewVisibilityBuilder builder(int visibility) {
        return new ViewVisibilityBuilder(visibility);
    }

    public static ViewVisibilityBuilder gone() {
        return new ViewVisibilityBuilder(View.GONE);
    }

    public static ViewVisibilityBuilder visible() {
        return new ViewVisibilityBuilder(View.VISIBLE);
    }

    public static ViewVisibilityBuilder invisible() {
        return new ViewVisibilityBuilder(View.INVISIBLE);
    }

    /**
     * Sets the visibility of the view to View.VISIBLE and fades it in.
     */
    public static AnimationState<View> fadeIn() {
        return visible()
            .addAnimation(new AnimationAction.Animation<>(View.ALPHA, 1f))
            .build();
    }

    /**
     * Sets the visibility of the view to View.VISIBLE, fades it in and also set its translationX and translationY back to 0.
     */
    public static AnimationState<View> fadeInAndTranslateBack() {
        return visible()
            .addAnimations(
                new AnimationAction.Animation<>(View.ALPHA, 1f),
                new AnimationAction.Animation<>(View.TRANSLATION_X, 0f),
                new AnimationAction.Animation<>(View.TRANSLATION_Y, 0f))
            .build();
    }

    /**
     * Fades out the target and then sets its visibility to either View.INVISIBLE or GONE, depending on the gone parameter.
     */
    public static AnimationState<View> fadeOut(boolean gone) {
        return new ViewVisibilityBuilder(gone ? View.GONE : View.INVISIBLE)
            .addAnimation(new AnimationAction.Animation<>(View.ALPHA, 0f))
            .build();
    }

    /**
     * Fades out the target and then sets its visibility to either View.INVISIBLE or GONE, depending on the gone parameter.
     * Also moves the view by xTranslation and yTranslation.
     */
    public static AnimationState<View> fadeOutAndTranslate(boolean gone, float xTranslation, float yTranslation) {
        return new ViewVisibilityBuilder(gone ? View.GONE : View.INVISIBLE)
            .addAnimations(new AnimationAction.Animation<>(View.ALPHA, 0f),
                new AnimationAction.Animation<>(View.TRANSLATION_X, xTranslation),
                new AnimationAction.Animation<>(View.TRANSLATION_Y, yTranslation))
            .build();
    }

    /**
     * Fades out the target and then sets its visibility to either View.INVISIBLE or GONE, depending on the gone parameter.
     * Also moves the view horizontally by xTranslation.
     */
    public static AnimationState<View> fadeOutAndTranslateX(boolean gone, float xTranslation) {
        return new ViewVisibilityBuilder(gone ? View.GONE : View.INVISIBLE)
            .addAnimations(
                new AnimationAction.Animation<>(View.ALPHA, 0f),
                new AnimationAction.Animation<>(View.TRANSLATION_X, xTranslation)
            )
            .build();
    }

    /**
     * Fades out the target and then sets its visibility to either View.INVISIBLE or GONE, depending on the gone parameter.
     * Also moves the view vertically by yTranslation.
     */
    public static AnimationState<View> fadeOutAndTranslateY(boolean gone, float yTranslation) {
        return new ViewVisibilityBuilder(gone ? View.GONE : View.INVISIBLE)
            .addAnimations(
                new AnimationAction.Animation<>(View.ALPHA, 0f),
                new AnimationAction.Animation<>(View.TRANSLATION_Y, yTranslation))
            .build();
    }

    private List<AnimationAction.Animation<View>> mAnimations;
    private AnimationState.AnimationEndAction<View> mEndAction;
    private AnimationState.AnimationStartAction<View> mStartAction;

    public ViewVisibilityAnimation(int visibility, List<Animation<View>> animations) {
        switch (visibility) {
            case View.VISIBLE:
                mStartAction = view -> view.setVisibility(View.VISIBLE);
                break;
            case View.INVISIBLE:
                mEndAction = (view, wasCancelled) -> view.setVisibility(View.INVISIBLE);
                break;
            case View.GONE:
                mEndAction = (view, wasCancelled) -> view.setVisibility(View.GONE);
                break;
        }
        mAnimations = animations;
    }

    @Override
    public List<AnimationAction.Animation<View>> getAnimations() {
        return mAnimations;
    }

    @Override
    public AnimationEndAction<View> getAnimationEndAction() {
        return mEndAction;
    }

    @Override
    public AnimationStartAction<View> getAnimationStartAction() {
        return mStartAction;
    }
}
