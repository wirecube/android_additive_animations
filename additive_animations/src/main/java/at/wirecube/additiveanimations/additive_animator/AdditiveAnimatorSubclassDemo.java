package at.wirecube.additiveanimations.additive_animator;

import android.view.View;

public class AdditiveAnimatorSubclassDemo extends AdditiveAnimator<AdditiveAnimatorSubclassDemo> {

    public AdditiveAnimatorSubclassDemo(View v) {
        super(v);
    }

    public static AdditiveAnimatorSubclassDemo animate(View v) {
        return new AdditiveAnimatorSubclassDemo(v);
    }

    public AdditiveAnimatorSubclassDemo xy(float x, float y) {
        x(x);
        y(y);
        return this;
    }
}
