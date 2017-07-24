# android_additive_animations
Additive animations for Android! Inspired by a blog Post by David Rönnqvist: http://ronnqvi.st/multiple-animations/

This library provides an easy way to additively animate a huge number of properties. 
Some of the functionality was inspired by the excellent ViewPropertyObjectAnimator library: https://github.com/blipinsk/ViewPropertyObjectAnimator.

Here is a sample of what additive animations can do to the user experience (note: there seem to be a few dropped frames in the gif which aren't present when running on a device):


![Additive animations demo](https://github.com/davidganster/android_additive_animations/blob/master/gif/single_view.gif?raw=true)


The amount code required to produce this animation is trivial:

```java
public boolean onTouch(View v, MotionEvent event) {
    AdditiveAnimator.animate(animatedView).x(event.getX()).y(event.getY()).setDuration(1000).start();
}
```

Additionally, `AdditiveAnimator` supports animating multiple views simultaneously without additional boilerplate:

```java
new AdditiveAnimator().setDuration(1000)
    .setTarget(myView1).x(100).y(100)
    .setTarget(myView2).xBy(20).yBy(20)
    .start();
```
