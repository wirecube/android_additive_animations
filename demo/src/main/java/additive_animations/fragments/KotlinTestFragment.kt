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

package additive_animations.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import at.wirecube.additiveanimations.additive_animator.AdditiveAnimator
import at.wirecube.additiveanimations.additiveanimationsdemo.R


// subclass specifies generic type
class AdditiveAnimatorSubclass : AdditiveAnimator<AdditiveAnimatorSubclass> {
    constructor(view: View) : super(view)

    // can simply return subclass
    fun visibleAlpha(visible: Boolean) : AdditiveAnimatorSubclass {
        return alpha(if(visible) 1f else 0f)
    }
}

// generic function extension
fun AdditiveAnimator<*>.visibleAlpha(isVisible: Boolean): AdditiveAnimator<*> {
    val endAlpha = if (isVisible) 1f else 0f
    return alpha(endAlpha)
}

// subclass function extension doesn't require generics
fun AdditiveAnimatorSubclass.xy(x: Float, y: Float): AdditiveAnimatorSubclass {
    return x(x).y(y)
}

class KotlinTestFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater?.inflate(R.layout.fragment_tap_to_move_demo, container, false)!!
        val animatedView = root.findViewById(R.id.animated_view)!!
        AdditiveAnimator.animate(animatedView).alpha(1f)
        root.setOnTouchListener({ _, motionEvent ->
            AdditiveAnimatorSubclass(animatedView).xy(motionEvent.x, motionEvent.y).visibleAlpha(true).start()
            // this works fine as well:
//            AdditiveAnimator.animate(animatedView).x(motionEvent.x).y(motionEvent.y).visibleAlpha(true).start()
            true
        })
        return root
    }
}
