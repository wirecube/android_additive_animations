package additive_animations.fragments.states;

import android.view.View;

import androidx.annotation.Nullable;

import java.util.Arrays;

import at.wirecube.additiveanimations.additive_animator.animation_set.AnimationAction;
import at.wirecube.additiveanimations.additive_animator.animation_set.AnimationState;
import at.wirecube.additiveanimations.additive_animator.view_visibility.ViewVisibilityAnimation;

public class CustomViewStateAnimation {

    public static AnimationState<View> getCustomGoneAnimation() {
        return ViewVisibilityAnimation.gone()
            .addAnimations(
                new AnimationAction.Animation<>(View.ALPHA, 0f),
                new AnimationAction.Animation<>(View.ROTATION, 90),
                new AnimationAction.Animation<>(View.SCALE_X, 0.1f),
                new AnimationAction.Animation<>(View.SCALE_Y, 0.1f)
            )
            .build();
    }

    public static AnimationState<View> getCustomVisibleAnimation() {
        return ViewVisibilityAnimation.visible()
            .addAnimations(
                new AnimationAction.Animation<>(View.ALPHA, 1f),
                new AnimationAction.Animation<>(View.ROTATION, 0f),
                new AnimationAction.Animation<>(View.SCALE_X, 1f),
                new AnimationAction.Animation<>(View.SCALE_Y, 1f),
                new AnimationAction.Animation<>(View.TRANSLATION_X, 0f)
            )
            .build();
    }
}
