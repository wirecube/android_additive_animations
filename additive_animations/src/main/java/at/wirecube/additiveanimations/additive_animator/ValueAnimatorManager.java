package at.wirecube.additiveanimations.additive_animator;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.util.Log;
import android.view.animation.LinearInterpolator;

import java.security.cert.CertificateNotYetValidException;
import java.util.ArrayList;
import java.util.List;

// Responsible for managing a ValueAnimator shared by all animators in a `then()` chain.
// This class is used to vastly improve performance when chaining a lot of animators together, and
// provides the ability to repeat entire sets of animations in different repeat modes (as provided by ValueAnimator)
class ValueAnimatorManager {

    private ValueAnimator mValueAnimator = ValueAnimator.ofFloat(0, 1);
    private List<BaseAdditiveAnimator> mAnimators = new ArrayList<>(1);
    private BaseAdditiveAnimator[] mAnimatorArray;
    private int mNumRunningAnimators;
    private int mValueAnimatorRepeatCount = 0;

    ValueAnimatorManager() {
        mValueAnimator.addUpdateListener(mAnimatorUpdateListener);
        mValueAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationRepeat(Animator animation) {
                if(mNumRunningAnimators > 0) {
                    // we didn't call onAnimationUpdate()/onAnimationEnd() on all of them correctly!
                    onAnimationUpdate(1f);
                }
                mValueAnimatorRepeatCount++;

                mAnimators.toArray(mAnimatorArray);
                mNumRunningAnimators = mAnimators.size();

                for(int i = mAnimatorArray.length - 1; i >= 0; i--) {
                    BaseAdditiveAnimator animator = mAnimatorArray[i];
                    animator.getAnimationAccumulator().onAnimationRepeat();
                    animator.getAnimationAccumulator().setPrepareAnimationStartValuesOnRepeat(false);
                }
            }
        });
        mValueAnimator.setInterpolator(new LinearInterpolator());
    }

    private boolean willRunInReverseNextTime() {
        if(mValueAnimator.getRepeatMode() == ValueAnimator.REVERSE) {
            // this is a workaround for repeating animations freaking out when run in reverse.
            // TODO: this breaks the additive nature of animations, so we should instead update the target/start values properly.
            return true;
        } else {
            return false;
        }
//        mValueAnimatorRepeatCount++;
//        boolean willRunInReverse = isRunningInReverse();
//        mValueAnimatorRepeatCount--;
//        return willRunInReverse;
    }

    private boolean isRunningInReverse() {
        return mValueAnimatorRepeatCount % 2 != 0 && mValueAnimator.getRepeatMode() == ValueAnimator.REVERSE;
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
        mAnimatorArray = new BaseAdditiveAnimator[mAnimators.size()];
        mAnimators.toArray(mAnimatorArray);

        for(int i = mAnimatorArray.length - 1; i >= 0; i--) {
            BaseAdditiveAnimator animator = mAnimatorArray[i];
            animator.getAnimationAccumulator().onAnimationStart();
        }

        mNumRunningAnimators = mAnimators.size();
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
            // we always restart, the animator itself will have the reverse/restart flag set.
            mValueAnimator.setRepeatMode(animator.getRepeatMode());
//            animator.setRepeatMode(ValueAnimator.RESTART);
        } else if(mValueAnimator.getDuration() < totalDuration) {
            mValueAnimator.setDuration(totalDuration);
        }
        mAnimators.add(animator);
    }

    // Includes start delay
    private long calculateTotalDuration(BaseAdditiveAnimator accumulator) {
        if(accumulator.getRepeatCount() == ValueAnimator.INFINITE) {
            return ValueAnimator.DURATION_INFINITE;
        } else {
            return ((accumulator.getRepeatCount() + 1) * accumulator.getDuration()) + accumulator.getRawStartDelay();
        }
    }

    private void onAnimationUpdate(float animatedFraction) {
        // based on current play time:
        // * check if canceled
        // * queue for removal if cancelled
        // * call onAnimationStart listeners
        // * call onAnimationUpdate listeners
        // * call onAnimationEnd listeners
        // * if all animations are canceled or done - cancel animator.

        boolean animationWasCancelled = false;
        for(int i = mAnimatorArray.length - 1; i >= 0; i--) {
            BaseAdditiveAnimator animator = mAnimatorArray[i];
            if(animator == null) {
                continue;
            }
            AdditiveAnimationAccumulator acc = animator.getAnimationAccumulator();
            if(acc.getCanceled()) {
                acc.onAnimationCancel();
                acc.onAnimationEnd(false);
                mNumRunningAnimators--;
                animationWasCancelled = true;
                mAnimatorArray[i] = null;
                mAnimators.remove(acc);
                continue;
            }
            animationWasCancelled = false;

            if(calculateStartFraction(animator) > animatedFraction) {
                // not yet started, but we don't break here because other animators might have been cancelled.
                continue;
            }

            float fractionComplete = calculateTotalFractionComplete(animatedFraction, animator);
            float fractionInCurrentRepeat = calculateCurrentRepeatFraction(fractionComplete, animator);
            // make sure the animation gets its final update even if fractionComplete == 1
            acc.onAnimationUpdate(fractionInCurrentRepeat);

            // TODO: correct onAnimationEnd callback when number of animator repeats == animator.getRepeatCount().
            if(fractionComplete >= 1) {
                mAnimatorArray[i] = null;
                mNumRunningAnimators--;
                if(calculateCurrentRepeat(fractionComplete, animator) == animator.getRepeatCount() || mValueAnimator.getRepeatCount() == ValueAnimator.INFINITE) {
                    // let's see if our value animator will repeat:
                    boolean willRepeat = mValueAnimatorRepeatCount < mValueAnimator.getRepeatCount() || mValueAnimator.getRepeatCount() == ValueAnimator.INFINITE;
                    boolean prepareAnimationStartValues = !willRunInReverseNextTime();
                    acc.setPrepareAnimationStartValuesOnRepeat(prepareAnimationStartValues);
                    acc.onAnimationEnd(willRepeat);
                }
            }
        }

        if(mNumRunningAnimators == 0 && mValueAnimatorRepeatCount == mValueAnimator.getRepeatCount()) {
            // done!
            // TODO: is this expected behaviour?
            if(animationWasCancelled) {
                mValueAnimator.cancel();
            } else {
                // TODO: how to handle?
            }
        }
    }

    private ValueAnimator.AnimatorUpdateListener mAnimatorUpdateListener = new ValueAnimator.AnimatorUpdateListener() {
        @Override
        public void onAnimationUpdate(ValueAnimator valueAnimator) {
            // Let's hope using getAnimatedFraction() is good enough for any number of consecutive animations
            float animatedFraction = valueAnimator.getAnimatedFraction();
            ValueAnimatorManager.this.onAnimationUpdate(animatedFraction);
        }
    };

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
        float fractionInCurrentRepeat = (fractionInAnimator - ((1.0f / (animator.getRepeatCount() + 1)) * currentRepeat)) * (animator.getRepeatCount() + 1);
        if(currentRepeat % 2 != 0 && animator.getRepeatMode() == ValueAnimator.REVERSE) {
            fractionInCurrentRepeat = 1f - fractionInCurrentRepeat;
        }
        if(fractionInCurrentRepeat > 1 || fractionInCurrentRepeat < 0) {
            Log.e("ValueAnimatorManager", "incorrect fractionComplete: " + fractionInCurrentRepeat);
        }
        return clamp(fractionInCurrentRepeat);
    }

    // ValueAnimator.getTotalDuration() is only available on API >= 24
    private float getValueAnimatorTotalDuration() {
        if (mValueAnimator.getRepeatCount() == ValueAnimator.INFINITE) {
            return ValueAnimator.DURATION_INFINITE;
        } else {
            return mValueAnimator.getStartDelay() + (mValueAnimator.getDuration() * (mValueAnimator.getRepeatCount() + 1));
        }
    }

    private float calculateStartFraction(BaseAdditiveAnimator animator) {
        float totalDuration = getValueAnimatorTotalDuration();
        if(totalDuration == 0) {
            return 0;
        }
        if(totalDuration == ValueAnimator.DURATION_INFINITE) {
            return (float)animator.getRawStartDelay()/(float)mValueAnimator.getDuration();
        } else {
            totalDuration /= (mValueAnimator.getRepeatCount() + 1);
            return animator.getRawStartDelay() / totalDuration;
        }
    }

    private float calculateEndFraction(BaseAdditiveAnimator animator) {
        float totalDuration = getValueAnimatorTotalDuration();
        if(totalDuration == 0) {
            return 0;
        }
        if(totalDuration == ValueAnimator.DURATION_INFINITE) {
            float multiplier = 1;
            if(animator.getRepeatCount() != ValueAnimator.INFINITE) {
                multiplier = animator.getRepeatCount() + 1;
            }
            return ((animator.getDuration() * multiplier) + animator.getRawStartDelay())/(float)mValueAnimator.getDuration();
        } else {
            totalDuration /= (mValueAnimator.getRepeatCount() + 1);
            return (float) calculateTotalDuration(animator) / totalDuration;
        }
    }

    private int calculateCurrentRepeat(float relativeFractionComplete, BaseAdditiveAnimator animator) {
        if(animator.getRepeatCount() == ValueAnimator.INFINITE) {
            return 0;
        }
        float repeatPercentage = 1f/(animator.getRepeatCount() + 1);
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
