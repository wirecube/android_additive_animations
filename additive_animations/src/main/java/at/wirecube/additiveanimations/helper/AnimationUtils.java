package at.wirecube.additiveanimations.helper;

public class AnimationUtils {

    public static float clamp(float from, float to, float value) {
        return Math.max(from, Math.min(to, value));
    }

    public static float lerp(float from, float to, float progress) {
        return from + (to - from) * clamp(0, 1, progress);
    }
}

