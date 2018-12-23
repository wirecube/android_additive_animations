package additive_animations.fragments.states;

import android.view.View;

import java.util.Arrays;
import java.util.List;

import at.wirecube.additiveanimations.additive_animator.animation_set.AnimationAction;
import at.wirecube.additiveanimations.additive_animator.animation_set.AnimationState;

public class AnimationStates {
    public static final AnimationState<View> HIDDEN = new AnimationState<View>() {
        @Override public int getId() { return 1; }

        @Override public List<AnimationAction.Animation<View>> getAnimations() {
            return Arrays.asList(
                    new AnimationAction.Animation<>(View.ALPHA, 0f),
                    new AnimationAction.Animation<>(View.TRANSLATION_X, 100)
            );
        }

        @Override public AnimationState.AnimationEndAction<View> getAnimationEndAction() {
            return (target, wasCancelled) -> target.setVisibility(View.GONE);
        }
    };

    public final static AnimationState<View> VISIBLE = new AnimationState<View>() {
        @Override public int getId() { return 2; }

        @Override public List<Animation<View>> getAnimations() {
            return Arrays.asList(
                    new Animation<>(View.ALPHA, 1f),
                    new Animation<>(View.TRANSLATION_X, 0)
            );
        }

        @Override public AnimationStartAction<View> getAnimationStartAction() {
            return view -> view.setVisibility(View.VISIBLE);
        }
    };
}
