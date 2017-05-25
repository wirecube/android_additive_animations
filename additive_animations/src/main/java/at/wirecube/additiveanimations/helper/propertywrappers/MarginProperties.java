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
