package at.wirecube.additiveanimations.additive_animator.sequence

internal class PlaySequentiallyAnimationSequence(
    private val animations: List<AnimationSequence>
) : AnimationSequence() {

    private var delay: Long = 0

    override fun start() {
        var totalDelay: Long = 0
        for (sequence in animations) {
            sequence.setDelayInSequence(totalDelay + delay)
            totalDelay += sequence.getTotalDurationInSequence()
            sequence.start()
        }
    }

    override fun setDelayInSequence(delay: Long) {
        this.delay = delay
    }

    override fun getTotalDurationInSequence(): Long {
        var totalDelay = delay
        for (sequence in animations) {
            totalDelay += sequence.getTotalDurationInSequence()
        }
        return totalDelay + delay
    }
}

