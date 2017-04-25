package additive_animations.demo;

import android.animation.ValueAnimator;
import android.graphics.Path;
import android.os.Bundle;
import android.renderscript.Sampler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;

import com.bartoszlipinski.viewpropertyobjectanimator.ViewPropertyObjectAnimator;

import at.wirecube.additiveanimations.additive_animator.AdditiveAnimator;
import at.wirecube.additiveanimations.additiveanimationsdemo.R;
import at.wirecube.additiveanimations.helper.EaseInOutPathInterpolator;

public class MoveAlongPathDemoFragment extends Fragment {
    FrameLayout rootView;
    View animatedView;

    int circleRadius = 100; // TODO: dp to pixels

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = (FrameLayout) inflater.inflate(R.layout.fragment_tap_to_move_demo, container, false);
        animatedView = rootView.findViewById(R.id.animated_view);

        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_DOWN) {
                    if(AdditiveAnimationsShowcaseActivity.ADDITIVE_ANIMATIONS_ENABLED) {
                        AdditiveAnimator.animate(animatedView).setDuration(1000)
                                .x(event.getX())
                                .y(event.getY())
                                .start();
                    }
                }
                return true;
            }
        });

        // wait for rootView to layout itself so we can get its center
        rootView.post(new Runnable() {
            @Override
            public void run() {
                if (AdditiveAnimationsShowcaseActivity.ADDITIVE_ANIMATIONS_ENABLED) {
                    // small circle
                    final Path path1 = new Path();
                    path1.addCircle(rootView.getWidth() / 2, rootView.getHeight() / 2, circleRadius, Path.Direction.CW);
                    AdditiveAnimator.animate(animatedView).setDuration(1000).setInterpolator(new LinearInterpolator())
                            .xyAlongPath(path1)
                            .setRepeatCount(ValueAnimator.INFINITE)
                            .start();

                    // another circle which also updates rotation to better show where on the path we are
                    final Path path2 = new Path();
                    path2.addCircle(rootView.getWidth() / 2, rootView.getHeight() / 2, rootView.getWidth() / 4, Path.Direction.CW);
                    AdditiveAnimator.animate(animatedView).setDuration(3200).setInterpolator(new LinearInterpolator())
                            .xyRotationAlongPath(path2)
                            .setRepeatCount(ValueAnimator.INFINITE)
                            .start();
                } else {
                    // TODO
                }
            }
        });
        return rootView;
    }
}