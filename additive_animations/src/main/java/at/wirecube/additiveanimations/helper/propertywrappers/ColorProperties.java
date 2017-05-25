/*
 *  Copyright 2017 David Ganster
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

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
