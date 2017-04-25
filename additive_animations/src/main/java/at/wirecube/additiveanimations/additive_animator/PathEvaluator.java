package at.wirecube.additiveanimations.additive_animator;

import android.graphics.Path;
import android.graphics.PathMeasure;


/**
 * It is NOT safe to share objects of this type among different animators since it holds state.
 * It doesn't conform to TypeEvaluator because its usage is too obtuse to be generally useful.
 */
class PathEvaluator {

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

    float evaluate(float fraction, PropertyDescription.PathMode pathMode, Path path) {
        if(fraction == lastEvaluatedFraction) {
            return getResult(pathMode);
        }
        float tan[] = new float[2];
        PathMeasure pathMeasure = new PathMeasure(path, true);
        pathMeasure.getPosTan(pathMeasure.getLength() * fraction, lastPoint, tan);
        lastAngle = (float)(Math.atan2(tan[1], tan[0])*180.0/Math.PI);
        lastEvaluatedFraction = fraction;
        return getResult(pathMode);
    }
}
