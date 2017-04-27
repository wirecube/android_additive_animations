package at.wirecube.additiveanimations.additive_animator;

import android.graphics.drawable.ColorDrawable;
import android.view.View;

import java.util.Map;

import at.wirecube.additiveanimations.helper.evaluators.ArgbFloatEvaluator;

public class AdditiveAnimatorSubclassDemo extends AdditiveAnimator<AdditiveAnimatorSubclassDemo> {

    private static final String BACKGROUND_COLOR_TAG = "BACKGROUND_COLOR_TAG";

    public AdditiveAnimatorSubclassDemo(View v) {
        super(v);
    }

    public static AdditiveAnimatorSubclassDemo animate(View v) {
        return new AdditiveAnimatorSubclassDemo(v);
    }

    @Override
    protected void applyCustomProperties(Map<String, Float> tempProperties, View targetView) {
        if(tempProperties.get(BACKGROUND_COLOR_TAG) != null) {
            targetView.setBackgroundColor(tempProperties.get(BACKGROUND_COLOR_TAG).intValue());
        }
    }

    public AdditiveAnimatorSubclassDemo backgroundColor(int color) {
        int startVal = ((ColorDrawable)currentTarget().getBackground()).getColor();
        PropertyDescription desc = new PropertyDescription(currentTarget(), BACKGROUND_COLOR_TAG, startVal, color);
        desc.setCustomTypeEvaluator(new ArgbFloatEvaluator());
        animateProperty(desc);
        return this;
    }
}
