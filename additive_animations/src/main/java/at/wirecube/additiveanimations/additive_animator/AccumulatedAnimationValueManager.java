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

package at.wirecube.additiveanimations.additive_animator;

import java.util.HashMap;

/**
 * Helper class for accumulating the changes made by all of the additive animators.
 */
class AccumulatedAnimationValueManager {

    private HashMap<AdditiveAnimation, AccumulatedAnimationValue> accumulatedAnimationValues = new HashMap<>();

    // Returns an accumulator that you should store if possible
    public AccumulatedAnimationValue getAccumulatedAnimationValue(AdditiveAnimation animation) {
        // TODO: is there any way to make this `get()` faster?
        AccumulatedAnimationValue accumulatedAnimationValue = accumulatedAnimationValues.get(animation);
        if(accumulatedAnimationValue != null) {
            return accumulatedAnimationValue;
        }
        accumulatedAnimationValue = new AccumulatedAnimationValue(animation);
        accumulatedAnimationValues.put(animation, accumulatedAnimationValue);
        return accumulatedAnimationValue;
    }

}
