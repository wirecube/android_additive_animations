package additive_animations.fragments;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import additive_animations.AdditiveAnimationsShowcaseActivity;
import at.wirecube.additiveanimations.additive_animator.AdditiveAnimator;
import at.wirecube.additiveanimations.additiveanimationsdemo.R;
import at.wirecube.additiveanimations.helper.EaseInOutPathInterpolator;

public class TapToMoveDemoFragment extends Fragment {
    private FrameLayout rootView;
    private View animatedView;
    private View mTouchView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = (FrameLayout) inflater.inflate(R.layout.fragment_tap_to_move_demo, container, false);
        mTouchView = rootView.findViewById(R.id.touch_view);
        animatedView = rootView.findViewById(R.id.animated_view);

        rootView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_DOWN) {
                AdditiveAnimator.cancelAnimationsForObject(mTouchView);
                mTouchView.setAlpha(1);
                mTouchView.setX(event.getX() - mTouchView.getWidth()/2);
                mTouchView.setY(event.getY() - mTouchView.getHeight()/2);
                if(AdditiveAnimationsShowcaseActivity.ADDITIVE_ANIMATIONS_ENABLED) {
                    AdditiveAnimator.animate(animatedView)
                            .centerX(event.getX())
                            // uncomment the next line to see how you can use a different interpolator for each property!
//                                .switchInterpolator(new BounceInterpolator())
                            .centerY(event.getY())
                            .start();

                } else {
                    animatedView.animate().setInterpolator(EaseInOutPathInterpolator.create()).setDuration(1000)
                            .x(event.getX() - animatedView.getWidth() / 2)
                            .y(event.getY() - animatedView.getHeight() / 2)
                            .start();
                }
            }
            if(event.getAction() == MotionEvent.ACTION_UP) {
                AdditiveAnimator.animate(mTouchView, 100).alpha(0).start();
            }
            return true;
        });
        return rootView;
    }
}