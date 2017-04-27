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
        while(diff > 360) {
            diff -= 360;
        }
        while(diff < -360) {
            diff += 360;
        }
        if(diff > 180) {
            diff -= 360;
        }
        if(diff < -180) {
            diff += 360;
        }
        return diff;
    }

}

