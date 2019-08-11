package additive_animations.fragments.states;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import at.wirecube.additiveanimations.additive_animator.AdditiveAnimator;
import at.wirecube.additiveanimations.additive_animator.view_visibility.ViewVisibilityAnimation;
import at.wirecube.additiveanimations.additiveanimationsdemo.R;

public class StateDemoFragment extends Fragment {

    View rootView;
    View view1;
    View view2;
    View mTouchView;
    SwitchCompat mUseCustomTransitionSwitch;

    private boolean mFirstClick = false;
    private boolean mUseCustomTransition = false;

    private ViewVisibilityAnimation getGoneAnim() {
        return mUseCustomTransition ? CustomViewStateAnimation.GONE_ROTATE_AND_SCALE : ViewVisibilityAnimation.fadeOutAndTranslateX(true, 100f);
    }

    private ViewVisibilityAnimation getVisibleAnim() {
        return CustomViewStateAnimation.VISIBLE_ROTATE_AND_SCALE_BACK;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_state_demo, container, false);
        mTouchView = rootView.findViewById(R.id.touch_view);
        view1 = rootView.findViewById(R.id.animated_view);
        view2 = rootView.findViewById(R.id.animated_view2);

        mUseCustomTransitionSwitch = rootView.findViewById(R.id.sdf_custom_transition_switch);

        mUseCustomTransitionSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mUseCustomTransition = b;
            }
        });

        // A state can be set without any animation by using the `apply()` method - this will
        // cause the animation start/end actions to be called immediately.
        AdditiveAnimator.apply(getVisibleAnim(), view1);
        AdditiveAnimator.apply(getGoneAnim(), view2);

        rootView.setOnClickListener(view -> {
            mFirstClick = !mFirstClick;
            if (mFirstClick) {
                AdditiveAnimator.animate(view1)
                        .setDuration(300)
                        .visibility(getGoneAnim())
                        .thenWithDelay(150).target(view2)
                        .visibility(getVisibleAnim())
                        .start();
            } else {
                AdditiveAnimator.animate(view2)
                        .setDuration(300)
                        .visibility(getGoneAnim())
                        .thenWithDelay(150).target(view1)
                        .visibility(getVisibleAnim())
                        .start();
            }
        });
        return rootView;
    }
}