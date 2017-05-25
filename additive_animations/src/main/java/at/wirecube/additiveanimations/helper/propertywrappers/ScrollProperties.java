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

import at.wirecube.additiveanimations.helper.FloatProperty;

// TODO: this works, but fires the onScrollChanged() event too many times (once for scrollX, once for scrollY).
public class ScrollProperties {

    public static Property<View, Float> SCROLL_X = new FloatProperty("SCROLL_X") {
        @Override
        public Float get(View object) {
            return Float.valueOf(object.getScrollX());
        }

        @Override
        public void set(View object, Float value) {
            object.setScrollX(value.intValue());
        }
    };

    public static Property<View, Float> SCROLL_Y = new FloatProperty("SCROLL_Y") {
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
