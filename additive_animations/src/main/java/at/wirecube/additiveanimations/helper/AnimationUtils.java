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

package at.wirecube.additiveanimations.helper;

public class AnimationUtils {

    public static float clamp(float from, float to, float value) {
        return Math.max(from, Math.min(to, value));
    }

    public static float lerp(float from, float to, float progress) {
        return from + (to - from) * clamp(0, 1, progress);
    }

    public static float shortestAngleBetween(float start, float dest) {
        float diff = dest - start;
        if (Math.abs(diff) > 180) {
            if (diff > 0) {
                diff = diff - 360;
            } else {
                diff = 360 + diff;
            }
        }
        while (diff > 360) {
            diff -= 360;
        }
        while (diff < -360) {
            diff += 360;
        }
        if (diff > 180) {
            diff -= 360;
        }
        if (diff < -180) {
            diff += 360;
        }
        return diff;
    }

}

