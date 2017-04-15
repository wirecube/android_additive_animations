package at.wirecube.additiveanimations.additiveanimationsdemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;


import at.wirecube.additiveanimations.additiveanimationsdemo.animation.additive_animations.base.AdditiveAnimator;

public class TapToMoveDemoFragment extends Fragment {

    FrameLayout rootView;
    View animatedView;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = (FrameLayout) inflater.inflate(R.layout.content_additive_animations_showcase, container);
        animatedView = rootView.findViewById(R.id.animated_view);

        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    AdditiveAnimator.animate(animatedView).x(event.getX()).y(event.getY()).setDuration(1000).start();
                    return true;
                }
                return true;
            }
        });
        return rootView;
    }
}
