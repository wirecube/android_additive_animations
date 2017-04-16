package at.wirecube.additiveanimations.additiveanimationsdemo.animation;

import android.support.v4.view.animation.PathInterpolatorCompat;
import android.view.animation.Interpolator;

public class EaseInOutPathInterpolator {
    public static Interpolator create() {
        return PathInterpolatorCompat.create(0.25f, 0.1f, 0.25f, 1.f);
    }
}
