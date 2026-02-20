package at.wirecube.additiveanimations.additive_animator

/**
 * This is a small utility class which can animate any kind of object using the
 * [.property] and [.property] methods.
 * If you'd like to provide your own builder methods for creating animations, subclass [BaseAdditiveAnimator].
 */
class AdditiveObjectAnimator<V> : BaseAdditiveAnimator<AdditiveObjectAnimator<V?>, V?>() {
    private var mAnimationApplier: Runnable? = null

    protected override fun newInstance(): AdditiveObjectAnimator<V?> {
        return AdditiveObjectAnimator()
    }

    protected override fun setParent(other: AdditiveObjectAnimator<V?>): AdditiveObjectAnimator<V?> {
        val child = super.setParent(other)
        child.setAnimationApplier(mAnimationApplier)
        return child
    }

    @Suppress("UNCHECKED_CAST")
    fun setAnimationApplier(animationApplier: Runnable?): AdditiveObjectAnimator<V?> {
        mAnimationApplier = animationApplier
        return this as AdditiveObjectAnimator<V?>
    }

    override fun getCurrentPropertyValue(propertyName: String?): Float? {
        // AdditiveObjectAnimator only works with property-backed animations, so we don't need to implement this method
        return null
    }

    override fun onApplyChanges() {
        if (mAnimationApplier != null) {
            mAnimationApplier!!.run()
        }
    }

    companion object {
        fun <V> animate(target: V?): AdditiveObjectAnimator<V?>? {
            return AdditiveObjectAnimator<V?>().target(target)
        }

        fun <V> animate(target: V?, duration: Long): AdditiveObjectAnimator<V?>? {
            return animate<V?>(target)!!.setDuration(duration)
        }

        fun <V> create(): AdditiveObjectAnimator<V?> {
            return AdditiveObjectAnimator()
        }

        fun <V> create(duration: Long): AdditiveObjectAnimator<V?>? {
            return AdditiveObjectAnimator<V?>().setDuration(duration)
        }
    }
}
