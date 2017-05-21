package at.wirecube.additiveanimations.helper.propertywrappers;

import android.util.Property;
import android.view.View;
import android.view.ViewGroup;

import at.wirecube.additiveanimations.helper.FloatProperty;

public class MarginProperties {

    public static Property<View, Float> MARGIN_LEFT = new FloatProperty( "MARGIN_LEFT") {
        @Override
        public Float get(View object) {
            return Float.valueOf(((ViewGroup.MarginLayoutParams)object.getLayoutParams()).leftMargin);
        }

        @Override
        public void set(View object, Float value) {
            ((ViewGroup.MarginLayoutParams)object.getLayoutParams()).leftMargin = value.intValue();
        }
    };

    public static Property<View, Float> MARGIN_RIGHT = new FloatProperty("MARGIN_RIGHT") {
        @Override
        public Float get(View object) {
            return Float.valueOf(((ViewGroup.MarginLayoutParams)object.getLayoutParams()).rightMargin);
        }

        @Override
        public void set(View object, Float value) {
            ((ViewGroup.MarginLayoutParams)object.getLayoutParams()).rightMargin = value.intValue();
        }
    };

    public static Property<View, Float> MARGIN_TOP = new FloatProperty("MARGIN_TOP") {
        @Override
        public Float get(View object) {
            return Float.valueOf(((ViewGroup.MarginLayoutParams)object.getLayoutParams()).topMargin);
        }

        @Override
        public void set(View object, Float value) {
            ((ViewGroup.MarginLayoutParams)object.getLayoutParams()).topMargin = value.intValue();
        }
    };

    public static Property<View, Float> MARGIN_BOTTOM = new FloatProperty("MARGIN_BOTTOM") {
        @Override
        public Float get(View object) {
            return Float.valueOf(((ViewGroup.MarginLayoutParams)object.getLayoutParams()).bottomMargin);
        }

        @Override
        public void set(View object, Float value) {
            ((ViewGroup.MarginLayoutParams)object.getLayoutParams()).bottomMargin = value.intValue();
        }
    };
}
