package at.wirecube.additiveanimations.additive_animator.animation_set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class AnimationState<T> implements AnimationAction<T> {

    public static class Builder<BuilderSubclass extends Builder<?, T>, T> {

        @NonNull
        protected final List<Animation<T>> animations = new ArrayList<>();

        @Nullable
        protected AnimationEndAction<T> endAction;

        @Nullable
        protected AnimationStartAction<T> startAction;

        public Builder() {}

        @NonNull
        public BuilderSubclass addAnimation(@NonNull AnimationAction.Animation<T> animation) {
            animations.add(animation);
            return (BuilderSubclass) this;
        }

        @NonNull
        public BuilderSubclass addAnimations(@NonNull List<AnimationAction.Animation<T>> animations) {
            this.animations.addAll(animations);
            return (BuilderSubclass) this;
        }

        @SafeVarargs
        @NonNull
        public final BuilderSubclass addAnimations(@NonNull AnimationAction.Animation<T>... animations) {
            this.animations.addAll(Arrays.asList(animations));
            return (BuilderSubclass) this;
        }

        @NonNull
        public BuilderSubclass withEndAction(@Nullable AnimationEndAction<T> endAction) {
            this.endAction = endAction;
            return (BuilderSubclass) this;
        }

        @NonNull
        public BuilderSubclass withStartAction(@Nullable AnimationStartAction<T> startAction) {
            this.startAction = startAction;
            return (BuilderSubclass) this;
        }

        public AnimationState<T> build() {
            return new AnimationState<T>() {
                @Override
                public List<Animation<T>> getAnimations() {
                    return animations;
                }

                @Override
                public AnimationEndAction<T> getAnimationEndAction() {
                    return endAction;
                }

                @Override
                public AnimationStartAction<T> getAnimationStartAction() {
                    return startAction;
                }
            };
        }
    }

    public interface AnimationStartAction<T> {
        void onStart(T target);
    }

    public interface AnimationEndAction<T> {
        void onEnd(T target, boolean wasCancelled);
    }

    /**
     * The animations are only allowed to run if the current state of the animated object matches
     * this state.
     */
    public final boolean shouldRun(AnimationState<T> currentState) {
        return currentState == null || currentState == this;
    }

    /**
     * The animationEndListener is only allowed to run if the current state of the animated object matches
     * this state.
     */
    public final boolean shouldRunEndListener(AnimationState<T> currentState) {
        return currentState == null || currentState == this;
    }

    public AnimationEndAction<T> getAnimationEndAction() {
        return null;
    }

    public AnimationStartAction<T> getAnimationStartAction() {
        return null;
    }
}
