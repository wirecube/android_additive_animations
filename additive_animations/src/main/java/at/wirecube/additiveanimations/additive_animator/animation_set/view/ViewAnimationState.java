package at.wirecube.additiveanimations.additive_animator.animation_set.view;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import at.wirecube.additiveanimations.additive_animator.animation_set.AnimationAction;
import at.wirecube.additiveanimations.additive_animator.animation_set.AnimationState;

public class ViewAnimationState extends AnimationState<View> {

    @NonNull
    private final List<AnimationAction.Animation<View>> mAnimations = new ArrayList<>();
    @Nullable
    private final AnimationStartAction<View> mStartAction;
    @Nullable
    private final AnimationEndAction<View> mEndAction;

    public ViewAnimationState(@NonNull List<ViewAnimation> animations) {
        this(animations, null, null);
    }

    public ViewAnimationState(@NonNull ViewAnimation... animations) {
        this(Arrays.asList(animations), null, null);
    }

    public ViewAnimationState(@NonNull List<ViewAnimation> animations, @Nullable AnimationStartAction<View> startAction) {
        this(animations, startAction, null);
    }

    public ViewAnimationState(ViewAnimation animation, @Nullable AnimationStartAction<View> startAction) {
        this(Arrays.asList(animation), startAction, null);
    }

    public ViewAnimationState(@Nullable AnimationStartAction<View> startAction, @NonNull ViewAnimation... animations) {
        this(Arrays.asList(animations), startAction, null);
    }

    public ViewAnimationState(@NonNull ViewAnimation animation, @Nullable AnimationEndAction<View> endAction) {
        this(Arrays.asList(animation), null, endAction);
    }

    public ViewAnimationState(@NonNull List<ViewAnimation> animations, @Nullable AnimationEndAction<View> endAction) {
        this(animations, null, endAction);
    }

    public ViewAnimationState(@Nullable AnimationEndAction<View> endAction, @NonNull ViewAnimation... animations) {
        this(Arrays.asList(animations), null, endAction);
    }

    public ViewAnimationState(@NonNull ViewAnimation animation, @Nullable AnimationStartAction<View> startAction, @Nullable AnimationEndAction<View> endAction) {
        this(Arrays.asList(animation), startAction, endAction);
    }

    public ViewAnimationState(@Nullable AnimationStartAction<View> startAction, @Nullable AnimationEndAction<View> endAction, @NonNull ViewAnimation... animations) {
        this(Arrays.asList(animations), startAction, endAction);
    }

    public ViewAnimationState(@NonNull List<ViewAnimation> animations, @Nullable AnimationStartAction<View> startAction, @Nullable AnimationEndAction<View> endAction) {
        mAnimations.addAll(animations);
        mStartAction = startAction;
        mEndAction = endAction;
    }

    @Override
    public List<Animation<View>> getAnimations() {
        return mAnimations;
    }

    @Override
    public AnimationStartAction<View> getAnimationStartAction() {
        return mStartAction;
    }

    @Override
    public AnimationEndAction<View> getAnimationEndAction() {
        return mEndAction;
    }
}
