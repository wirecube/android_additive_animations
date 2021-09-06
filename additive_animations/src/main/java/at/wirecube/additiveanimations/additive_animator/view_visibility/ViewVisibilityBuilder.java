package at.wirecube.additiveanimations.additive_animator.view_visibility;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import at.wirecube.additiveanimations.additive_animator.animation_set.AnimationState;

public class ViewVisibilityBuilder extends AnimationState.Builder<View> {

    private final AnimationState.AnimationStartAction<View> visibilityStartAction;
    private final AnimationState.AnimationEndAction<View> visibilityEndAction;

    public ViewVisibilityBuilder(int visibility) {
        switch (visibility) {
            case View.VISIBLE:
                visibilityStartAction = view -> view.setVisibility(View.VISIBLE);
                visibilityEndAction = null;
                break;
            case View.INVISIBLE:
                visibilityStartAction = null;
                visibilityEndAction = (view, wasCancelled) -> view.setVisibility(View.INVISIBLE);
                break;
            case View.GONE:
                visibilityStartAction = null;
                visibilityEndAction = (view, wasCancelled) -> view.setVisibility(View.GONE);
                break;
            default:
                throw new IllegalArgumentException("Cannot instantiate a ViewVisibilityAnimation.Builder without a valid visibility (given: " + visibility + ").");
        }
    }

    @Override
    @NonNull
    public AnimationState.Builder<View> withStartAction(@Nullable AnimationState.AnimationStartAction<View> startAction) {
        return super.withStartAction(view -> {
            if (visibilityStartAction != null) {
                visibilityStartAction.onStart(view);
            }
            if (startAction != null) {
                startAction.onStart(view);
            }
        });
    }

    @Override
    @NonNull
    public AnimationState.Builder<View> withEndAction(@Nullable AnimationState.AnimationEndAction<View> endAction) {
        return super.withEndAction((view, wasCancelled) -> {
            if (visibilityEndAction != null) {
                visibilityEndAction.onEnd(view, wasCancelled);
            }
            if (endAction != null) {
                endAction.onEnd(view, wasCancelled);
            }
        });
    }

}
