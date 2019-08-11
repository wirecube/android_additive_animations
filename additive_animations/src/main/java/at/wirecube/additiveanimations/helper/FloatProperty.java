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

package at.wirecube.additiveanimations.helper;

import android.util.Property;
import android.view.View;

public abstract class FloatProperty <T> extends Property<T, Float> {

    public interface Get<T> {
        float get(T object);
    }

    public interface Set<T> {
        void set(T object, float value);
    }

    public static <T> FloatProperty<T> create(String name, Get<T> getter, Set<T> setter) {
        return new FloatProperty<T>(name) {
            @Override
            public void set(T object, Float value) {
                setter.set(object, value);
            }

            @Override
            public Float get(T object) {
                return getter.get(object);
            }
        };
    }

    public FloatProperty(String name) {
        super(Float.class, name);
    }

    @Override
    public abstract void set(T object, Float value);
}
