package additive_animations.fragments.custom_drawing;

import java.util.Map;

import at.wirecube.additiveanimations.additive_animator.AdditiveAnimation;
import at.wirecube.additiveanimations.additive_animator.BaseAdditiveAnimator;

// Example of an animator subclass that works with a class which doesn't derive from `View`
public class AdditiveRectAnimator extends BaseAdditiveAnimator<AdditiveRectAnimator, Rect> {

    private static final String X = "RectX";
    private static final String Y = "RectY";
    private static final String SIZE = "RectSize";
    private static final String CORNER_RADIUS = "RectCornerRadius";
    private static final String ROTATION = "RectRotation";

    @Override
    protected AdditiveRectAnimator newInstance() {
        return new AdditiveRectAnimator();
    }

    public static AdditiveRectAnimator animate(Rect rect) {
        return new AdditiveRectAnimator().target(rect);
    }

    public AdditiveRectAnimator x(float x) {
        // AdditiveAnimation objects can be instantiated with a getter/setter or just by providing a key which will later be retrieved in applyCustomProperties()
        return animate(new AdditiveAnimation<>(mCurrentTarget, X, mCurrentTarget.mX, x));
    }

    public AdditiveRectAnimator y(float y) {
        return animate(new AdditiveAnimation<>(mCurrentTarget, Y, mCurrentTarget.mY, y));
    }

    public AdditiveRectAnimator size(float size) {
        return animate(new AdditiveAnimation<>(mCurrentTarget, SIZE, mCurrentTarget.mSize, size));
    }

    public AdditiveRectAnimator cornerRadius(float cornerRadius) {
        return animate(new AdditiveAnimation<>(mCurrentTarget, CORNER_RADIUS, mCurrentTarget.mCornerRadius, cornerRadius));
    }

    public AdditiveRectAnimator rotation(float rotation) {
        return animate(new AdditiveAnimation<>(mCurrentTarget, ROTATION, mCurrentTarget.mRotation, rotation));
    }

    // This method is called when we try to animate keys without a getter/setter, as we do in this example
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
                case SIZE:
                    target.mSize = entry.getValue();
                    break;
                case ROTATION:
                    target.mRotation = entry.getValue();
                    break;
                case CORNER_RADIUS:
                    target.mCornerRadius = entry.getValue();
                    break;
            }
        }

        // force redraw of the parent view:
        target.getView().invalidate();
    }
}
