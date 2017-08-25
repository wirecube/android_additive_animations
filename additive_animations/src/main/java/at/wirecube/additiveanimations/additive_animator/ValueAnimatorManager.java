package at.wirecube.additiveanimations.additive_animator;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.util.Log;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

// Responsible for managing a ValueAnimator shared by all animators in a `then()` chain.
// This class is used to vastly improve performance when chaining a lot of animators together, and
// provides the ability to repeat entire sets of animations in different repeat modes (as provided by ValueAnimator)
class ValueAnimatorManager {

    private ValueAnimator mValueAnimator = ValueAnimator.ofFloat(0, 1);
    private List<BaseAdditiveAnimator> mAnimationAccumulators = new ArrayList<>(1);
    private BaseAdditiveAnimator[] mAnimationAccumulatorArray;
    private int mNumRunningAnimators;
    private int mValueAnimatorRepeatCount = 0;

    ValueAnimatorManager() {
        mValueAnimator.addUpdateListener(mAnimatorUpdateListener);
        mValueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationRepeat(Animator animation) {
                mValueAnimatorRepeatCount++;
            }
        });
        mValueAnimator.setInterpolator(new LinearInterpolator());
    }

    // Sets the overall repeat count of ALL added accumulators (set of animations)
    public void setRepeatCount(int repeatCount) {
        mValueAnimator.setRepeatCount(repeatCount);
    }

    // Sets the overall repeat mode of ALL added accumulators (set of animations)
    public void setRepeatMode(int repeatMode) {
        mValueAnimator.setRepeatMode(repeatMode);
    }

    public void start() {
        // sort animators such that they
        Collections.reverse(mAnimationAccumulators);
        for(BaseAdditiveAnimator animator : mAnimationAccumulators) {
            animator.getAnimationAccumulator().onAnimationStart();
        }
        mAnimationAccumulatorArray = new BaseAdditiveAnimator[mAnimationAccumulators.size()];
        mAnimationAccumulators.toArray(mAnimationAccumulatorArray);
        mNumRunningAnimators = mAnimationAccumulators.size();

        mValueAnimator.start();
    }

    public void addAnimator(BaseAdditiveAnimator animator) {
        if(mValueAnimator.isStarted()) {
            throw new IllegalStateException("Cannot modify a running ValueAnimatorManager");
        }

        long totalDuration = calculateTotalDuration(animator);
        if(animator.getRepeatCount() == ValueAnimator.INFINITE) {
            mValueAnimator.setDuration(animator.getDuration());
            mValueAnimator.setRepeatCount(ValueAnimator.INFINITE);
            // TODO: disable erroneous inspection warning
            mValueAnimator.setRepeatMode(animator.getRepeatMode());
        } else if(mValueAnimator.getDuration() < totalDuration) {
            mValueAnimator.setDuration(totalDuration);
        }
        mAnimationAccumulators.add(animator);
    }

    // Includes start delay
    private long calculateTotalDuration(BaseAdditiveAnimator accumulator) {
        if(accumulator.getRepeatCount() == ValueAnimator.INFINITE) {
            return ValueAnimator.DURATION_INFINITE;
        } else {
            return ((accumulator.getRepeatCount() + 1) * accumulator.getDuration()) + accumulator.getStartDelay();
        }
    }

    private ValueAnimator.AnimatorUpdateListener mAnimatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {

            // based on current play time:
            // * check if canceled
            // * queue for removal if cancelled
            // * call onAnimationStart listeners
            // * call onAnimationUpdate listeners
            // * call onAnimationEnd listeners
            // * if all animations are canceled or done - cancel animator.

            // Let's hope using getAnimatedFraction() is good enough for any number of consecutive animations
            float animatedFraction = valueAnimator.getAnimatedFraction();

            boolean animationWasCancelled = false;
            for(int i = 0; i < mAnimationAccumulatorArray.length; i++) {
                BaseAdditiveAnimator animator = mAnimationAccumulatorArray[i];
                if(animator == null) {
                    continue;
                }
                AdditiveAnimationAccumulator acc = animator.getAnimationAccumulator();
                if(acc.getCanceled()) {
                    acc.onAnimationCancel();
                    acc.onAnimationEnd();
                    mNumRunningAnimators--;
                    animationWasCancelled = true;
                    mAnimationAccumulatorArray[i] = null;
                    continue;
                }
                animationWasCancelled = false;

                if(calculateStartFraction(animator) > animatedFraction) {
                    // not yet started, and our list is sorted - so all the other ones after this one won't start yet either!
                    // TODO: determine how much time is remaining until the start of the animation. if remainingTime < timeToNextFrame/2, call onAnimationStart() now.
                    continue;
                }

                float fractionComplete = calculateTotalFractionComplete(animatedFraction, animator);
                float fractionInCurrentRepeat = calculateCurrentRepeatFraction(fractionComplete, animator);
                // make sure the animation gets its final update even if fractionComplete == 1
                acc.onAnimationUpdate(fractionInCurrentRepeat);

                if(fractionComplete >= 1 && mValueAnimatorRepeatCount >= mValueAnimator.getRepeatCount() && mValueAnimator.getRepeatCount() != ValueAnimator.INFINITE) {
                    acc.onAnimationEnd();
                    mAnimationAccumulatorArray[i] = null;
                    mNumRunningAnimators--;
                }
            }

            if(mNumRunningAnimators == 0) {
                // done!
                // TODO: is this expected behaviour?
                if(animationWasCancelled) {
                    mValueAnimator.cancel();
                } else {
                    // TODO: how to handle?
                }
            }
        }
    };

