package additive_animations.fragments;

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.renderscript.Sampler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import additive_animations.helper.DpConverter;
import at.wirecube.additiveanimations.additive_animator.AdditiveAnimator;
import additive_animations.subclass.AdditiveAnimatorSubclassDemo;
import at.wirecube.additiveanimations.additive_animator.AnimationEndListener;
import at.wirecube.additiveanimations.additiveanimationsdemo.R;

public class RepeatingChainedAnimationsDemoFragment extends Fragment {
    ViewGroup rootView;
    View animatedView;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_tap_to_move_demo, container, false);
        animatedView = rootView.findViewById(R.id.animated_view);

        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    AdditiveAnimator.cancelAnimation(animatedView, View.ROTATION);
                }
                return true;
            }
        });

        int colors[] = new int[] {
                getResources().getColor(R.color.niceOrange),
                getResources().getColor(R.color.niceBlue),
                getResources().getColor(R.color.niceGreen),
                getResources().getColor(R.color.nicePink)
        };
        // TODO: don't use hardcoded px values
        AdditiveAnimatorSubclassDemo.animate(animatedView)
                .x(px(50)).y(px(100)).backgroundColor(colors[1]).rotation(0)
                .thenBounceBeforeEnd(800, 300)
                .thenBeforeEnd(400).x(px(250)).backgroundColor(colors[2]).rotationBy(45).setDuration(1000)
                .thenBounceBeforeEnd(800, 300)
                .thenBeforeEnd(400).y(px(500)).backgroundColor(colors[3]).rotationBy(45).setDuration(1000)
                .thenBounceBeforeEnd(800, 300)
                .thenBeforeEnd(400).x(px(50)).backgroundColor(colors[0]).rotationBy(90).setDuration(1000)
                .thenBounceBeforeEnd(800, 300)
                .setOverallRepeatCount(ValueAnimator.INFINITE)
                .start();

        return rootView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        AdditiveAnimatorSubclassDemo.cancelAnimations(animatedView);
    }

    private int px(int dp) {
        return DpConverter.converDpToPx(dp);
    }

}
