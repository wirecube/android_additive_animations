package additive_animations.fragments;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.bartoszlipinski.viewpropertyobjectanimator.ViewPropertyObjectAnimator;

import additive_animations.AdditiveAnimationsShowcaseActivity;
import additive_animations.helper.DpConverter;
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
                    int offset =  DpConverter.converDpToPx(150);
                    if(AdditiveAnimationsShowcaseActivity.ADDITIVE_ANIMATIONS_ENABLED) {

                        AdditiveAnimator.animate(animatedView)
                                .centerX(event.getX()).centerY(event.getY())
                                .then() // execute the following animations after the previous ones have finished
                                .centerX(event.getX() - offset).centerY(event.getY() - offset)
                                .start();

                    } else {

                        PropertyValuesHolder pvhX = PropertyValuesHolder.ofFloat(View.TRANSLATION_X, event.getX() - animatedView.getWidth() / 2);
                        PropertyValuesHolder pvhY = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, event.getY() - animatedView.getHeight() / 2);
                        ObjectAnimator animator1  = ObjectAnimator.ofPropertyValuesHolder(animatedView, pvhX, pvhY);

                        PropertyValuesHolder pvhX2 = PropertyValuesHolder.ofFloat(View.TRANSLATION_X, (event.getX() - animatedView.getWidth() / 2) - offset);
                        PropertyValuesHolder pvhY2 = PropertyValuesHolder.ofFloat(View.TRANSLATION_Y, (event.getY() - animatedView.getHeight() / 2) - offset);
                        ObjectAnimator animator2   = ObjectAnimator.ofPropertyValuesHolder(animatedView, pvhX2, pvhY2);

                        AnimatorSet animators = new AnimatorSet();
                        animators.playSequentially(animator1, animator2);
                        animators.setDuration(2000);
                        animators.setInterpolator(EaseInOutPathInterpolator.create());
                        animators.start();
                    }
                }
                return true;
            }
        });
        return rootView;
    }
}