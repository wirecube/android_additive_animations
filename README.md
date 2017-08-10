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
# Integration
To use `AdditiveAnimator` in your project, add the following lines to your `build.gradle`:
```
dependencies {
compile 'at.wirecube:additive_animations:1.3.1'
}
...
repositories {
    jcenter()
}
```

# License
`AdditiveAnimator` is licensed under the Apache v2 license:

```
Copyright 2017 David Ganster

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
