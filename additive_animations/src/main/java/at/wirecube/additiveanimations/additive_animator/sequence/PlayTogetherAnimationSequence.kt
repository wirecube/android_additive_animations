package at.wirecube.additiveanimations.additive_animator.sequence;

import java.util.List;

public class PlayTogetherAnimationSequence extends AnimationSequence {

    private final List<AnimationSequence> animations;
    private long delayInSequence;

    PlayTogetherAnimationSequence(List<AnimationSequence> animations) {
        this.animations = animations;
    }

    @Override
    public void start() {
        for (AnimationSequence sequence : animations) {
            sequence.setDelayInSequence(delayInSequence);
            sequence.start();
        }
    }

    @Override
    public void setDelayInSequence(long delay) {
        this.delayInSequence = delay;
    }

    @Override
    public long getTotalDurationInSequence() {
        long longestDuration = 0;
        for (AnimationSequence sequence : animations) {
            if(sequence.getTotalDurationInSequence() > longestDuration) {
                longestDuration = sequence.getTotalDurationInSequence();
            }
        }
        return longestDuration + delayInSequence;
    }
}
