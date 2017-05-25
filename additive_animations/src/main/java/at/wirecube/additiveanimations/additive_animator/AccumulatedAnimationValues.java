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
import java.util.Map;

/**
 * Helper class for accumulating the changes made by all of the additive animators.
 */
class AccumulatedAnimationValues {
    private Map<AdditiveAnimation, Float> tempProperties = new HashMap<>();
    int totalNumAnimationUpdaters = 0;
//    int updateCounter = 0;

    void addDelta(AdditiveAnimation property, Float delta) {
        tempProperties.put(property, tempProperties.get(property) + delta);
    }

    Map<AdditiveAnimation, Float> getAccumulatedProperties() {
        return tempProperties;
    }
}
