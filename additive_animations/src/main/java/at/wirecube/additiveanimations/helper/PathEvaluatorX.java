package at.wirecube.additiveanimations.helper;

import android.animation.TypeEvaluator;
import android.graphics.Path;
import android.graphics.PathMeasure;

public class PathEvaluatorX implements TypeEvaluator {

    @Override
    public Object evaluate(float fraction, Object startValue, Object endValue) {
        if(endValue instanceof Path) {
            float[] point = new float[2];
            PathMeasure pathMeasure = new PathMeasure((Path) endValue, true);
            pathMeasure.getPosTan(pathMeasure.getLength() * fraction, point, null);
            return point[0];
        } else {
            return startValue;
        }
    }
}
