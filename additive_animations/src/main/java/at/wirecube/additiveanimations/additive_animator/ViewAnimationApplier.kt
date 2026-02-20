package at.wirecube.additiveanimations.additive_animator
import android.view.View
class ViewAnimationApplier @JvmOverloads constructor(
    private val target: View,
    private val requestLayout: Boolean = false
) : Runnable {
    override fun run() {
        if (requestLayout) {
            target.requestLayout()
        } else {
            target.invalidate()
        }
    }
}
