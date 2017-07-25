package additive_animations.subclass;

import android.view.View;

import at.wirecube.additiveanimations.additive_animator.AdditiveAnimator;

public class AdditiveAnimatorSubclassDemo extends AdditiveAnimator<AdditiveAnimatorSubclassDemo> {

    public static AdditiveAnimatorSubclassDemo animate(View v) {
        return new AdditiveAnimatorSubclassDemo().addTarget(v);
    }

    public AdditiveAnimatorSubclassDemo() { super(); }

    @Override
    protected AdditiveAnimatorSubclassDemo newInstance() {
        return new AdditiveAnimatorSubclassDemo();
    }

    static boolean isPulsing = false;

    public void startPulsing() {
        setDuration(3000);
        getCurrentTarget().setScaleX(0);
        getCurrentTarget().setScaleY(0);
        getCurrentTarget().setAlpha(1f);
        addEndAction(new AnimationEndListener() {
            @Override
            public void onAnimationEnd(boolean wasCancelled) {
                if (isPulsing) {
                    AdditiveAnimatorSubclassDemo.animate(getCurrentTarget()).startPulsing();
                }
            }
        });
        alpha(0);
        scale(1);
        start();
        isPulsing = true;
    }

    public void stopPulsing() {
        isPulsing = false;
    }


    public AdditiveAnimatorSubclassDemo thenBounceBeforeEnd(int millis, long duration) {
        return thenBeforeEnd(millis).scale(1.2f).setDuration(duration)
               .thenBeforeEnd(100).scale(0.8f)
               .thenBeforeEnd(100).scale(1f);
    }
}
