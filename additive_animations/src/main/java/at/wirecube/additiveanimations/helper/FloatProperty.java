package at.wirecube.additiveanimations.helper;

import android.util.Property;
import android.view.View;

public abstract class FloatProperty extends Property<View, Float> {
    public FloatProperty(String name) {
        super(Float.class, name);
    }
}
