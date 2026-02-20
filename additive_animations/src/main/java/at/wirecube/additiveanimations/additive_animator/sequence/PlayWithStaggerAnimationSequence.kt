package at.wirecube.additiveanimations.additive_animator.sequence

class PlayWithStaggerAnimationSequence(
    private val stagger: Long,
    vararg animations: AnimationSequence
) : AnimationSequence() {

    private val animations: List<AnimationSequence> = animations.toList()
    private var delayInSequence: Long = 0

    override fun start() {
        var totalDelay: Long = 0
        for (sequence in animations) {
            sequence.setDelayInSequence(totalDelay + delayInSequence)
            totalDelay += stagger
            sequence.start()
        }
    }

    override fun setDelayInSequence(delay: Long) {
        this.delayInSequence = delay
    }

    override fun getTotalDurationInSequence(): Long {
        var longestDuration: Long = 0
        var currentStagger: Long = 0
        for (sequence in animations) {
            val duration = sequence.getTotalDurationInSequence() + currentStagger
            if (duration > longestDuration) {
                longestDuration = duration
            }
            currentStagger += stagger
        }
        return longestDuration + delayInSequence
    }
}

