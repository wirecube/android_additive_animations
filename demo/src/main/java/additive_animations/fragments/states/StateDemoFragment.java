package additive_animations.fragments.states;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.Fragment;

import at.wirecube.additiveanimations.additive_animator.AdditiveAnimator;
import at.wirecube.additiveanimations.additive_animator.animation_set.AnimationState;
import at.wirecube.additiveanimations.additive_animator.view_visibility.ViewVisibilityAnimation;
import at.wirecube.additiveanimations.additiveanimationsdemo.R;

public class StateDemoFragment extends Fragment {

    View rootView;
    View view1;
    View view2;
    View view3;
    SwitchCompat mUseCustomTransitionSwitch;

    private boolean mFirstClick = false;
    private boolean mUseCustomTransition = false;

    private AnimationState<View> getGoneAnim() {
        return mUseCustomTransition ? CustomViewStateAnimation.getCustomGoneAnimation() : ViewVisibilityAnimation.fadeOutAndTranslateX(true, 100f);
    }

    private AnimationState<View> getVisibleAnim() {
        return CustomViewStateAnimation.getCustomVisibleAnimation();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_state_demo, container, false);
        view1 = rootView.findViewById(R.id.animated_view);
        view2 = rootView.findViewById(R.id.animated_view2);
        view3 = rootView.findViewById(R.id.animated_view3);

        mUseCustomTransitionSwitch = rootView.findViewById(R.id.sdf_custom_transition_switch);

        mUseCustomTransitionSwitch.setOnCheckedChangeListener((compoundButton, b) -> mUseCustomTransition = b);

        // A state can be set without any animation by using the `apply()` method - this will
        // cause the animation start/end actions to be called immediately.
        AdditiveAnimator.apply(getVisibleAnim(), view1, view2);
        AdditiveAnimator.apply(getGoneAnim(), view3);

        rootView.setOnClickListener(view -> {
            mFirstClick = !mFirstClick;
            if (mFirstClick) {
                AdditiveAnimator.animate(view1, view2)
                    .setDuration(300)
                    .visibility(getGoneAnim())
                    .thenWithDelay(50).target(view3)
                    .visibility(getVisibleAnim())
                    .start();
            } else {
                AdditiveAnimator.animate(view3)
                    .setDuration(300)
                    .visibility(getGoneAnim())
                    .thenWithDelay(50).targets(view1, view2)
                    .visibility(getVisibleAnim())
                    .start();
            }
        });
        return rootView;
    }
}