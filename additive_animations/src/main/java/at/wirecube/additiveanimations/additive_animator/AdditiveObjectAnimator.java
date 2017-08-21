package at.wirecube.additiveanimations.additive_animator;

public class AdditiveObjectAnimator<V> extends BaseAdditiveAnimator<AdditiveObjectAnimator<V>, V> {

    @Override
    protected AdditiveObjectAnimator<V> newInstance() {
        return new AdditiveObjectAnimator<>();
    }

}
