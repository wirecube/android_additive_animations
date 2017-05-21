package at.wirecube.additiveanimations.additive_animator;

import android.animation.TypeEvaluator;
import android.graphics.Path;
import android.util.Property;
import android.view.View;

import java.lang.ref.WeakReference;

import at.wirecube.additiveanimations.helper.evaluators.PathEvaluator;

/**
 * This class is public for subclasses of AdditiveAnimator only, and should not be used outside of that.
 *
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
    private WeakReference<View> mTargetView;

    /**
     * The preferred constructor to use when animating properties. If you use this constructor, you
     * don't need to worry about the logic to apply the changes. This is taken care of by using the
     * Setter provided by `property`.
     */
    public AdditiveAnimation(View targetView, Property<View, Float> property, float startValue, float targetValue) {
        mTargetView = new WeakReference(targetView);
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
    public AdditiveAnimation(View targetView, String tag, float startValue, float targetValue) {
        mTargetView = new WeakReference(targetView);
        mTag = tag;
        mStartValue = startValue;
        mTargetValue = targetValue;
    }

    public AdditiveAnimation(View targetView, String tag, float startValue, Path path, PathEvaluator.PathMode pathMode, PathEvaluator sharedEvaluator) {
        mTargetView = new WeakReference(targetView);
        mTag = tag;
        mStartValue = startValue;
        mPath = path;
        mSharedPathEvaluator = sharedEvaluator;
        mPathMode = pathMode;
        mTargetValue = evaluateAt(1f);
    }

    public AdditiveAnimation(View targetView, Property<View, Float> property, float startValue, Path path, PathEvaluator.PathMode pathMode, PathEvaluator sharedEvaluator) {
        mTargetView = new WeakReference(targetView);
        mProperty = property;
        mStartValue = startValue;
        mPath = path;
        mSharedPathEvaluator = sharedEvaluator;
        mPathMode = pathMode;
        mTargetValue = evaluateAt(1f);
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

    public TypeEvaluator getCustomTypeEvaluator() {
        return mCustomTypeEvaluator;
    }

    public View getView() {
        return mTargetView.get();
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
        if(mTag != null) {
            return mTag.hashCode();
        } else {
            return mProperty.getName().hashCode();
        }
    }

    @Override
    public boolean equals(Object o) {
        if(!(o instanceof AdditiveAnimation)) {
            return false;
        }
        AdditiveAnimation other = (AdditiveAnimation) o;
        if(other.mTag != null && mTag != null) {
            return other.mTag.equals(mTag) && other.getView() == getView();
        } else if(other.mProperty != null && mProperty != null) {
            return other.mProperty.getName().equals(mProperty.getName()) && other.getView() == getView();
        } else {
            return false;
        }
    }
}
