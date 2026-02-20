package at.wirecube.additiveanimations.additive_animator.sequence

class PlayTogetherAnimationSequence internal constructor(
    private val animations: List<AnimationSequence>
) : AnimationSequence() {

    private var delayInSequence: Long = 0

    override fun start() {
        for (sequence in animations) {
            sequence.setDelayInSequence(delayInSequence)
            sequence.start()
        }
    }

    override fun setDelayInSequence(delay: Long) {
        this.delayInSequence = delay
    }

    override fun getTotalDurationInSequence(): Long {
        var longestDuration: Long = 0
        for (sequence in animations) {
            val duration = sequence.getTotalDurationInSequence()
            if (duration > longestDuration) {
                longestDuration = duration
            }
        }
        return longestDuration + delayInSequence
    }
}

