# Android additive animations

Additive animations for Android!
An easy way to additively animate a huge number of properties. 

Get a good overview of this library here: https://medium.com/@david.gansterd/bringing-smooth-animation-transitions-to-android-88786347e512


# Quick start
Here is a sample of what additive animations can do to the user experience (note: there seem to be a few dropped frames in the gif which aren't present when running on a device):


![Additive animations demo](https://github.com/davidganster/android_additive_animations/blob/master/gif/single_view.gif?raw=true)


The amount code required to produce this animation is trivial:

```java
public boolean onTouch(View v, MotionEvent event) {
    AdditiveAnimator.animate(animatedView).x(event.getX()).y(event.getY()).setDuration(1000).start();
    return true;
}
```

Additionally, `AdditiveAnimator` supports animating multiple views simultaneously without any boilerplate:

```java
new AdditiveAnimator().setDuration(1000)
    .target(myView1).x(100).y(100)
    .target(myView2).xBy(20).yBy(20)
    .start();
```

To use `AdditiveAnimator` in your project, add the following lines to your `build.gradle`:
```
dependencies {
    compile 'at.wirecube:additive_animations:1.3'
}
...
repositories {
    jcenter()
}
```
