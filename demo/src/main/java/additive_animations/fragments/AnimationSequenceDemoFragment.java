package additive_animations.fragments;

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
import java.util.stream.Collectors;

import additive_animations.fragments.sequence.AnimationSequenceJson;
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

//        AnimationSequence.playTogether(
//            AnimationSequence.playSequentially(
//                AdditiveAnimator.animate(animatedView).property(100, FloatProperty.create(View.X), true),
//                AdditiveAnimator.animate(animatedView).property(-100, FloatProperty.create(View.X), true)
//            ),
//            AnimationSequence.playSequentially(
//                AdditiveAnimator.animate(animatedView).property(100, FloatProperty.create(View.Y), true),
//                AdditiveAnimator.animate(animatedView).property(-100, FloatProperty.create(View.Y), true)
//            )
//        ).start();

        buildAnimationSequenceDemo();

        // equivalent to:
//        AdditiveAnimator.animate(animatedView)
//            .xBy(100).yBy(100)
//            .then()
//            .xBy(-100).yBy(-100)
//            .start();
    }

    private void buildAnimationSequenceDemo() {
        // using a json string for illustration purposes for how to dynamically build animations from a string representation.
        String demoJson = "{\"animationType\":\"Spawn\",\"children\":[{\"animationType\":\"Sequence\",\"children\":[{\"animationType\":\"AtOnce\",\"animations\":[{\"by\":true,\"propertyName\":\"x\",\"value\":100.0},{\"by\":true,\"propertyName\":\"y\",\"value\":0.0}]},{\"animationType\":\"AtOnce\",\"animations\":[{\"by\":true,\"propertyName\":\"x\",\"value\":-100.0},{\"by\":true,\"propertyName\":\"y\",\"value\":0.0}]}]},{\"animationType\":\"Sequence\",\"children\":[{\"animationType\":\"AtOnce\",\"animations\":[{\"by\":true,\"propertyName\":\"x\",\"value\":0.0},{\"by\":true,\"propertyName\":\"y\",\"value\":100.0}]},{\"animationType\":\"AtOnce\",\"animations\":[{\"by\":true,\"propertyName\":\"x\",\"value\":0.0},{\"by\":true,\"propertyName\":\"y\",\"value\":-100.0}]}]}]}";
        AnimationSequenceJson parsedSequence = new Gson().fromJson(demoJson, AnimationSequenceJson.class);

        // build animation recursively from json:
        AnimationSequence animationSequence = getAnimationSequence(animatedView, parsedSequence);

        animationSequence.start();
    }

    private AnimationSequence getAnimationSequence(View target, AnimationSequenceJson json) {
        List<AnimationSequence> children = new ArrayList<>();
        if (json.getChildren() != null) {
            for (AnimationSequenceJson child : json.getChildren()) {
                children.add(getAnimationSequence(target, child));
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
                    if (animation.getValue() != 0) {
                        animator.property(animation.getValue(), getProperty(animation.getPropertyName()), animation.isBy());
                    }
                }
                return animator;
        }
        return null;
    }

    private FloatProperty<View> getProperty(String propertyName) {
        // TODO: add more property names
        Map<String, FloatProperty<View>> propertyMap = new HashMap<>();
        propertyMap.put("x", FloatProperty.create(View.X));
        propertyMap.put("y", FloatProperty.create(View.Y));
        propertyMap.put("alpha", FloatProperty.create(View.ALPHA));

        return propertyMap.get(propertyName);
    }
}
