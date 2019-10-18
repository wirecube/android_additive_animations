package at.wirecube.additiveanimations.additive_animator.animation_set;

public abstract class AnimationState<T extends Object> implements AnimationAction<T> {

    public interface AnimationStartAction<T> {
        void onStart(T target);
    }

    public interface AnimationEndAction<T> {
        void onEnd(T target, boolean wasCancelled);
    }

    /**
     * By default, the animations are only allowed to run if the current state of the animated object matches
     * this state.
     */
    public boolean shouldRun(AnimationState currentState) {
        return currentState == null || currentState == this;
    }

    /**
     * By default, the animationEndListener is only allowed to run if the current state of the animated object matches
     * this state.
     */
    public boolean shouldRunEndListener(AnimationState currentState) {
        return currentState == null || currentState == this;
    }

    public AnimationEndAction<T> getAnimationEndAction() {
        return null;
    }

    public AnimationStartAction<T> getAnimationStartAction() {
        return null;
    }
}
