package at.wirecube.additiveanimations.additiveanimationsdemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.Arrays;
import java.util.Map;

import at.wirecube.additiveanimations.additiveanimationsdemo.animation.additive_animations.base.AdditiveAnimator;
import at.wirecube.additiveanimations.additiveanimationsdemo.animation.additive_animations.base.AdditiveAnimatorUpdater;
import at.wirecube.additiveanimations.additiveanimationsdemo.animation.additive_animations.base.AdditivelyAnimatedPropertyDescription;

public class TapToMoveDemoFragment extends Fragment {

    class MyAnimationUpdater implements AdditiveAnimatorUpdater {
        public AdditivelyAnimatedPropertyDescription x(float target) {
            return new AdditivelyAnimatedPropertyDescription(View.X.getName(), animatedView.getX(), target);
        };

        public AdditivelyAnimatedPropertyDescription y(float target) {
            return new AdditivelyAnimatedPropertyDescription(View.Y.getName(), animatedView.getY(), target);
        };
        @Override
        public void applyChanges(Map<String, Double> tempProperties, View targetView) {
            double x = tempProperties.get(View.X.getName());
            View.X.set(targetView, (float) x);
            double y = tempProperties.get(View.Y.getName());
            View.Y.set(targetView, (float) y);
        }
    }

    FrameLayout rootView;
    View animatedView;

    MyAnimationUpdater updater = new MyAnimationUpdater();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = (FrameLayout) inflater.inflate(R.layout.content_additive_animations_showcase, container);
        animatedView = rootView.findViewById(R.id.animated_view);

        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    AdditiveAnimator.animate(animatedView).addAnimations(Arrays.asList(updater.x(event.getX()), updater.y(event.getY())), 1000, updater);
                    return true;
                }
                return true;
            }
        });
        return rootView;
    }
}
