package at.wirecube.additiveanimations.additive_animator.sequence;

import java.util.Arrays;
import java.util.List;

public abstract class AnimationSequence {

    public abstract void start();
    public abstract void setDelayInSequence(long delay);
    public abstract long getTotalDurationInSequence();

    public static AnimationSequence playTogether(AnimationSequence... animations) {
        return new PlayTogetherAnimationSequence(Arrays.asList(animations));
    }

    public static AnimationSequence playTogether(List<AnimationSequence> animations) {
        return new PlayTogetherAnimationSequence(animations);
    }

    public static AnimationSequence playSequentially(AnimationSequence... animations) {
        return new PlaySequentiallyAnimationSequence(Arrays.asList(animations));
    }

    public static AnimationSequence playSequentially(List<AnimationSequence> animations) {
        return new PlaySequentiallyAnimationSequence(animations);
    }

    public static AnimationSequence playWithDelayBetweenAnimations(long stagger, AnimationSequence... animations) {
        return new PlayWithStaggerAnimationSequence(stagger, animations);
    }
}
