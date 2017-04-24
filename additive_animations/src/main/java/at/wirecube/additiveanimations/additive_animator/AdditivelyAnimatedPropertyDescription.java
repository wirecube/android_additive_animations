package at.wirecube.additiveanimations.additive_animator;

import android.animation.TypeEvaluator;
import android.util.Property;
import android.view.View;

public class AdditivelyAnimatedPropertyDescription {
    private String mTag;
    private float mStartValue;
    private final float mTargetValue;
    private Property<View, Float> mProperty;
    private TypeEvaluator<Float> mCustomTypeEvaluator;

    /**
     * The preferred constructor to use when animating properties. If you use this constructor, you
     * don't need to worry about the logic to apply the changes. This is taken care of by using the
     * Setter provided by `property`.
     */
    public AdditivelyAnimatedPropertyDescription(Property<View, Float> property, float startValue, float targetValue) {
        mProperty = property;
        mTargetValue = targetValue;
        mStartValue = startValue;
    }

    /**
     * Use this constructor for custom properties that have no simple getter or setter.
     * @param tag Name of the animated property. Must be unique.
     * @param startValue Start value of the animated property.
     * @param targetValue Target value of the animated property.
     */
    public AdditivelyAnimatedPropertyDescription(String tag, float startValue, float targetValue) {
        this.mTag = tag;
        this.mStartValue = startValue;
        this.mTargetValue = targetValue;
    }

    public String getTag() {
        return mProperty != null ? mProperty.getName() : mTag;
    }

    public float getStartValue() {
        return mStartValue;
    }

    public float getTargetValue() {
        return mTargetValue;
    }

    public void setStartValue(float startValue) {
        this.mStartValue = startValue;
    }

    public void setCustomTypeEvaluator(TypeEvaluator<Float> evaluator) {
        mCustomTypeEvaluator = evaluator;
    }

    public TypeEvaluator<Float> getCustomTypeEvaluator() {
        return mCustomTypeEvaluator;
    }

    public Property<View, Float> getProperty() { return mProperty; }

    @Override
    public int hashCode() {
        if(mTag != null) {
            return mTag.hashCode();
        } else {
            return mProperty.getName().hashCode();
        }
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof AdditivelyAnimatedPropertyDescription)) {
            return false;
        }
        AdditivelyAnimatedPropertyDescription other = (AdditivelyAnimatedPropertyDescription) o;
        if(other.mTag != null && mTag != null) {
            return other.mTag.equals(mTag);
        } else if(other.mProperty != null && mProperty != null) {
            return other.mProperty.getName().equals(mProperty.getName());
        } else {
            return false;
        }
    }
}
