package at.wirecube.additiveanimations.additiveanimationsdemo.animation.additive_animations.base;

import android.view.View;

import java.util.Map;

public interface AdditiveAnimatorUpdater {
    void applyChanges(Map<String, Double> tempProperties, View targetView);
}
