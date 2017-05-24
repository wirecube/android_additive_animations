package additive_animations.fragments;

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
                    if(AdditiveAnimationsShowcaseActivity.ADDITIVE_ANIMATIONS_ENABLED) {
                        AdditiveAnimator.animate(animatedView).setDuration(1000)
                                .centerX(event.getX()).centerY(event.getY()) //.scale(1.2f).backgroundColor(getResources().getColor(R.color.niceBlue))
                                .then().centerX(event.getX() - DpConverter.converDpToPx(150)).centerY(event.getY() - DpConverter.converDpToPx(150) )//.scale(1.0f).backgroundColor(getResources().getColor(R.color.niceOrange))
                                .start();
                    } else {
                        ViewPropertyObjectAnimator.animate(animatedView).setInterpolator(EaseInOutPathInterpolator.create()).setDuration(1000)
                                .x(event.getX() - animatedView.getWidth() / 2).y(event.getY() - animatedView.getHeight() / 2)
                                .start();
                        ViewPropertyObjectAnimator.animate(animatedView).setInterpolator(EaseInOutPathInterpolator.create()).setDuration(1000)
                                .setStartDelay(1100)
                                .x(animatedView.getX()).y(animatedView.getY())
                                .start();
                    }
                }
                return true;
            }
        });
        return rootView;
    }
}