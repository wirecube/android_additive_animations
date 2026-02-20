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
import at.wirecube.additiveanimations.additiveanimationsdemo.databinding.FragmentExpandingButtonsDemoBinding

class ExpandingButtonsDemoFragment : Fragment() {

    private var timer: UtilTimer? = null
    private var hasCommittedAction = false

    lateinit var binding: FragmentExpandingButtonsDemoBinding

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentExpandingButtonsDemoBinding.inflate(inflater)

        binding.root.doOnPreDraw {

            val initialHeight = binding.root.height / 3
            val anticipationHeight = initialHeight + DpConverter.converDpToPx(50f)
            val expandedHeight = binding.root.height

            binding.topButton.updateLayoutParams<FrameLayout.LayoutParams> {
                height = initialHeight
            }
            binding.bottomButton.updateLayoutParams<FrameLayout.LayoutParams> {
                height = initialHeight
            }

            val onTouchListener = OnTouchListener { view, event ->
                val anticipationDuration = 500L
                val expansionDelay = 0L
                val expansionDuration = 200L
                val collapseDuration = anticipationDuration

                if (hasCommittedAction) {
                    view.isPressed = false
                    if (event.action == MotionEvent.ACTION_DOWN) {
                        // reset state for testing
                        hasCommittedAction = false
                        AdditiveAnimator.animate(binding.bottomButton, binding.topButton)
                            .setDuration(collapseDuration)
                            .height(initialHeight)
                            .targets(binding.bottomButton, binding.topButton, binding.bottomButton)
                            .translationY(0f)
                            .start()
                    }
                    return@OnTouchListener true
                }

                val otherViews = listOf(
                    binding.topButton,
                    binding.bottomButton,
                    binding.centerView
                ).filter { it != view }

                val translationY = DpConverter.converDpToPx(20f).toFloat()
                val anticipationTranslationY =
                    if (view == binding.topButton) translationY else -translationY
                val expansionTranslationY = anticipationTranslationY * 3

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
                            .height(anticipationHeight)
                            .targets(otherViews)
                            .translationY(anticipationTranslationY)
                            .start()

                        timer = UtilTimer {
                            hasCommittedAction = true
                            // expansion animation:
                            AdditiveAnimator.animate(view)
                                .setDuration(expansionDuration)
                                .height(expandedHeight)
                                .targets(otherViews)
                                .translationY(expansionTranslationY)
                                .start()
                        }
                        timer?.startTimer(delay = anticipationDuration + expansionDelay)
                    }

                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        view.isPressed = false
                        timer?.cancelTimer()
                        // collapse animation:
                        AdditiveAnimator.animate(view)
                            .setDuration(collapseDuration)
                            .height(initialHeight)
                            .targets(otherViews)
                            .translationY(0f)
                            .start()
                    }
                }
                true
            }

            binding.topButton.setOnTouchListener(onTouchListener)
            binding.bottomButton.setOnTouchListener(onTouchListener)
        }
        return binding.root
    }
}