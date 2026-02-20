package additive_animations.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.bartoszlipinski.viewpropertyobjectanimator.ViewPropertyObjectAnimator;

import additive_animations.AdditiveAnimationsShowcaseActivity;
import at.wirecube.additiveanimations.additive_animator.AdditiveAnimator;
import at.wirecube.additiveanimations.additiveanimationsdemo.R;
import at.wirecube.additiveanimations.helper.EaseInOutPathInterpolator;

public class MarginsDemoFragment extends Fragment {
    ViewGroup rootView;
    View animatedView;
    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_margins_demo, container, false);
        animatedView = rootView.findViewById(R.id.animated_view_with_margins);

        rootView.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_DOWN) {
                if(AdditiveAnimationsShowcaseActivity.ADDITIVE_ANIMATIONS_ENABLED) {
                    AdditiveAnimator.animate(animatedView).leftMargin((int) event.getX()).topMargin((int) event.getY()).start();
                } else {
                    ViewPropertyObjectAnimator.animate(animatedView).leftMargin((int) event.getX()).topMargin((int) event.getY()).setInterpolator(EaseInOutPathInterpolator.create()).setDuration(1000).start();
                }
            }
            return true;
        });
        return rootView;
    }
}