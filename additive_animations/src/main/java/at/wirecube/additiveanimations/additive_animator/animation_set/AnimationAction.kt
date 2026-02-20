package at.wirecube.additiveanimations.additive_animator.animation_set
import android.animation.TypeEvaluator
import android.util.Property

interface AnimationAction<T> {
    open class Animation<T> @JvmOverloads constructor(
        val property: Property<T, Float>,
        val targetValue: Float,
        val typeEvaluator: TypeEvaluator<Float>? = null
    )
    fun getAnimations(): List<Animation<T>>
}
