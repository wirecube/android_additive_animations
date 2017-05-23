package at.wirecube.additiveanimations.additive_animator;

import android.animation.TypeEvaluator;
import android.graphics.Path;
import android.util.Property;
import android.view.View;

import at.wirecube.additiveanimations.helper.evaluators.PathEvaluator;

/**
 * This class is public for subclasses of AdditiveAnimator only, and should not be used outside of that.
 */
public class AdditiveAnimation {

    private String mTag;
    private float mStartValue;
    private float mTargetValue;
    private Property<View, Float> mProperty;
    private Path mPath;
    private PathEvaluator.PathMode mPathMode;
    private PathEvaluator mSharedPathEvaluator;
    private TypeEvaluator mCustomTypeEvaluator;
    private View mTargetView;
    private int mHashCode;

    /**
     * The preferred constructor to use when animating properties. If you use this constructor, you
     * don't need to worry about the logic to apply the changes. This is taken care of by using the
     * Setter provided by `property`.
     */
    public AdditiveAnimation(View targetView, Property<View, Float> property, float startValue, float targetValue) {
        mTargetView = targetView;
        mProperty = property;
        mTargetValue = targetValue;
        mStartValue = startValue;
        setTag(property.getName());
    }

    /**
     * Use this constructor for custom properties that have no simple getter or setter.
     * @param tag Name of the animated property. Must be unique.
     * @param startValue Start value of the animated property.
     * @param targetValue Target value of the animated property.
     */
    public AdditiveAnimation(View targetView, String tag, float startValue, float targetValue) {
        mTargetView = targetView;
        mStartValue = startValue;
        mTargetValue = targetValue;
        setTag(tag);
    }

    public AdditiveAnimation(View targetView, String tag, float startValue, Path path, PathEvaluator.PathMode pathMode, PathEvaluator sharedEvaluator) {
        mTargetView = targetView;
        mStartValue = startValue;
        mPath = path;
        mSharedPathEvaluator = sharedEvaluator;
        mPathMode = pathMode;
        mTargetValue = evaluateAt(1f);
        setTag(tag);
    }

    public AdditiveAnimation(View targetView, Property<View, Float> property, float startValue, Path path, PathEvaluator.PathMode pathMode, PathEvaluator sharedEvaluator) {
        mTargetView = targetView;
        mProperty = property;
        mStartValue = startValue;
        mPath = path;
        mSharedPathEvaluator = sharedEvaluator;
        mPathMode = pathMode;
        mTargetValue = evaluateAt(1f);

        setTag(property.getName());
    }

    private void setTag(String tag) {
        mTag = tag;
        // TODO: find a good hash code that doesn't collide often
        mHashCode = mTag.hashCode() * ((2 << 17) - 1) + mTargetView.hashCode();
    }

    public String getTag() {
        return mTag;
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

    public TypeEvaluator getCustomTypeEvaluator() {
        return mCustomTypeEvaluator;
    }

    public View getView() {
        return mTargetView;
    }

    public Property<View, Float> getProperty() { return mProperty; }

    public Path getPath() {
        return mPath;
    }

    public float evaluateAt(float progress) {
        if(mPath != null) {
            return mSharedPathEvaluator.evaluate(progress, mPathMode, mPath);
        } else {
            if(mCustomTypeEvaluator != null) {
                return (float) mCustomTypeEvaluator.evaluate(progress, mStartValue, mTargetValue);
            } else {
                return mStartValue + (mTargetValue - mStartValue) * progress;
            }
        }
    }

    @Override
    public int hashCode() {
        return mHashCode;
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof AdditiveAnimation)) {
            return false;
        }
        AdditiveAnimation other = (AdditiveAnimation) o;
        return other.mTag.hashCode() == mTag.hashCode() && other.mTargetView == mTargetView;
    }
}
