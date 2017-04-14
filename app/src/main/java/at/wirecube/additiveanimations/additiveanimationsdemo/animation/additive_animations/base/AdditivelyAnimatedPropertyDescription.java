package at.wirecube.additiveanimations.additiveanimationsdemo.animation.additive_animations.base;

public class AdditivelyAnimatedPropertyDescription {
    private final String tag;

    private double startValue;
    private final double targetValue;

    public AdditivelyAnimatedPropertyDescription(String tag, double startValue, double targetValue) {
        this.tag = tag;
        this.startValue = startValue;
        this.targetValue = targetValue;
    }

    public String getTag() {
        return tag;
    }

    public double getStartValue() {
        return startValue;
    }

    public double getTargetValue() {
        return targetValue;
    }

    public void setStartValue(double startValue) {
        this.startValue = startValue;
    }
}
