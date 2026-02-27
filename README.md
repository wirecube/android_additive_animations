# Android Additive Animations

Additive animations for Android!
An easy way to additively animate any property of any object, with convenient builder methods for `View`s.

Get a good overview of this library here: https://medium.com/@david.gansterd/bringing-smooth-animation-transitions-to-android-88786347e512

# NOTE:
The latest public version of `AdditiveAnimator` is 1.9.3. I don't have time to keep the releases on maven central up-to-date, but the code still reflects the latest version.
Feel free to fork/use the source directly.

The newest versions of the library support physics-based spring animations, like Figma's "gentle" animation curve (similar to `spring()` in Compose), and introduce kotlin to improve ergonomics.

# Integration
To use `AdditiveAnimator` in your project, add the following lines to your `build.gradle`:
```
dependencies {
    compile 'at.wirecube:additive_animations:1.9.3'
}
```

# Quick start
Here is a sample of what additive animations can do for the user experience (note: there seem to be a few dropped frames in the gif which aren't present when running on a device):


![Additive animations demo](https://github.com/davidganster/android_additive_animations/blob/master/gif/single_view.gif?raw=true)


The amount code required to produce this animation is trivial:

```java
public boolean onTouch(View v, MotionEvent event) {
    AdditiveAnimator.animate(animatedView, 1000).x(event.getX()).y(event.getY()).start();
    return true;
}
```

Additionally, `AdditiveAnimator` supports animating multiple targets simultaneously without any boilerplate:

```java
new AdditiveAnimator().setDuration(1000)
                      .target(myView1).x(100).y(100)
                      .target(myView2).xBy(20).yBy(20)
                      .start();
```

**New in 1.6:**

1.6 added a some convenience features, such as the ability to switch duration midway to building an animation, providing a `SpringInterpolator` class, and being able to switch back to the default interpolator using the `switchToDefaultInterpolator()` method.

Then main attraction of 1.6 though:

You can now animate the same property for multiple views without looping.

```java
new AdditiveAnimator().targets(myView1, myView2).alpha(0).start();
```

To achieve a delay between the start of the animation of each target, you can optionally add the 'stagger' parameter to add a delay between each of the animations.
```java
long staggerBetweenAnimations = 50L;
new AdditiveAnimator().targets(Arrays.asList(myView1, myView2), staggerBetweenAnimations).alpha(0).start();
```
In this example, `myView1` is faded out 50 milliseconds before `myView2`.

Starting with 1.6.1, the delay between the animation of the views is preserved when using `then()` chaining:
```java
long staggerBetweenAnimations = 50L;
AdditiveAnimator.animate(Arrays.asList(myView1, myView2), staggerBetweenAnimations).translationYBy(50).thenWithDelay(20).translationYBy(-50).start();
```

The timeline of this animation looks like this:
`myView1` is translated by 50 pixels at delay 0.
`myView2` is translated by 50 pixels at delay 50.
`myView1` is translated by -50 pixles at delay 20.
`myView2` is translated by -50 pixles at delay 70.

Check out `MultipleViewsAnimationDemoFragment` in the demo app for an example of this!


# Visibility animations
**New in 1.7.2**

View visibility can now be properly animated without adding an animation end block and checking if the visibility should be updated based on some other state variable:

```java
AdditiveAnimator.animate(view)
    .fadeVisibility(View.GONE) // fades out the view, then sets visibility to GONE
    .start();
```

Since fading the visibiliy is probably the most common usecase, there's a default builder method for it. 
A few more default animations are provided as well:

```java
AdditiveAnimator.animate(view)
    // the first param decides whether the view should be GONE or INVISIBLE,
    // the second one decides how much to move the view as it fades out
    .visibility(ViewVisibilityAnimation.fadeOutAndTranslateX(true, 100f)) // only move x
    .visibility(ViewVisibilityAnimation.fadeOutAndTranslateY(true, 100f)) // only move y
    .visibility(ViewVisibilityAnimation.fadeOutAndTranslate(true, 100f, 100f)) // move x and y
    .start();
```

The new `ViewVisibilityAnimation` class provides a convenient constructor to make your own view state animations - an example can be found in the new demo (`StateDemoFragment`).

**New in 1.9.2**
The API for building new AnimationStates and view visibility animations has been improved.
You can now access the `AnimationState.Builder<T>` class to more easily use one-off states.
There are also more specializations for `View`-specific classes, like the `ViewAnimation`, `ViewAnimationState` and `ViewStateBuilder`.

# Animation States

`AdditiveAnimator` now supports the concept of _animation states_.
A __State__ encapsulates a set of animations to perform when an object changes its... state.

What's special about this is that `AdditiveAnimator` can now automatically decide whether or not to run animation start and end blocks - if the view is no longer in the appropriate state for the block, it won't run.

This is how the view visibility feature is implemented, and it can easily be extended to work with all kinds of custom states via the new `state()` builder method.

For example, we might want to switch the states of some views between __highlighted__ and __normal__ in a `then()`-chained block like this:

```java
new AdditiveAnimator()
    .targets(normalViews)
    .scale(1f) // normal
    .then()
    .target(highlightedView)
    .scale(1.2f) // highlighted
    .start();
```

There's a race condition in this piece of code: The `then()`-chained animation is executed whether or not the `highlightedView` is actually still highlighted by the time the previous animation finishes.

Animation states fix this problem entirely:

```java
new AdditiveAnimator()
    .targets(normalViews)
    .state(MyViewState.NORMAL)
    .then()
    .target(highlightedView)
    .state(MyViewState.HIGHLIGHTED)
    .start();
```

With this code, the animations associated with the `NORMAL` and `HIGHLIGHTED` states are only allowed to run if the state of the enqueued animation still matches the current view state.
Even when rapidly switching which view is highlighted, this will produce the desired outcome.

# Animating all kinds of objects and properties
In addition to the builder methods for views, there are multiple options for animating custom properties of any object.
The first - *highly recommended* - option is to simply provide a `Property` for the object you want to animate, plus (if needed) a way to trigger a redraw of your custom object:

```java
// Declaring an animatable property:
FloatProperty<Paint> mPaintColorProperty = 
        FloatProperty.create("PaintColor", paint -> (float) paint.getColor(), (paint, color) -> paint.setColor((int) color));
...

// Using the property to animate the color of a paint:
AdditiveObjectAnimator.animate(myPaint)
    .property(targetColor, // target value
              new ColorEvaluator(), // custom evaluator for colors
              mPaintColorProperty) // how to get/set the property value
    .setAnimationApplier(new ViewAnimationApplier(myView)) // tells the generic AdditiveObjectAnimator how to apply the changed values
    .start();
```

The second option is not recommended unless you need very specific control over how properties are applied (for example, only applying x/y-scroll changes together instead of one at a time when animating 2-dimensional scrolling).
In works by subclassing `BaseAdditiveAnimator` and providing your own builder methods (which are usually one-liners) such as this:

```java
class PaintAdditiveAnimator extends BaseAdditiveAnimator<PaintAdditiveAnimator, Paint> {
    private static final String COLOR_ANIMATION_KEY = "ANIMATION_KEY";

    // Support animation chaining by providing a construction method:
    @Override protected PaintAdditiveAnimator newInstance() { return new PaintAdditiveAnimator(); }

    // Custom builder method for animating the color of a Paint:
    public PaintAdditiveAnimator color(int color) {
        return animate(new AdditiveAnimation<>(
            mCurrentTarget, // animated object (usually this is the current target)
            COLOR_ANIMATION_KEY, // key to identify the animation
            mCurrentTarget.getColor(), // start value
            color)); // target value
    }

    // Applying the changed properties when they don't have a Property wrapper:
    @Override protected void applyCustomProperties(Map<String, Float> tempProperties, Paint target) {
        if(tempProperties.containsKey(COLOR_ANIMATION_KEY)) {
            target.setColor(tempProperties.get(COLOR_ANIMATION_KEY).intValue());
        }
    }
    
    // For animations without a property, your subclass is responsible for providing the current property value.
    // This is easy to forget when adding new animatable properties, which is one of the reasons this method is discouraged.
    @Override public Float getCurrentPropertyValue(String propertyName) {
        switch(propertyName) {
            case ANIMATION_KEY:
                return mCurrentTarget.getColor();
        }
        return null;
    }
}
```

A more complete example of both of these approaches can be found in the sample app in  `CustomDrawingFragment.java`.


Of course you can combine both approaches - custom builder methods which animate properties. This is the **recommended approach** and is how everything provided by `AdditiveAnimator` was built.

Both versions only require very little code, and the few lines you have to write are almost always trivial - mostly getters and setters.

### Note: 
There is a  breaking change when migrating from a version <1.5.0 to a version >= 1.5.0:
Instead of subclassing `AdditiveAnimator`, you now have to subclass `SubclassableAdditiveViewAnimator` instead.
Sorry for the change, it was necessary due to Java constraints (nesting of generics across subclasses) and improves interop with Kotlin (no more generic arguments required!).

### Note
There is another breaking change when migrating from <1.6.0 to >= 1.6.0:
You have to implement a new abstract method (`getCurrentPropertyValue()`) when subclassing `BaseAdditiveAnimator`.
This method is only called when using tag-based animations, instead of property-based ones. If your subclass does not use tag-based animations, you can simply  `return null;`.

# License
`AdditiveAnimator` is licensed under the Apache v2 license:

```
Copyright 2021 David Ganster

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
