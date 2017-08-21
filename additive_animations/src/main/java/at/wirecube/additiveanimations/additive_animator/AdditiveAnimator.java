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

import android.view.View;

/**
 * Additive animations are nicely explained here: http://ronnqvi.st/multiple-animations/
*/
public final class AdditiveAnimator extends SubclassableAdditiveViewAnimator<AdditiveAnimator> {

    /**
     * This is just a convenience method when you need to animate a single view.
     * No state is kept in individual AdditiveAnimator instances, so you don't need to keep a reference to it.
     * @param view The view to animate.
     * @return A new instance of AdditiveAnimator with `target` set to `view`.
     */
    public static AdditiveAnimator animate(View view) {
        return new AdditiveAnimator(view);
    }

    /**
     * This is just a convenience method when you need to animate a single view.
     * No state is kept in individual AdditiveAnimator instances, so you don't need to keep a reference to it.
     * @param view The view to animate.
     * @param duration The animation duration.
     * @return A new instance of AdditiveAnimator with the animation target set to `view` and the animationDuration set to `duration`.
     */
    public static AdditiveAnimator animate(View view, long duration) {
        return new AdditiveAnimator(view).setDuration(duration);
    }


    @Override
    protected AdditiveAnimator newInstance() {
        return new AdditiveAnimator();
    }

    protected AdditiveAnimator(View view) {
        target(view);
    }

    /**
     * Creates a new AdditiveAnimator instance without a target view.
     * You **must** call `target(View v)` before calling one of the animation methods.
     */
    public AdditiveAnimator() {}

    /**
     * Creates a new AdditiveAnimator instance with the specified animation duration for more convenience.
     * You **must** call `target(View v)` before calling one of the animation methods.
     */
    public AdditiveAnimator(long duration) {
        setDuration(duration);
    }

}
