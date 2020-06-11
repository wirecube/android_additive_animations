package additive_animations.fragments.sequence;

import java.util.List;

public class AnimationSequenceJson {
    private AnimationType  animationType;
    private List<AnimationSequenceJson> children;
    private List<AnimationInstructionJson> animations;

    public enum AnimationType {
        Spawn, Sequence, AtOnce
    }

    public AnimationSequenceJson() {
    }

    public AnimationSequenceJson(AnimationType type) {
        this.setAnimationType(type);
    }

    public static class AnimationInstructionJson {
        private String propertyName;
        private float value;
        private boolean by;

        public AnimationInstructionJson() {}

        public AnimationInstructionJson(String propertyName, float value, boolean by) {
            this.propertyName = propertyName;
            this.value = value;
            this.by = by;
        }

        public String getPropertyName() {
            return propertyName;
        }

        public void setPropertyName(String propertyName) {
            this.propertyName = propertyName;
        }

        public float getValue() {
            return value;
        }

        public void setValue(float value) {
            this.value = value;
        }

        public boolean isBy() {
            return by;
        }

        public void setBy(boolean by) {
            this.by = by;
        }
    }

    public AnimationType getAnimationType() {
        return animationType;
    }

    public void setAnimationType(AnimationType animationType) {
        this.animationType = animationType;
    }

    public List<AnimationSequenceJson> getChildren() {
        return children;
    }

    public AnimationSequenceJson setChildren(List<AnimationSequenceJson> children) {
        this.children = children;
        return this;
    }

    public List<AnimationInstructionJson> getAnimations() {
        return animations;
    }

    public AnimationSequenceJson setAnimations(List<AnimationInstructionJson> animations) {
        this.animations = animations;
        return this;
    }
}
