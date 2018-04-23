package additive_animations.fragments.custom_drawing;

import java.util.Map;

import at.wirecube.additiveanimations.additive_animator.AdditiveAnimation;
import at.wirecube.additiveanimations.additive_animator.BaseAdditiveAnimator;
import at.wirecube.additiveanimations.helper.FloatProperty;

/**
 *
 * Example of an animator subclass that works with a class which doesn't derive from `View`.
 * This example shows off both property- and tag-based animations.
 * Tag-based animations use only a String tag to identify them, but their value must be manually manipulated by the AdditiveAnimator subclass.
 * Property-based animations are easier to use, but they require a getter and setter for each property.
 * In principle, property-based animations should be favored because they make the rest of the implementation simpler and
 * more reusable - but it comes at the cost of more verbosity when declaring the properties.
 */
public class AdditiveRectAnimator extends BaseAdditiveAnimator<AdditiveRectAnimator, Rect> {

    // Tag-based animations need an identifier:
    private static final String X = "RectX";
    private static final String Y = "RectY";

    // Property-based animations need a FloatProperty declaration with a getter and setter.
    // It is recommended to move those declarations to the classes that they are referencing -
    // they are only part of this class for demo-purposes.
    private static final FloatProperty<Rect> SIZE_PROP = new FloatProperty<Rect>("RectSize") {
        @Override public void set(Rect rect, Float value) { rect.mSize = value; }
        @Override public Float get(Rect o) { return o.mSize; }
    };

    private static final FloatProperty<Rect> CORNER_RADIUS_PROP = new FloatProperty<Rect>("RectCornerRadius") {
        @Override public void set(Rect rect, Float value) { rect.mCornerRadius = value; }
        @Override public Float get(Rect o) { return o.mCornerRadius; }
    };

    private static final FloatProperty<Rect> ROTATION_PROP = new FloatProperty<Rect>("RectRotation") {
        @Override public void set(Rect rect, Float value) { rect.mRotation = value; }
        @Override public Float get(Rect o) { return o.mRotation; }
    };

    @Override
    protected AdditiveRectAnimator newInstance() {
        return new AdditiveRectAnimator();
    }

    public static AdditiveRectAnimator animate(Rect rect) {
        return new AdditiveRectAnimator().target(rect);
    }

    public AdditiveRectAnimator size(float size) {
        // AdditiveAnimation objects can also (and preferably) be created using a FloatProperty object.
        // In this case, you don't need to do anything else to make your property animatable!
        return animate(new AdditiveAnimation<>(getCurrentTarget(), SIZE_PROP, mCurrentTarget.mSize, size));
    }

    public AdditiveRectAnimator cornerRadius(float cornerRadius) {
        return animate(new AdditiveAnimation<>(getCurrentTarget(), CORNER_RADIUS_PROP, mCurrentTarget.mCornerRadius, cornerRadius));
    }

    public AdditiveRectAnimator rotation(float rotation) {
        return animate(new AdditiveAnimation<>(getCurrentTarget(), ROTATION_PROP, mCurrentTarget.mRotation, rotation));
    }

    public AdditiveRectAnimator x(float x) {
        // AdditiveAnimation objects can also be instantiated by providing a key which will later be retrieved in applyCustomProperties():
        return animate(new AdditiveAnimation<>(getCurrentTarget(), X, mCurrentTarget.mX, x));
    }

    public AdditiveRectAnimator y(float y) {
        return animate(new AdditiveAnimation<>(getCurrentTarget(), Y, mCurrentTarget.mY, y));
    }

    // This method is called when we try to animate keys without a getter/setter, as we do in this example for X and Y.
    // In this demo class, the two approaches are mixed, with only X and Y being animated without a property:
    @Override
    protected void applyCustomProperties(Map<String, Float> tempProperties, Rect target) {
        for(Map.Entry<String, Float> entry : tempProperties.entrySet()) {
            switch (entry.getKey()) {
                case X:
                    target.mX = entry.getValue();
                    break;
                case Y:
                    target.mY = entry.getValue();
                    break;
            }
        }
    }

    // Implementing this method is only necessary if we don't provide Property<> animations, but use tags instead.
    // In this demo class, the two approaches are mixed, with only X and Y being animated without a property:
    @Override
    public Float getCurrentPropertyValue(String propertyName) {
        if(X.equals(propertyName)) {
            return getCurrentTarget().mX;
        } else if (Y.equals(propertyName)) {
            return getCurrentTarget().mY;
        }
        return null;
    }

    // This method is called after the current frame has been calculated.
    // You have to make sure to invalidate the your view/custom object here!
    @Override
    public void onApplyChanges() {
        // force redraw of the parent view:
        getCurrentTarget().getView().invalidate();
    }
}
