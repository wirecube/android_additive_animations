package additive_animations.fragments.sequence;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import at.wirecube.additiveanimations.additive_animator.AdditiveAnimator;
import at.wirecube.additiveanimations.additive_animator.sequence.AnimationSequence;
import at.wirecube.additiveanimations.additiveanimationsdemo.R;
import at.wirecube.additiveanimations.helper.FloatProperty;

public class AnimationSequenceDemoFragment extends Fragment {
    private FrameLayout rootView;
    private View animatedView;
    private View mTouchView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = (FrameLayout) inflater.inflate(R.layout.fragment_tap_to_move_demo, container, false);
        mTouchView = rootView.findViewById(R.id.touch_view);
        animatedView = rootView.findViewById(R.id.animated_view);

        rootView.setOnClickListener(v -> doSequenceAnimation());

        return rootView;
    }

    private void doSequenceAnimation() {
        buildAnimationSequenceDemo();
    }

    private void buildAnimationSequenceDemo() {
        // using a json string for illustration purposes for how to dynamically build animations from a string representation.
        String demoJson = "{\"animationType\":\"Spawn\",\"children\":[{\"animationType\":\"Sequence\",\"children\":[{\"animationType\":\"AtOnce\",\"animations\":[{\"by\":true,\"propertyName\":\"x\",\"value\":100.0},{\"by\":true,\"propertyName\":\"y\",\"value\":0.0}]},{\"animationType\":\"AtOnce\",\"animations\":[{\"by\":true,\"propertyName\":\"x\",\"value\":-100.0},{\"by\":true,\"propertyName\":\"y\",\"value\":0.0}]}]},{\"animationType\":\"Sequence\",\"children\":[{\"animationType\":\"AtOnce\",\"animations\":[{\"by\":true,\"propertyName\":\"x\",\"value\":0.0},{\"by\":true,\"propertyName\":\"y\",\"value\":100.0}]},{\"animationType\":\"AtOnce\",\"animations\":[{\"by\":true,\"propertyName\":\"x\",\"value\":0.0},{\"by\":true,\"propertyName\":\"y\",\"value\":-100.0}]}]}]}";
        AnimationSequenceJson animationDescription = new Gson().fromJson(demoJson, AnimationSequenceJson.class);

        // the sequence encoded in the json is equivalent to this code:
//        AnimationSequence.playTogether(
//            AnimationSequence.playSequentially(
//                AdditiveAnimator.animate(animatedView).xBy(100),
//                AdditiveAnimator.animate(animatedView).xBy(-100)
//            ),
//            AnimationSequence.playSequentially(
//                AdditiveAnimator.animate(animatedView).yBy(100),
//                AdditiveAnimator.animate(animatedView).yBy(-100)
//            )
//        ).start();

        // build animation tree recursively from json:
        AnimationSequence animationSequence = createAnimationSequence(animatedView, animationDescription);
        animationSequence.start();
    }

    private AnimationSequence createAnimationSequence(View target, AnimationSequenceJson json) {
        List<AnimationSequence> children = new ArrayList<>();
        if (json.getChildren() != null) {
            for (AnimationSequenceJson child : json.getChildren()) {
                children.add(createAnimationSequence(target, child));
            }
        }
        switch (json.getAnimationType()) {
            case Spawn:
                return AnimationSequence.playTogether(children);
            case Sequence:
                return AnimationSequence.playSequentially(children);
            case AtOnce:
                AdditiveAnimator animator = AdditiveAnimator.animate(target);
                for (AnimationSequenceJson.AnimationInstructionJson animation : json.getAnimations()) {
                    animator.property(animation.getValue(), getProperty(animation.getPropertyName()), animation.isBy());
                }
                return animator;
            default:
                throw new IllegalArgumentException("Don't know how to handle '" + json.getAnimationType() + "'.");
        }
    }

    private FloatProperty<View> getProperty(String propertyName) {
        // TODO: add more property names here for all the animations you might need to parse
        Map<String, FloatProperty<View>> propertyMap = new HashMap<>();
        propertyMap.put("x", FloatProperty.create(View.X));
        propertyMap.put("y", FloatProperty.create(View.Y));
        propertyMap.put("alpha", FloatProperty.create(View.ALPHA));

        return propertyMap.get(propertyName);
    }
}
