package at.wirecube.additiveanimations.additive_animator.animation_set;

import android.animation.TypeEvaluator;
import android.util.Property;

import java.util.List;

public interface AnimationAction<T extends Object> {
    class Animation<T extends Object> {
        private final Property<T, Float> mProperty;
        private final float mTargetValue;
        private TypeEvaluator<Float> mTypeEvaluator = null;

        public Animation(Property<T, Float> property, float targetValue) {
            this.mProperty = property;
            this.mTargetValue = targetValue;
        }

        public Animation(Property<T, Float> property, float targetValue, TypeEvaluator<Float> evaluator) {
            this.mProperty = property;
            this.mTargetValue = targetValue;
            this.mTypeEvaluator = evaluator;
        }

        public Property<T, Float> getProperty() {
            return mProperty;
        }

        public float getTargetValue() {
            return mTargetValue;
        }

        public TypeEvaluator<Float> getTypeEvaluator() {
            return mTypeEvaluator;
        }
    }

    List<AnimationAction.Animation<T>> getAnimations();

}