package at.wirecube.additiveanimations.additive_animator;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class to make then-chaining possible when targeting multiple views with a stagger parameter.
 *
 * Rationale:
 * Creating an animation by calling animate(targets, stagger) creates n animators, one for each target.
 * Calling then() on the result has to create n animator again, each targeting the same child as before but with a different delay.
 * The animation group is used to figure out which animators have been then-chained and which ones have been staggered.
 */
class AdditiveAnimatorGroup {

    interface StartDelayProvider {
        long getStartDelay(BaseAdditiveAnimator parent);
    }

    /**
     * The list of grouped animators. The first element is the innermost parent of the group ([1] is child of [0]).
     */
    List<BaseAdditiveAnimator> mAnimators = new ArrayList<>();

    public void add(BaseAdditiveAnimator animator) {
        animator.setAnimationGroup(this);
        mAnimators.add(animator);
    }

    public BaseAdditiveAnimator outermostChildAnimator() {
        return mAnimators.get(mAnimators.size() - 1);
    }

    /**
     * Creates a new animation group with all animators in the group and links them together such that the new group
     * targets the same views in the same order.
     * The first animator in the returned group is the child of the first animator in the current group.
     */
    public AdditiveAnimatorGroup copyAndChain(StartDelayProvider delayProvider) {
        AdditiveAnimatorGroup newGroup = new AdditiveAnimatorGroup();
        BaseAdditiveAnimator parent = outermostChildAnimator();
        BaseAdditiveAnimator newestChild;
        for(BaseAdditiveAnimator animator : mAnimators) {
            newestChild = animator.newInstance();
            // we want to copy the properties from the parent:
            newestChild.setParent(parent);
            // but keep the same target as the current animator in the chain:
            newestChild.target(animator.getCurrentTarget());
            // we also need to make sure the animation timing is correct:
            newestChild.setStartDelay(delayProvider.getStartDelay(animator));

            newGroup.add(newestChild);
            parent = newestChild;
        }
        return newGroup;
    }
}