//    private float calculateFractionComplete(long currentAnimatedTime, BaseAdditiveAnimator acc) {
//        if(currentAnimatedTime > calculateTotalDuration(acc)) {
//            return 1;
//        }
//        float fractionCompleteInCurrentRepeat = (float)((currentAnimatedTime - acc.getStartDelay()) % acc.getDuration())/(float)acc.getDuration();
//        long currentRepeat = (long)Math.floor((currentAnimatedTime - acc.getStartDelay())/acc.getDuration());
//        boolean isOddRepeat = currentRepeat % 2 != 0;
//        if(acc.getRepeatMode() == ValueAnimator.REVERSE && isOddRepeat) {
//            fractionCompleteInCurrentRepeat = 1 - fractionCompleteInCurrentRepeat;
//        }
//        return clamp(fractionCompleteInCurrentRepeat);
//    }

    private float calculateTotalFractionComplete(float totalFractionComplete, BaseAdditiveAnimator animator) {
        if(calculateTotalDuration(animator) == ValueAnimator.DURATION_INFINITE) {
            // infinite animations are just mapped 1:1 to our duration.
            return totalFractionComplete;
        }
        float startFraction = calculateStartFraction(animator);
        float endFraction = calculateEndFraction(animator); // should include all repeats
        if(endFraction == 0) {
            // the animation duration is 0, meaning we are done immediately
            return 1;
        }

        // simple case with no repeats:
        float fractionComplete = (totalFractionComplete - startFraction) / (endFraction - startFraction);
        return clamp(fractionComplete);
    }

    private float calculateCurrentRepeatFraction(float fractionInAnimator, BaseAdditiveAnimator animator) {
        if(calculateTotalDuration(animator) == ValueAnimator.DURATION_INFINITE) {
            // infinite animations are just mapped 1:1 to our duration.
            return fractionInAnimator;
        }
        int currentRepeat = calculateCurrentRepeat(fractionInAnimator, animator);
        fractionInAnimator = fractionInAnimator * (animator.getRepeatCount() + 1) - (1.0f/(animator.getRepeatCount() + 1))*currentRepeat;
        if(currentRepeat % 2 != 0 && animator.getRepeatMode() == ValueAnimator.REVERSE) {
            fractionInAnimator = 1f - fractionInAnimator;
        }
        if(fractionInAnimator > 1 || fractionInAnimator < 0) {
            Log.e("ValueAnimatorManager", "incorrect fractionComplete: " + fractionInAnimator);
        }
        return fractionInAnimator;
    }

    // ValueAnimator.getTotalDuration() is only available on API >= 24
    private float getValueAnimatorTotalDuration() {
        if (mValueAnimator.getRepeatCount() == ValueAnimator.INFINITE) {
            return ValueAnimator.DURATION_INFINITE;
        } else {
            return mValueAnimator.getStartDelay() + (mValueAnimator.getDuration() * (mValueAnimator.getRepeatCount() + 1));
        }
    }

    private float calculateStartFraction(BaseAdditiveAnimator acc) {
        if(getValueAnimatorTotalDuration() == 0) {
            return 0;
        }
        return acc.getStartDelay()/getValueAnimatorTotalDuration();
    }

    private float calculateEndFraction(BaseAdditiveAnimator acc) {
        if(getValueAnimatorTotalDuration() == 0) {
            return 0;
        }

        return (float)calculateTotalDuration(acc)/getValueAnimatorTotalDuration();
    }

    private int calculateCurrentRepeat(float relativeFractionComplete, BaseAdditiveAnimator acc) {
        if(acc.getRepeatCount() == ValueAnimator.INFINITE) {
            return 0;
        }
        float repeatPercentage = 1f/acc.getRepeatCount();
        int currentRepeat = 0;
        while(relativeFractionComplete - repeatPercentage > 0) {
            currentRepeat++;
            relativeFractionComplete -= repeatPercentage;
        }
        return currentRepeat;
    }

    private float clamp(float fraction) {
        return Math.min(Math.max(fraction, 0), 1);
    }

    public void addListener(Animator.AnimatorListener listener) {
        mValueAnimator.addListener(listener);
    }

    public void setInterpolator(TimeInterpolator interpolator) {
        mValueAnimator.setInterpolator(interpolator);
    }

    public void addUpdateListener(ValueAnimator.AnimatorUpdateListener listener) {
        mValueAnimator.addUpdateListener(listener);
    }

    public TimeInterpolator getInterpolator() {
        return mValueAnimator.getInterpolator();
    }

    public void setStartDelay(long startDelay) {
        mValueAnimator.setStartDelay(startDelay);
    }

    public long getStartDelay() {
        return mValueAnimator.getStartDelay();
    }
}
