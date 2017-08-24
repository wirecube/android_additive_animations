package at.wirecube.additiveanimations.additive_animator;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.renderscript.Sampler;
import android.util.Log;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;

// Responsible for managing a ValueAnimator shared by all animators in a `then()` chain.
// This class is used to vastly improve performance when chaining a lot of animators together, and
// provides the ability to repeat entire sets of animations in different repeat modes (as provided by ValueAnimator)
class ValueAnimatorManager {

    private ValueAnimator mValueAnimator = ValueAnimator.ofFloat(0, 1);
    private List<AdditiveAnimationAccumulator> mAnimationAccumulators = new ArrayList<>(1);
    private List<Integer> indicesToRemove = new ArrayList<>(); // done/canceled animators are removed

    ValueAnimatorManager() {
        mValueAnimator.addUpdateListener(mAnimatorUpdateListener);
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
        // sort animators such that they are ordered by start delay
        Collections.sort(mAnimationAccumulators, new Comparator<AdditiveAnimationAccumulator>() {
            @Override
            public int compare(AdditiveAnimationAccumulator o1, AdditiveAnimationAccumulator o2) {
                return Long.valueOf(o1.getStartDelay()).compareTo(Long.valueOf(o2.getStartDelay()));
            }
        });
        mValueAnimator.start();
    }

    public void addAnimationAccumulator(AdditiveAnimationAccumulator accumulator) {
        long totalDuration = calculateTotalDuration(accumulator);
        if(accumulator.getRepeatCount() == ValueAnimator.INFINITE) {
            mValueAnimator.setDuration(accumulator.getDuration());
            mValueAnimator.setRepeatCount(ValueAnimator.INFINITE);
            mValueAnimator.setRepeatMode(accumulator.getRepeatMode());
        } else if(mValueAnimator.getDuration() < totalDuration) {
            mValueAnimator.setDuration(totalDuration);
        }
        mAnimationAccumulators.add(accumulator);
    }

    // Includes start delay
    private long calculateTotalDuration(AdditiveAnimationAccumulator accumulator) {
        if(accumulator.getRepeatCount() == ValueAnimator.INFINITE) {
            return ValueAnimator.DURATION_INFINITE;
        } else {
            return accumulator.getRepeatCount() * accumulator.getDuration() + accumulator.getStartDelay();
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

            // TODO: must not use getCurrentPlayTime(), its resolution too coarse.
            // Let's hope using getAnimatedFraction() is good enough
            float animatedFraction = valueAnimator.getAnimatedFraction();

//            long currentAnimatedTime = valueAnimator.getCurrentPlayTime();
            indicesToRemove.clear();
            boolean animationWasCancelled = false;
            for(int i = 0; i < mAnimationAccumulators.size(); i++) {
                AdditiveAnimationAccumulator acc = mAnimationAccumulators.get(i);
                if(acc.getCanceled()) {
                    acc.onAnimationCancel();
                    acc.onAnimationEnd();
                    indicesToRemove.add(i);
                    animationWasCancelled = true;
                    continue;
                }
                animationWasCancelled = false;

                float fractionComplete = calculateFractionComplete(animatedFraction, acc);
                acc.onAnimationUpdate(fractionComplete);

                if(fractionComplete >= 1) {
                    acc.onAnimationEnd();
                    indicesToRemove.add(i);
                }

                if(acc.getStartDelay() > valueAnimator.getCurrentPlayTime()) {
                    // not yet started, and our list is sorted - so all the other ones don't start yet either!
                    break;
                }
            }

            for(int i = indicesToRemove.size() - 1; i >= 0; i--) {
                mAnimationAccumulators.remove(indicesToRemove.get(i));
            }

            if(mAnimationAccumulators.size() == 0) {
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

    private float calculateFractionComplete(long currentAnimatedTime, AdditiveAnimationAccumulator acc) {
        if(currentAnimatedTime > calculateTotalDuration(acc)) {
            return 1;
        }
        float fractionCompleteInCurrentRepeat = (float)((currentAnimatedTime - acc.getStartDelay()) % acc.getDuration())/(float)acc.getDuration();
        long currentRepeat = (long)Math.floor((currentAnimatedTime - acc.getStartDelay())/acc.getDuration());
        boolean isOddRepeat = currentRepeat % 2 != 0;
        if(acc.getRepeatMode() == ValueAnimator.REVERSE && isOddRepeat) {
            fractionCompleteInCurrentRepeat = 1 - fractionCompleteInCurrentRepeat;
        }
        return clamp(fractionCompleteInCurrentRepeat);
    }

    private float calculateFractionComplete(float totalFractionComplete, AdditiveAnimationAccumulator acc) {
        if(calculateTotalDuration(acc) == ValueAnimator.DURATION_INFINITE) {
            // infinite animations are just mapped 1:1 to our duration.
            return totalFractionComplete;
        }
        float startFraction = calculateStartFraction(acc);
        float endFraction = calculateEndFraction(acc); // should include all repeats
        if(endFraction == 0) {
            // the animation duration is 0, meaning we are done immediately
            return 1;
        }

        // simple case with no repeats:
        float fractionComplete = (totalFractionComplete - startFraction) / (endFraction - startFraction);

        int currentRepeat = calculateCurrentRepeat(fractionComplete, acc);
        fractionComplete = fractionComplete * acc.getRepeatCount() - (1.0f/acc.getRepeatCount())*currentRepeat;
        if(currentRepeat % 2 != 0 && acc.getRepeatMode() == ValueAnimator.REVERSE) {
            fractionComplete = 1f - fractionComplete;
        }
        return fractionComplete;
    }

    private float calculateStartFraction(AdditiveAnimationAccumulator acc) {
        if(mValueAnimator.getTotalDuration() == 0) {
            return 0;
        }
        return acc.getStartDelay()/mValueAnimator.getTotalDuration();
    }

    private float calculateEndFraction(AdditiveAnimationAccumulator acc) {
        if(mValueAnimator.getTotalDuration() == 0) {
            return 0;
        }

        return (float)calculateTotalDuration(acc)/(float)mValueAnimator.getTotalDuration();
    }

    private int calculateCurrentRepeat(float relativeFractionComplete, AdditiveAnimationAccumulator acc) {
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
