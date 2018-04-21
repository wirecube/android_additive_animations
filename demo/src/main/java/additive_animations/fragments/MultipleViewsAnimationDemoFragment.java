package additive_animations.fragments;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.graphics.Path;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.bartoszlipinski.viewpropertyobjectanimator.ViewPropertyObjectAnimator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import additive_animations.AdditiveAnimationsShowcaseActivity;
import at.wirecube.additiveanimations.additive_animator.AdditiveAnimator;
import at.wirecube.additiveanimations.additiveanimationsdemo.R;
import at.wirecube.additiveanimations.helper.EaseInOutPathInterpolator;
import at.wirecube.additiveanimations.helper.evaluators.PathEvaluator;

public class MultipleViewsAnimationDemoFragment extends Fragment {
    FrameLayout rootView;
    int rotation = 0;

    List<View> views = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = (FrameLayout) inflater.inflate(R.layout.fragment_multiple_views_demo, container, false);
        views = Arrays.asList(
                rootView.findViewById(R.id.animated_view4), rootView.findViewById(R.id.animated_view5),
                rootView.findViewById(R.id.animated_view6), rootView.findViewById(R.id.animated_view7),
                rootView.findViewById(R.id.animated_view8), rootView.findViewById(R.id.animated_view9),
                rootView.findViewById(R.id.animated_view10), rootView.findViewById(R.id.animated_view11),
                rootView.findViewById(R.id.animated_view12), rootView.findViewById(R.id.animated_view13),
                rootView.findViewById(R.id.animated_view14), rootView.findViewById(R.id.animated_view15),
                rootView.findViewById(R.id.animated_view16), rootView.findViewById(R.id.animated_view17),
                rootView.findViewById(R.id.animated_view18), rootView.findViewById(R.id.animated_view19),
                rootView.findViewById(R.id.animated_view20), rootView.findViewById(R.id.animated_view21),
                rootView.findViewById(R.id.animated_view22), rootView.findViewById(R.id.animated_view23),
                rootView.findViewById(R.id.animated_view24), rootView.findViewById(R.id.animated_view25));

        for(View v : views) {
            v.setAlpha(0.2f);
        }

        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_UP) {
                    float x = event.getX();
                    float y = event.getY();

                    if(event.getAction() == MotionEvent.ACTION_UP && AdditiveAnimationsShowcaseActivity.ADDITIVE_ANIMATIONS_ENABLED) {
                        // snap to 360° only when using additive animations - you won't ever see the views rotate without additive animations otherwise.
                        rotation = 0;
                    } else if(x < rootView.getWidth()/2.0) {
                        rotation -= 10;
                    } else {
                        rotation += 10;
                    }

                    long animationStagger = 50;

                    if(AdditiveAnimationsShowcaseActivity.ADDITIVE_ANIMATIONS_ENABLED) {
                        AdditiveAnimator.animate(views, animationStagger).withLayer().x(x).y(y).rotation(rotation).start();
                        // The above line is equivalent to this loop:
//                    AdditiveAnimator animator = new AdditiveAnimator();
//                    for(View view : mViews) {
//                        animator = animator.target(view).x(x).y(y).rotation(rotation).thenWithDelay(50);
//                    }
//                    animator.start();
                    } else {
                        for(int i = 0; i < views.size(); i++) {
                            ViewPropertyObjectAnimator.animate(views.get(i))
                                    .setStartDelay(animationStagger * i)
                                    .withLayer()
                                    .setDuration(1000)
                                    .x(x)
                                    .y(y)
                                    .rotation(rotation)
                                    .start();
                        }
//                        AnimatorSet animatorSet = new AnimatorSet();
//                        animatorSet.setInterpolator(EaseInOutPathInterpolator.create());
//                        animatorSet.setDuration(1000);
//                        List<Animator> animators = new ArrayList<>();
//                        for(View view : views) {
//                            animators.add(ViewPropertyObjectAnimator.animate(view).x(x).y(y).rotation(-rotation).get());
//                        }
//                        animatorSet.playTogether(animators);
//                        animatorSet.start();
                    }
                }
                return true;
            }
        });
        return rootView;
    }
}
