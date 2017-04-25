package at.wirecube.additiveanimations.helper;

import android.animation.TypeEvaluator;
import android.graphics.Path;
import android.graphics.PathMeasure;

public class PathEvaluatorRotation implements TypeEvaluator {

    @Override
    public Object evaluate(float fraction, Object startValue, Object endValue) {
        if(endValue instanceof Path) {
            float[] tan = new float[2];
            PathMeasure pathMeasure = new PathMeasure((Path) endValue, true);
            pathMeasure.getPosTan(pathMeasure.getLength() * fraction, null, tan);
            float targetAngle = (float)(Math.atan2(tan[1], tan[0])*180.0/Math.PI);
            return targetAngle;
        } else {
            return startValue;
        }
    }
}
