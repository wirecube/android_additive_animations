package additive_animations.demo;

import android.animation.AnimatorSet;
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

public class MultipleViewsAnimationDemoFragment extends Fragment {
    FrameLayout rootView;
    View orangeView;
    View blueView;
    View greenView;
    View pinkView;
    int rotation = 0;

    private int distance(int alpha, int beta) {
        int phi = Math.abs(beta - alpha) % 360;       // This is either the distance or 360 - distance
        int distance = phi > 180 ? 360 - phi : phi;
        return distance;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = (FrameLayout) inflater.inflate(R.layout.fragment_multiple_views_demo, container, false);
        orangeView = rootView.findViewById(R.id.animated_view);
        blueView = rootView.findViewById(R.id.animated_view2);
        greenView = rootView.findViewById(R.id.animated_view3);
        pinkView = rootView.findViewById(R.id.animated_view4);

        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_UP) {
                    float x = event.getX();
                    float y = event.getY();

                    if(event.getAction() == MotionEvent.ACTION_UP) {
                        int numRotations = Math.abs(rotation)/360;
                        int sign = rotation < 0 ? -1 : 1;
                        rotation = sign*360*numRotations; // snap to 360°
                    } else if(x < rootView.getWidth()/2.0) {
                        rotation -= 10;
                    } else {
                        rotation += 10;
                    }

                    float width = rootView.getWidth();
                    float height = rootView.getHeight();

                    if(AdditiveAnimationsShowcaseActivity.ADDITIVE_ANIMATIONS_ENABLED) {
                        new AdditiveAnimator().setDuration(1000)
                                .setTarget(orangeView).x(x).y(y).rotation(rotation)
                                .setTarget(blueView).x(width - x - blueView.getWidth()).y(height - y).rotation(-rotation)
                                .setTarget(greenView).x(x).y(height - y).rotation(-rotation)
                                .setTarget(pinkView).x(width - x - pinkView.getWidth()).y(y).rotation(rotation)
                                .start();
                    } else {
                        AnimatorSet animatorSet = new AnimatorSet();
                        animatorSet.setInterpolator(EaseInOutPathInterpolator.create());
                        animatorSet.setDuration(1000);
                        animatorSet.playTogether(
                                ViewPropertyObjectAnimator.animate(orangeView).x(x).y(y).rotation(rotation).get(),
                                ViewPropertyObjectAnimator.animate(blueView).x(width - x - blueView.getWidth()).y(height - y).rotation(-rotation).get(),
                                ViewPropertyObjectAnimator.animate(greenView).x(x).y(height - y).rotation(-rotation).get(),
                                ViewPropertyObjectAnimator.animate(pinkView).x(width - x - pinkView.getWidth()).y(y).rotation(rotation).get()
                        );
                        animatorSet.start();
                    }

                    return true;
                }
                return true;
            }
        });
        return rootView;
    }
}
