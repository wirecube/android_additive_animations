package additive_animations.fragments

import additive_animations.helper.DpConverter
import additive_animations.helper.UtilTimer
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.core.view.doOnPreDraw
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import at.wirecube.additiveanimations.additive_animator.AdditiveAnimator
import at.wirecube.additiveanimations.additiveanimationsdemo.databinding.FragmentExpanding2ButtonsDemoBinding

class ExpandingButtons2DemoFragment : Fragment() {

    private var timer: UtilTimer? = null
    private var hasCommittedAction = false

    lateinit var binding: FragmentExpanding2ButtonsDemoBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentExpanding2ButtonsDemoBinding.inflate(inflater)

        binding.root.doOnPreDraw {

            val initialHeight = binding.leftButton.height
            val initialWidth = binding.root.width / 2 - DpConverter.converDpToPx(16f) * 3
            val anticipationWidth = binding.root.width - DpConverter.converDpToPx(16f) * 2
            val expandedHeight = binding.root.height
            val expandedWidth = binding.root.width

            binding.leftButton.updateLayoutParams<FrameLayout.LayoutParams> {
                width = initialWidth
            }
            binding.rightButton.updateLayoutParams<FrameLayout.LayoutParams> {
                width = initialWidth
            }

            val onTouchListener = OnTouchListener { view, event ->
                val anticipationDuration = 500L
                val expansionDelay = 0L
                val expansionDuration = 200L

                if (hasCommittedAction) {
                    view.isPressed = false
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        // reset state for testing
                        hasCommittedAction = false
                        AdditiveAnimator.animate(binding.leftButton, binding.rightButton)
                            .setDuration(expansionDuration)
                            .width(initialWidth)
                            .height(initialHeight)
                            .margin(DpConverter.converDpToPx(16f))
                            .targets(binding.leftButton, binding.rightButton, binding.centerView)
                            .translationY(0f)
                            .start()
                    }
                    return@OnTouchListener true
                }

                val otherButton =
                    if (view == binding.leftButton) binding.rightButton else binding.leftButton

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    view.drawableHotspotChanged(event.x, event.y)
                }

                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        view.bringToFront()
                        view.isPressed = true

                        // anticipation animation:
                        AdditiveAnimator.animate(view)
                            .setDuration(anticipationDuration)
                            .width(anticipationWidth)
                            .target(otherButton)
                            .alpha(0f)
                            .start()

                        timer = UtilTimer {
                            hasCommittedAction = true
                            // expansion animation:
                            AdditiveAnimator.animate(view)
                                .setDuration(expansionDuration)
                                .width(expandedWidth)
                                .height(expandedHeight)
                                .margin(0)
                                .target(otherButton)
                                .alpha(0f)
                                .start()
                        }
                        timer?.startTimer(delay = anticipationDuration + expansionDelay)
                    }

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        view.isPressed = false
                        timer?.cancelTimer()
                        // collapse animation:
                        AdditiveAnimator.animate(view)
                            .setDuration(anticipationDuration)
                            .width(initialWidth)
                            .target(otherButton)
                            .alpha(1f)
                            .start()
                    }
                }
                true
            }

            binding.leftButton.setOnTouchListener(onTouchListener)
            binding.rightButton.setOnTouchListener(onTouchListener)
        }
        return binding.root
    }
}