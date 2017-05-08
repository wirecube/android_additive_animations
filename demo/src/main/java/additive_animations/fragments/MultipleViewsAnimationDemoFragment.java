package additive_animations.fragments;

import android.animation.Animator;
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

import java.util.ArrayList;
import java.util.List;

import additive_animations.AdditiveAnimationsShowcaseActivity;
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

    List<View> views = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = (FrameLayout) inflater.inflate(R.layout.fragment_multiple_views_demo, container, false);
        orangeView = rootView.findViewById(R.id.animated_view);
        blueView = rootView.findViewById(R.id.animated_view2);
        greenView = rootView.findViewById(R.id.animated_view3);
        pinkView = rootView.findViewById(R.id.animated_view4);

        for(int i = 5; i < 25; i++) {
            views.add(rootView.findViewById(R.id.animated_view4 + i-4));
        }

        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN /*|| event.getAction() == MotionEvent.ACTION_MOVE */ || event.getAction() == MotionEvent.ACTION_UP) {
                    float x = event.getX();
                    float y = event.getY();

                    if(event.getAction() == MotionEvent.ACTION_UP && AdditiveAnimationsShowcaseActivity.ADDITIVE_ANIMATIONS_ENABLED) {
                        // snap to 360°
                        rotation = 0;
                    } else if(x < rootView.getWidth()/2.0) {
                        rotation -= 10;
                    } else {
                        rotation += 10;
                    }

                    float width = rootView.getWidth();
                    float height = rootView.getHeight();

                    if(AdditiveAnimationsShowcaseActivity.ADDITIVE_ANIMATIONS_ENABLED) {
                        AdditiveAnimator animator = new AdditiveAnimator().setDuration(1000)
                                .addTarget(orangeView).x(x).y(y).rotation(rotation)
                                .addTarget(blueView).x(width - x - blueView.getWidth()).y(height - y).rotation(-rotation)
                                .addTarget(greenView).x(x).y(height - y).rotation(-rotation)
                                .addTarget(pinkView).x(width - x - pinkView.getWidth()).y(y).rotation(rotation);
                        for(View view : views) {
                            animator.addTarget(view).x(x).y(y).rotation(-rotation);
                        }
                        animator.start();
                    } else {
                        AnimatorSet animatorSet = new AnimatorSet();
                        animatorSet.setInterpolator(EaseInOutPathInterpolator.create());
                        animatorSet.setDuration(1000);
                        List<Animator> animators = new ArrayList<>();
                        animators.add(ViewPropertyObjectAnimator.animate(orangeView).x(x).y(y).rotation(rotation).get());
                        animators.add(ViewPropertyObjectAnimator.animate(blueView).x(width - x - blueView.getWidth()).y(height - y).rotation(-rotation).get());
                        animators.add(ViewPropertyObjectAnimator.animate(greenView).x(x).y(height - y).rotation(-rotation).get());
                        animators.add(ViewPropertyObjectAnimator.animate(pinkView).x(width - x - pinkView.getWidth()).y(y).rotation(rotation).get());
                        for(View view :views) {
                            animators.add(ViewPropertyObjectAnimator.animate(view).x(x).y(y).rotation(-rotation).get());
                        }
                        animatorSet.playTogether(animators);
                        animatorSet.start();
                    }
                }
                return true;
            }
        });
        return rootView;
    }
}
