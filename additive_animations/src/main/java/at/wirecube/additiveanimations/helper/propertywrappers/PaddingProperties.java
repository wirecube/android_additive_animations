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

public class PaddingProperties {

    public static Property<View, Float> PADDING_LEFT = new FloatProperty<View>("PADDING_LEFT") {
        @Override
        public Float get(View object) {
            return (float) object.getPaddingLeft();
        }

        @Override
        public void set(View object, Float value) {
            object.setPadding(value.intValue(), object.getPaddingTop(), object.getPaddingRight(), object.getPaddingBottom());
        }
    };

    public static Property<View, Float> PADDING_RIGHT = new FloatProperty<View>("PADDING_RIGHT") {
        @Override
        public Float get(View object) {
            return (float) object.getPaddingRight();
        }

        @Override
        public void set(View object, Float value) {
            object.setPadding(object.getPaddingLeft(), object.getPaddingTop(), value.intValue(), object.getPaddingBottom());
        }
    };

    public static Property<View, Float> PADDING_TOP = new FloatProperty<View>("PADDING_TOP") {
        @Override
        public Float get(View object) {
            return (float) object.getPaddingTop();
        }

        @Override
        public void set(View object, Float value) {
            object.setPadding(object.getPaddingLeft(), value.intValue(), object.getPaddingRight(), object.getPaddingBottom());
        }
    };

    public static Property<View, Float> PADDING_BOTTOM = new FloatProperty<View>("PADDING_BOTTOM") {
        @Override
        public Float get(View object) {
            return (float) object.getPaddingBottom();
        }

        @Override
        public void set(View object, Float value) {
            object.setPadding(object.getPaddingLeft(), object.getPaddingTop(), object.getPaddingRight(), value.intValue());
        }
    };
}
