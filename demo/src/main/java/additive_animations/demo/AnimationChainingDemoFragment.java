package additive_animations.demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.bartoszlipinski.viewpropertyobjectanimator.ViewPropertyObjectAnimator;

import at.wirecube.additiveanimations.additive_animator.AdditiveAnimator;
import at.wirecube.additiveanimations.additiveanimationsdemo.R;
import at.wirecube.additiveanimations.helper.EaseInOutPathInterpolator;

public class AnimationChainingDemoFragment extends Fragment {
    FrameLayout rootView;
    View animatedView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = (FrameLayout) inflater.inflate(R.layout.fragment_tap_to_move_demo, container, false);
        animatedView = rootView.findViewById(R.id.animated_view);

        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, final MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_DOWN) {
                    if(AdditiveAnimationsShowcaseActivity.ADDITIVE_ANIMATIONS_ENABLED) {
                        AdditiveAnimator.animate(animatedView).setDuration(1000)
                                .centerX(event.getX()).rotationBy(45)
                                .thenAfter(250).setDuration(800)
                                .centerY(event.getY()).rotationBy(45)
                                .thenAfter(250).setDuration(1200)
                                .x(animatedView.getX()).rotationBy(45)
                                .thenAfter(250).setDuration(800)
                                .y(animatedView.getY()).rotationBy(45)
                                .start();
                    } else {
                        ViewPropertyObjectAnimator.animate(animatedView).setInterpolator(EaseInOutPathInterpolator.create()).setDuration(1000)
                                .x(event.getX() - animatedView.getWidth() / 2)
                                .withEndAction(new Runnable() {
                                    @Override
                                    public void run() {
                                        ViewPropertyObjectAnimator.animate(animatedView).setInterpolator(EaseInOutPathInterpolator.create()).setDuration(1000)
                                                .y(event.getY() - animatedView.getHeight() / 2)
                                                .start();
                                    }
                                })
                                .start();
                    }
                }
                return true;
            }
        });
        return rootView;
    }
}