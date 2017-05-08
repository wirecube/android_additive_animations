package at.wirecube.additiveanimations.additive_animator;

import android.graphics.drawable.ColorDrawable;
import android.view.View;

import java.util.Map;

import at.wirecube.additiveanimations.helper.evaluators.ArgbFloatEvaluator;

public class AdditiveAnimatorSubclassDemo extends AdditiveAnimator<AdditiveAnimatorSubclassDemo> {

    private static final String BACKGROUND_COLOR_TAG = "BACKGROUND_COLOR_TAG";


    public static AdditiveAnimatorSubclassDemo animate(View v) {
        return new AdditiveAnimatorSubclassDemo().addTarget(v);
    }

    public AdditiveAnimatorSubclassDemo() { super(); }

    @Override
    protected AdditiveAnimatorSubclassDemo newInstance() {
        return new AdditiveAnimatorSubclassDemo();
    }

    @Override
    protected void applyCustomProperties(Map<String, Float> tempProperties, View targetView) {
        if(tempProperties.get(BACKGROUND_COLOR_TAG) != null) {
            targetView.setBackgroundColor(tempProperties.get(BACKGROUND_COLOR_TAG).intValue());
        }
    }

    public AdditiveAnimatorSubclassDemo thenBounceBefore(int millis, long duration) {
        return thenBeforeEnd(millis).scale(1.2f).setDuration(duration)
               .thenBeforeEnd(100).scale(0.8f)
               .thenBeforeEnd(100).scale(1f);
    }

    public AdditiveAnimatorSubclassDemo backgroundColor(int color) {
        int startVal = ((ColorDrawable)currentTarget().getBackground()).getColor();
        AdditiveAnimation desc = new AdditiveAnimation(currentTarget(), BACKGROUND_COLOR_TAG, startVal, color);
        desc.setCustomTypeEvaluator(new ArgbFloatEvaluator());
        animateProperty(desc);
        return this;
    }
}
