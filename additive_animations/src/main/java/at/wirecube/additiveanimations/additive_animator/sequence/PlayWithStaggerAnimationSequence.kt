package at.wirecube.additiveanimations.additive_animator.sequence;

import java.util.Arrays;
import java.util.List;

public class PlayWithStaggerAnimationSequence extends AnimationSequence {

    private final List<AnimationSequence> animations;
    private final long stagger;
    private long delayInSequence;

    public PlayWithStaggerAnimationSequence(long stagger, AnimationSequence... animations) {
        this.stagger = stagger;
        this.animations = Arrays.asList(animations);
    }


    @Override
    public void start() {
        long totalDelay = 0;
        for(AnimationSequence sequence : animations) {
            sequence.setDelayInSequence(totalDelay + this.delayInSequence);
            totalDelay += this.stagger;
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
        long currentStagger = 0;
        for (AnimationSequence sequence : animations) {
            long duration = sequence.getTotalDurationInSequence() + currentStagger;
            if(duration > longestDuration) {
                longestDuration = duration;
            }
            currentStagger += stagger;
        }
        return longestDuration + delayInSequence;
    }
}
