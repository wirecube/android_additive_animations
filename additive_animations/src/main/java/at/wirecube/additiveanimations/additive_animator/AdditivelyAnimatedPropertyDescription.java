package at.wirecube.additiveanimations.additive_animator;

import android.util.Property;
import android.view.View;

public class AdditivelyAnimatedPropertyDescription {
    private String mTag;
    private float mStartValue;
    private final float mTargetValue;
    private Property<View, Float> mProperty;

    public AdditivelyAnimatedPropertyDescription(Property<View, Float> property, float startValue, float targetValue) {
        mProperty = property;
        mTargetValue = targetValue;
        mStartValue = startValue;
    }

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
