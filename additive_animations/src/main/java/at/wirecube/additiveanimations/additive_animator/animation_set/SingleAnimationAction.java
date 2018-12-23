package at.wirecube.additiveanimations.additive_animator.animation_set;

import android.animation.TypeEvaluator;
import android.util.Property;

import java.util.ArrayList;
import java.util.List;

public class SingleAnimationAction<T> implements AnimationAction<T> {
    private List<AnimationAction.Animation<T>> mAnimations = new ArrayList<>();

    @Override
    public List<AnimationAction.Animation<T>> getAnimations() {
        return mAnimations;
    }

    public SingleAnimationAction(Property<T, Float> property, float target) {
        mAnimations.add(new AnimationAction.Animation<>(property, target));
    }

    public SingleAnimationAction(Property<T, Float> property, float target, TypeEvaluator evaluator) {
        mAnimations.add(new AnimationAction.Animation<>(property, target, evaluator));
    }

}
