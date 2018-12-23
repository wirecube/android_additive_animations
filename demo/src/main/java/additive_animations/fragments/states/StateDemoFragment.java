package additive_animations.fragments.states;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import java.util.Arrays;
import java.util.List;

import at.wirecube.additiveanimations.additive_animator.AdditiveAnimation;
import at.wirecube.additiveanimations.additive_animator.AdditiveAnimator;
import at.wirecube.additiveanimations.additive_animator.AnimationEndListener;
import at.wirecube.additiveanimations.additive_animator.animation_set.AnimationState;
import at.wirecube.additiveanimations.additiveanimationsdemo.R;

public class StateDemoFragment extends Fragment {

    FrameLayout rootView;
    View view1;
    View view2;
    View mTouchView;

    private boolean mFirstClick = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = (FrameLayout) inflater.inflate(R.layout.fragment_state_demo, container, false);
        mTouchView = rootView.findViewById(R.id.touch_view);
        view1 = rootView.findViewById(R.id.animated_view);
        view2 = rootView.findViewById(R.id.animated_view2);

        // set initial state:
        AdditiveAnimator.apply(AnimationStates.VISIBLE, view1);
        AdditiveAnimator.apply(AnimationStates.HIDDEN, view2);

        AdditiveAnimator.setDefaultDuration(300);

        rootView.setOnClickListener(view -> {
            mFirstClick = !mFirstClick;
            if (mFirstClick) {
                AdditiveAnimator.animate(view1).state(AnimationStates.HIDDEN)
                        .thenWithDelay(150).target(view2).state(AnimationStates.VISIBLE)
                        .start();
            } else {
                AdditiveAnimator.animate(view2).state(AnimationStates.HIDDEN)
                        .thenWithDelay(150).target(view1).state(AnimationStates.VISIBLE)
                        .start();
            }
        });
        return rootView;
    }
}