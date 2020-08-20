package additive_animations.fragments.custom_drawing;

import at.wirecube.additiveanimations.additive_animator.BaseAdditiveAnimator;
import at.wirecube.additiveanimations.helper.FloatProperty;

/**
 * Example of an animator subclass that works with a class which doesn't derive from `View`.
 * This example shows off both property- and tag-based animations.
 * Tag-based animations use only a String tag to identify them, but their value must be manually manipulated by the AdditiveAnimator subclass.
 * Property-based animations are easier to use, but they require a getter and setter for each property.
 * In principle, property-based animations should be favored because they make the rest of the implementation simpler and
 * more reusable - but it comes at the cost of more verbosity when declaring the properties.
 */
public class AdditiveRectAnimator extends BaseAdditiveAnimator<AdditiveRectAnimator, Rect> {

    @Override
    protected AdditiveRectAnimator newInstance() {
        return new AdditiveRectAnimator();
    }

    public static AdditiveRectAnimator animate(Rect rect) {
        return new AdditiveRectAnimator().target(rect);
    }

    public AdditiveRectAnimator size(float size) {
        // AdditiveAnimation objects can (preferably) be created using a FloatProperty object.
        // In this case, you don't need to do anything else to make your property animatable!
        return property(size, FloatProperty.create("RectSize", rect -> rect.mSize, (rect, s) -> rect.mSize = s));
    }

    public AdditiveRectAnimator cornerRadius(float cornerRadius) {
        return property(cornerRadius, FloatProperty.create("RectCornerRadius", rect -> rect.mCornerRadius, (rect, cr) -> rect.mCornerRadius = cr));
    }

    public AdditiveRectAnimator rotation(float rotation) {
        return property(rotation, FloatProperty.create("RectRotation", rect -> rect.mRotation, (rect, r) -> rect.mRotation = r));
    }

    public AdditiveRectAnimator x(float x) {
        return property(x, FloatProperty.create("RectX", rect -> rect.mX, (rect, xVal) -> rect.mX = xVal));
    }

    public AdditiveRectAnimator y(float y) {
        return property(y, FloatProperty.create("RectY", rect -> rect.mY, (rect, yVal) -> rect.mY = yVal));
    }

    // This method is called after the current frame has been calculated.
    // You have to make sure to invalidate the your view/custom object here!
    @Override
    public void onApplyChanges() {
        // force redraw of the parent view:
        if (getCurrentTarget().getView() != null) {
            getCurrentTarget().getView().invalidate();
        }
    }

    @Override
    public Float getCurrentPropertyValue(String propertyName) {
        // only necessary when implementing non-property-based (tag-based) animations, which is highly discouraged.
        return null;
    }
}
