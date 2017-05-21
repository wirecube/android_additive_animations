package at.wirecube.additiveanimations.helper.propertywrappers;

import android.graphics.drawable.ColorDrawable;
import android.util.Property;
import android.view.View;
import android.view.ViewGroup;

import at.wirecube.additiveanimations.helper.FloatProperty;

public class ColorProperties {
    public static Property<View, Float> BACKGROUND_COLOR = new FloatProperty("BACKGROUND_COLOR") {
        @Override
        public Float get(View object) {
            try {
                return Float.valueOf(((ColorDrawable)object.getBackground()).getColor());
            } catch (ClassCastException ex) {
                ex.printStackTrace();
            } catch (NullPointerException ex) {
                ex.printStackTrace();
            }
            return null;
        }

        @Override
        public void set(View object, Float value) {
            object.setBackgroundColor(value.intValue());
        }
    };

}
