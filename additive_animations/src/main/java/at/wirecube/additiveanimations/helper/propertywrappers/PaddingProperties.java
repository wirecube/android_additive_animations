package at.wirecube.additiveanimations.helper.propertywrappers;

import android.util.Property;
import android.view.View;
import android.view.ViewGroup;

import at.wirecube.additiveanimations.helper.FloatProperty;

public class PaddingProperties {

    public static Property<View, Float> PADDING_LEFT = new FloatProperty("PADDING_LEFT") {
        @Override
        public Float get(View object) {
            return Float.valueOf(object.getPaddingLeft());
        }

        @Override
        public void set(View object, Float value) {
            object.setPadding(value.intValue(), object.getPaddingTop(), object.getPaddingRight(), object.getPaddingBottom());
        }
    };

    public static Property<View, Float> PADDING_RIGHT = new FloatProperty("PADDING_RIGHT") {
        @Override
        public Float get(View object) {
            return Float.valueOf(object.getPaddingRight());
        }

        @Override
        public void set(View object, Float value) {
            object.setPadding(object.getPaddingLeft(), object.getPaddingTop(), value.intValue(), object.getPaddingBottom());
        }
    };

    public static Property<View, Float> PADDING_TOP = new FloatProperty("PADDING_TOP") {
        @Override
        public Float get(View object) {
            return Float.valueOf(object.getPaddingTop());
        }

        @Override
        public void set(View object, Float value) {
            object.setPadding(object.getPaddingLeft(), value.intValue(), object.getPaddingRight(), object.getPaddingBottom());
        }
    };

    public static Property<View, Float> PADDING_BOTTOM = new FloatProperty("PADDING_BOTTOM") {
        @Override
        public Float get(View object) {
            return Float.valueOf(object.getPaddingBottom());
        }

        @Override
        public void set(View object, Float value) {
            object.setPadding(object.getPaddingLeft(), object.getPaddingTop(), object.getPaddingRight(), value.intValue());
        }
    };
}
