package at.wirecube.additiveanimations.additive_animator;

import android.graphics.drawable.ColorDrawable;
import android.view.View;

import java.util.Map;

import at.wirecube.additiveanimations.helper.ArgbFloatEvaluator;

public class AdditiveAnimatorSubclassDemo extends AdditiveAnimator<AdditiveAnimatorSubclassDemo> {

    public AdditiveAnimatorSubclassDemo(View v) {
        super(v);
    }

    public static AdditiveAnimatorSubclassDemo animate(View v) {
        return new AdditiveAnimatorSubclassDemo(v);
    }

    @Override
    protected void applyCustomProperties(Map<String, Float> tempProperties, View targetView) {
        if(tempProperties.get("background_color") != null) {
            targetView.setBackgroundColor(tempProperties.get("background_color").intValue());
        }
    }

    public AdditiveAnimatorSubclassDemo backgroundColor(int color) {
        int startVal = ((ColorDrawable)currentTarget().getBackground()).getColor();
        AdditivelyAnimatedPropertyDescription desc = new AdditivelyAnimatedPropertyDescription("background_color", startVal, color);
        desc.setCustomTypeEvaluator(new ArgbFloatEvaluator());
        animateProperty(desc);
        return this;
    }
}
