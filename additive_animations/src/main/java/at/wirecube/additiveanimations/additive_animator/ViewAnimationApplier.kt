package at.wirecube.additiveanimations.additive_animator;

import android.view.View;

public class ViewAnimationApplier implements Runnable {

    private final View mTarget;
    private final boolean mRequestLayout;

    public ViewAnimationApplier(View target) {
        mTarget = target;
        mRequestLayout = false;
    }

    public ViewAnimationApplier(View target, boolean requestLayout) {
        mTarget = target;
        mRequestLayout = requestLayout;
    }

    @Override
    public void run() {
        if(mRequestLayout) {
            mTarget.requestLayout();
        } else {
            mTarget.invalidate();
        }
    }
}
