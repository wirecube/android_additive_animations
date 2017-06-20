package at.wirecube.additiveanimations.helper.propertywrappers;

import android.util.Property;
import android.view.View;
import android.view.ViewGroup;

import at.wirecube.additiveanimations.helper.FloatProperty;

public class ElevationProperties {

    @SuppressWarnings("NewApi")
    public static Property<View, Float> ELEVATION = new FloatProperty( "ELEVATION") {
        @Override
        public Float get(View object) {
            return Float.valueOf(object.getElevation());
        }

        @Override
        public void set(View object, Float value) {
            object.setElevation(value);
        }
    };

}
