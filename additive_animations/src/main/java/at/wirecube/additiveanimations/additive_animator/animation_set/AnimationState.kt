package at.wirecube.additiveanimations.additive_animator.animation_set

abstract class AnimationState<T : Any> : AnimationAction<T> {

    fun interface AnimationStartAction<T> {
        fun onStart(target: T)
    }

    fun interface AnimationEndAction<T> {
        fun onEnd(target: T, wasCancelled: Boolean)
    }

    open class Builder<BuilderSubclass : Builder<BuilderSubclass, T>, T : Any> {

        protected val animations: MutableList<AnimationAction.Animation<T>> = mutableListOf()

        protected var endAction: AnimationEndAction<T>? = null

        protected var startAction: AnimationStartAction<T>? = null

        @Suppress("UNCHECKED_CAST")
        private fun self(): BuilderSubclass = this as BuilderSubclass

        fun addAnimation(animation: AnimationAction.Animation<T>): BuilderSubclass {
            animations.add(animation)
            return self()
        }

        fun addAnimations(animations: List<AnimationAction.Animation<T>>): BuilderSubclass {
            this.animations.addAll(animations)
            return self()
        }

        @SafeVarargs
        fun addAnimations(vararg animations: AnimationAction.Animation<T>): BuilderSubclass {
            this.animations.addAll(animations)
            return self()
        }

        open fun withEndAction(endAction: AnimationEndAction<T>?): BuilderSubclass {
            this.endAction = endAction
            return self()
        }

        open fun withStartAction(startAction: AnimationStartAction<T>?): BuilderSubclass {
            this.startAction = startAction
            return self()
        }

        open fun build(): AnimationState<T> {
            val capturedAnimations = animations.toList()
            val capturedEndAction = endAction
            val capturedStartAction = startAction
            return object : AnimationState<T>() {
                override fun getAnimations(): List<AnimationAction.Animation<T>> = capturedAnimations
                override fun getAnimationEndAction(): AnimationEndAction<T>? = capturedEndAction
                override fun getAnimationStartAction(): AnimationStartAction<T>? = capturedStartAction
            }
        }
    }

    /**
     * The animations are only allowed to run if the current state of the animated object matches
     * this state.
     */
    fun shouldRun(currentState: AnimationState<T>?): Boolean {
        return currentState == null || currentState === this
    }

    /**
     * The animationEndListener is only allowed to run if the current state of the animated object matches
     * this state.
     */
    fun shouldRunEndListener(currentState: AnimationState<T>?): Boolean {
        return currentState == null || currentState === this
    }

    open fun getAnimationEndAction(): AnimationEndAction<T>? = null

    open fun getAnimationStartAction(): AnimationStartAction<T>? = null
}




