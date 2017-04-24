package at.wirecube.additiveanimations.helper.propertywrappers;

import android.util.Property;
import android.view.View;

// TODO: this works, but fires the onScrollChanged() event too many times (once for scrollX, once for scrollY).
public class ScrollProperties {

    public static Property<View, Float> SCROLL_X = new Property<View, Float>(Float.class, "SCROLL_X") {
        @Override
        public Float get(View object) {
            return Float.valueOf(object.getScrollX());
        }

        @Override
        public void set(View object, Float value) {
            object.setScrollX(value.intValue());
        }
    };

    public static Property<View, Float> SCROLL_Y = new Property<View, Float>(Float.class, "SCROLL_Y") {
        @Override
        public Float get(View object) {
            return Float.valueOf(object.getScrollY());
        }

        @Override
        public void set(View object, Float value) {
            object.setScrollY(value.intValue());
        }
    };

}
