/*
 *  Copyright 2017 David Ganster
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package at.wirecube.additiveanimations.helper.evaluators;

import android.graphics.Path;
import android.graphics.PathMeasure;


/**
 * A custom evaluator only to be used by {@link at.wirecube.additiveanimations.additive_animator.AdditiveAnimation}.
 * Use this class if you subclass {@link at.wirecube.additiveanimations.additive_animator.AdditiveAnimator} and want to
 * implement animating custom properties along paths.
 * It is NOT safe to share objects of this type among different animators since it holds state.
 */
public class PathEvaluator {

    public enum PathMode {
        X, Y, ROTATION;
        public static PathMode from(int mode) {
            switch (mode) {
                case 1: return Y;
                case 2: return ROTATION;
                default: return X;
            }
        }
    }

    private float lastEvaluatedFraction = -1;
    private float[] lastPoint = new float[2];
    private float lastAngle = 0;

    private float getResult(PathMode pathMode) {
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

    public float evaluate(float fraction, PathMode pathMode, Path path) {
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
