package at.wirecube.additiveanimations.additiveanimationsdemo.animation.additive_animations.base;

import android.view.View;

public class AdditiveAnimatorSubclassDemo extends AdditiveAnimator<AdditiveAnimatorSubclassDemo> {

    public AdditiveAnimatorSubclassDemo(View view) {
        super(view);
    }

//    public AdditiveAnimatorSubclassDemo rotation(float targetRotation) {
//        mAnimator.addAnimation(createDescription(View.ROTATION, targetRotation));
//        return this;
//    }
}
