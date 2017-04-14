package at.wirecube.additiveanimations.additiveanimationsdemo.animation;

import android.view.animation.PathInterpolator;

public class EaseInOutPathInterpolator extends PathInterpolator {
    public EaseInOutPathInterpolator() {
        super(0.25f, 0.1f, 0.25f, 1.0f);
    }
}
