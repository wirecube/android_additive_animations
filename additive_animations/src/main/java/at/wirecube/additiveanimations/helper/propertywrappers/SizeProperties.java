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

public class SizeProperties {
    public static Property<View, Float> WIDTH = new FloatProperty<View>("VIEW_WIDTH") {
        @Override
        public Float get(View object) {
            return Float.valueOf((object.getLayoutParams()).width);
        }

        @Override
        public void set(View object, Float value) {
            object.getLayoutParams().width = value.intValue();
        }
    };

    public static Property<View, Float> HEIGHT = new FloatProperty<View>("VIEW_HEIGHT") {
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
