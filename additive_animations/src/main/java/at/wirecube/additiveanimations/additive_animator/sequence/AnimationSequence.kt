package at.wirecube.additiveanimations.additive_animator.sequence

abstract class AnimationSequence {

    abstract fun start()
    abstract fun setDelayInSequence(delay: Long)
    abstract fun getTotalDurationInSequence(): Long

    companion object {
        @JvmStatic
        fun playTogether(vararg animations: AnimationSequence): AnimationSequence {
            return PlayTogetherAnimationSequence(animations.toList())
        }

        @JvmStatic
        fun playTogether(animations: List<AnimationSequence>): AnimationSequence {
            return PlayTogetherAnimationSequence(animations)
        }

        @JvmStatic
        fun playSequentially(vararg animations: AnimationSequence): AnimationSequence {
            return PlaySequentiallyAnimationSequence(animations.toList())
        }

        @JvmStatic
        fun playSequentially(animations: List<AnimationSequence>): AnimationSequence {
            return PlaySequentiallyAnimationSequence(animations)
        }

        @JvmStatic
        fun playWithDelayBetweenAnimations(stagger: Long, vararg animations: AnimationSequence): AnimationSequence {
            return PlayWithStaggerAnimationSequence(stagger, *animations)
        }
    }
}



