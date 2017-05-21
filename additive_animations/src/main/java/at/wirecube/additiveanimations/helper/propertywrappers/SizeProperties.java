package at.wirecube.additiveanimations.helper.propertywrappers;

import android.util.Property;
import android.view.View;

import at.wirecube.additiveanimations.helper.FloatProperty;

public class SizeProperties {
    public static Property<View, Float> WIDTH = new FloatProperty("VIEW_WIDTH") {
        @Override
        public Float get(View object) {
            return Float.valueOf((object.getLayoutParams()).width);
        }

        @Override
        public void set(View object, Float value) {
            object.getLayoutParams().width = value.intValue();
        }
    };

    public static Property<View, Float> HEIGHT = new FloatProperty("VIEW_HEIGHT") {
        @Override
        public Float get(View object) {
            return Float.valueOf((object.getLayoutParams()).height);
        }

        @Override
        public void set(View object, Float value) {
            object.getLayoutParams().height = value.intValue();
        }
    };

}
