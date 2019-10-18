package at.wirecube.additiveanimations.additive_animator.view_visibility;

import android.view.View;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Random;

import at.wirecube.additiveanimations.additive_animator.animation_set.AnimationAction;
import at.wirecube.additiveanimations.additive_animator.animation_set.AnimationState;


public class ViewVisibilityAnimation extends AnimationState<View> {

    /**
     * Sets the visibility of the view to View.VISIBLE and fades it in.
     */
    public static ViewVisibilityAnimation fadeIn() {
        return new ViewVisibilityAnimation(View.VISIBLE,
                Collections.singletonList(new AnimationAction.Animation<View>(View.ALPHA, 1f)));
    }

    /**
     * Sets the visibility of the view to View.VISIBLE, fades it in and also set its translationX and translationY back to 0.
     */
    public static ViewVisibilityAnimation fadeInAndTranslateBack() {
        return new ViewVisibilityAnimation(View.VISIBLE,
                Arrays.asList(
                        new AnimationAction.Animation<>(View.ALPHA, 1f),
                        new AnimationAction.Animation<>(View.TRANSLATION_X, 0f),
                        new AnimationAction.Animation<>(View.TRANSLATION_Y, 0f)));
    }

    /**
     * Fades out the target and then sets its visibility to either View.INVISIBLE or GONE, depending on the gone parameter.
     */
    public static ViewVisibilityAnimation fadeOut(boolean gone) {
        return new ViewVisibilityAnimation(gone ? View.GONE : View.INVISIBLE,
                Collections.singletonList(new AnimationAction.Animation<>(View.ALPHA, 0f)));
    }

    /**
     * Fades out the target and then sets its visibility to either View.INVISIBLE or GONE, depending on the gone parameter.
     * Also moves the view by xTranslation and yTranslation.
     */
    public static ViewVisibilityAnimation fadeOutAndTranslate(boolean gone, float xTranslation, float yTranslation) {
        return new ViewVisibilityAnimation(gone ? View.GONE : View.INVISIBLE,
                Arrays.asList(
                        new AnimationAction.Animation<>(View.ALPHA, 0f),
                        new AnimationAction.Animation<>(View.TRANSLATION_X, xTranslation),
                        new AnimationAction.Animation<>(View.TRANSLATION_Y, yTranslation)));
    }

    /**
     * Fades out the target and then sets its visibility to either View.INVISIBLE or GONE, depending on the gone parameter.
     * Also moves the view horizontally by xTranslation.
     */
    public static ViewVisibilityAnimation fadeOutAndTranslateX(boolean gone, float xTranslation) {
        return new ViewVisibilityAnimation(gone ? View.GONE : View.INVISIBLE,
                Arrays.asList(
                        new AnimationAction.Animation<>(View.ALPHA, 0f),
                        new AnimationAction.Animation<>(View.TRANSLATION_X, xTranslation)));
    }

    /**
     * Fades out the target and then sets its visibility to either View.INVISIBLE or GONE, depending on the gone parameter.
     * Also moves the view vertically by yTranslation.
     */
    public static ViewVisibilityAnimation fadeOutAndTranslateY(boolean gone, float yTranslation) {
        return new ViewVisibilityAnimation(gone ? View.GONE : View.INVISIBLE,
                Arrays.asList(
                        new AnimationAction.Animation<>(View.ALPHA, 0f),
                        new AnimationAction.Animation<>(View.TRANSLATION_Y, yTranslation)));
    }


    private List<AnimationAction.Animation<View>> mAnimations;
    private AnimationState.AnimationEndAction<View> mEndAction;
    private AnimationState.AnimationStartAction<View> mStartAction;

    public ViewVisibilityAnimation(int visibility, List<AnimationAction.Animation<View>> animations) {
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
