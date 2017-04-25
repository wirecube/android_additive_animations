package at.wirecube.additiveanimations.additive_animator;

import android.animation.TypeEvaluator;
import android.graphics.Path;
import android.graphics.PathMeasure;

import at.wirecube.additiveanimations.additive_animator.PropertyDescription;

/**
 * It is NOT safe to share objects of this type among different animators since it holds state.
 * It also butchers the evaluate method quite a bit, so don't use it anywhere else.
 */
class PathEvaluator implements TypeEvaluator {

    private float lastEvaluatedFraction = -1;
    private float[] lastPoint = new float[2];
    private float lastAngle = 0;

    private float getResult(PropertyDescription.PathMode pathMode) {
        switch (pathMode) {
            case X:
                return lastPoint[0];
            case Y:
                return lastPoint[1];
            case ROTATION:
                return lastAngle;
        }
        return 0;
    }

    @Override
    public Object evaluate(float fraction, Object pathMode, Object path) {
        if(fraction == lastEvaluatedFraction) {
            return getResult((PropertyDescription.PathMode)pathMode);
        }
        if(path instanceof Path) {
            float tan[] = new float[2];
            PathMeasure pathMeasure = new PathMeasure((Path) path, true);
            pathMeasure.getPosTan(pathMeasure.getLength() * fraction, lastPoint, tan);
            lastAngle = (float)(Math.atan2(tan[1], tan[0])*180.0/Math.PI);
            lastEvaluatedFraction = fraction;
            return getResult((PropertyDescription.PathMode)pathMode);
        } else {
            return 0f;
        }
    }
}
