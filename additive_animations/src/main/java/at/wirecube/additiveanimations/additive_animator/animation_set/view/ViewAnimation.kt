package at.wirecube.additiveanimations.additive_animator.animation_set.view;

import android.animation.TypeEvaluator;
import android.util.Property;
import android.view.View;

import at.wirecube.additiveanimations.additive_animator.animation_set.AnimationAction;

public class ViewAnimation extends AnimationAction.Animation<View> {
    public ViewAnimation(Property<View, Float> property, float targetValue) {
        super(property, targetValue);
    }

    public ViewAnimation(Property<View, Float> property, float targetValue, TypeEvaluator<Float> evaluator) {
        super(property, targetValue, evaluator);
    }
}
