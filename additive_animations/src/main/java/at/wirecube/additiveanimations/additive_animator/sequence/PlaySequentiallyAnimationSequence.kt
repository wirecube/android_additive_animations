package at.wirecube.additiveanimations.additive_animator.sequence;

import java.util.List;

class PlaySequentiallyAnimationSequence extends AnimationSequence {
    private final List<AnimationSequence> animations;
    private long delay = 0;

    public PlaySequentiallyAnimationSequence(List<AnimationSequence> animations) {
        this.animations = animations;
    }

    @Override
    public void start() {
        long totalDelay = 0;
        for(AnimationSequence sequence : animations) {
            sequence.setDelayInSequence(totalDelay + this.delay);
            totalDelay += sequence.getTotalDurationInSequence();
            sequence.start();
        }
    }

    @Override
    public void setDelayInSequence(long delay) {
        this.delay = delay;
    }

    @Override
    public long getTotalDurationInSequence() {
        long totalDelay = delay;
        for(AnimationSequence sequence : animations) {
            totalDelay += sequence.getTotalDurationInSequence();
        }
        return totalDelay + this.delay;
    }
}
