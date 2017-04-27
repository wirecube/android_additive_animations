package additive_animations.demo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import at.wirecube.additiveanimations.additive_animator.AdditiveAnimator;
import at.wirecube.additiveanimations.additive_animator.AdditiveAnimatorSubclassDemo;
import at.wirecube.additiveanimations.additiveanimationsdemo.R;

public class RepeatingChainedAnimationsDemoFragment extends Fragment {
    ViewGroup rootView;
    View animatedView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_tap_to_change_color_demo, container, false);
        animatedView = rootView.findViewById(R.id.animated_view);

        animate();
        return rootView;
    }

    private void animate() {
        int colors[] = new int[] {
                getResources().getColor(R.color.niceOrange),
                getResources().getColor(R.color.niceBlue),
                getResources().getColor(R.color.niceGreen),
                getResources().getColor(R.color.nicePink)
        };
        AdditiveAnimatorSubclassDemo.animate(animatedView).setDuration(1000)
                .x(100).y(200).backgroundColor(colors[1])
                .then().x(500).backgroundColor(colors[2])
                .then().y(1000).backgroundColor(colors[3])
                .then().x(100).backgroundColor(colors[0])
                .addEndAction(new AdditiveAnimator.AnimationEndListener() {
                    @Override
                    public void onAnimationEnd(boolean wasCancelled) {
                        animate();
                    }
                })
                .start();
    }

}
