package additive_animations.fragments;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import at.wirecube.additiveanimations.additive_animator.AdditiveAnimator;
import at.wirecube.additiveanimations.additiveanimationsdemo.R;
import at.wirecube.additiveanimations.helper.FloatProperty;
import at.wirecube.additiveanimations.helper.evaluators.ColorEvaluator;

public class CustomAnimationsWithoutSubclassDemoFragment extends Fragment {
    ViewGroup rootView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = (ViewGroup) inflater.inflate(R.layout.fragment_text_view_color_demo, container, false);
        final TextView animatedView = (TextView) rootView.findViewById(R.id.animated_text_view);
        Button button = (Button) rootView.findViewById(R.id.change_color_button);

        button.setOnClickListener(new View.OnClickListener() {
            int currentColor = 0;
            final int colors[] = new int[] {
                    getResources().getColor(R.color.niceBlue),
                    getResources().getColor(R.color.niceGreen),
                    getResources().getColor(R.color.nicePink),
                    getResources().getColor(R.color.niceOrange)
            };
            @Override
            public void onClick(View v) {
                AdditiveAnimator.animate(animatedView).setInterpolator(new LinearOutSlowInInterpolator())
                        .property(colors[currentColor++ % 4], new ColorEvaluator(), new FloatProperty<View>("TextColorAnimationTag") {
                    @Override
                    public Float get(View object) {
                        return Float.valueOf(animatedView.getCurrentTextColor());
                    }

                    @Override
                    public void set(View object, Float value) {
                        animatedView.setTextColor(value.intValue());
                    }
                }).start();
            }
        });
        return rootView;
    }
}