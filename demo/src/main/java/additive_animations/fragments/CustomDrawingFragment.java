package additive_animations.fragments;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.animation.LinearOutSlowInInterpolator;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import additive_animations.helper.DpConverter;
import at.wirecube.additiveanimations.additive_animator.AdditiveAnimation;
import at.wirecube.additiveanimations.additive_animator.AdditiveObjectAnimator;
import at.wirecube.additiveanimations.additive_animator.BaseAdditiveAnimator;
import at.wirecube.additiveanimations.additiveanimationsdemo.R;
import at.wirecube.additiveanimations.helper.FloatProperty;
import at.wirecube.additiveanimations.helper.evaluators.ColorEvaluator;

public class CustomDrawingFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_custom_drawing, container, false);
        View v = new DemoView(root.getContext());
        root.addView(v);
        return root;
    }

    private static class DemoView extends View {

        final List<Rect> mRects = new ArrayList<>();
        final Paint mPaint = new Paint();

        private static class Rect {
            private final View mParent;
            float mRotation = 0;
            float mX = DpConverter.converDpToPx(60);
            float mY = DpConverter.converDpToPx(120);
            float mSize = DpConverter.converDpToPx(100);
            float mCornerRadius = 0;

            Rect(View parent) {
                mParent = parent;
            }

            // Example of an animator subclass that works with a class which doesn't derive from `View`
            private static class AdditiveRectAnimator extends BaseAdditiveAnimator<AdditiveRectAnimator, Rect> {

                private static final String X = "RectX";
                private static final String Y = "RectY";
                private static final String SIZE = "RectSize";
                private static final String CORNER_RADIUS = "RectCornerRadius";
                private static final String ROTATION = "RectRotation";

                @Override
                protected AdditiveRectAnimator newInstance() {
                    return new AdditiveRectAnimator();
                }

                public static AdditiveRectAnimator animate(Rect rect) {
                    return new AdditiveRectAnimator().target(rect);
                }

                public AdditiveRectAnimator x(float x) {
                    // AdditiveAnimation objects can be instantiated with a getter/setter or just by providing a key which will later be retrieved in applyCustomProperties()
                    return animate(new AdditiveAnimation(mCurrentTarget, X, mCurrentTarget.mX, x));
                }
                public AdditiveRectAnimator y(float y) {
                    return animate(new AdditiveAnimation(mCurrentTarget, Y, mCurrentTarget.mY, y));
                }
                public AdditiveRectAnimator size(float size) {
                    return animate(new AdditiveAnimation(mCurrentTarget, SIZE, mCurrentTarget.mSize, size));
                }
                public AdditiveRectAnimator cornerRadius(float cornerRadius) {
                    return animate(new AdditiveAnimation(mCurrentTarget, CORNER_RADIUS, mCurrentTarget.mCornerRadius, cornerRadius));
                }
                public AdditiveRectAnimator rotation(float rotation) {
                    return animate(new AdditiveAnimation(mCurrentTarget, ROTATION, mCurrentTarget.mRotation, rotation));
                }

                // This method is called when we try to animate keys without a getter/setter, as we do in this example
                @Override
                protected void applyCustomProperties(Map<String, Float> tempProperties, Rect target) {
                    for(Map.Entry<String, Float> entry : tempProperties.entrySet()) {
                        if(entry.getKey().equals(X)) {
                           target.mX = entry.getValue();
                        }
                        if(entry.getKey().equals(Y)) {
                            target.mY = entry.getValue();
                        }
                        if(entry.getKey().equals(SIZE)) {
                            target.mSize = entry.getValue();
                        }
                        if(entry.getKey().equals(ROTATION)) {
                            target.mRotation = entry.getValue();
                        }
                        if(entry.getKey().equals(CORNER_RADIUS)) {
                            target.mCornerRadius = entry.getValue();
                        }
                    }

                    // force redraw of the parent view:
                    target.mParent.invalidate();
                }
            }
        }

        // Animating a non-view property using AdditiveObjectAnimator just requires a getter and setter for the property:
        private FloatProperty<Paint> paintColorProperty = new FloatProperty<Paint>("PaintColor") {
            @Override
            public Float get(Paint paint) {
                return Float.valueOf(paint.getColor());
            }

            @Override
            public void set(Paint object, Float value) {
                object.setColor(value.intValue());
            }
        };

        // Helper to tell AdditiveObjectAnimator how to make our changes visible.
        // In our case, we just want to invalidate ourselves to trigger a redraw of the canvas.
        private Runnable animationApplier = new Runnable() {
            @Override
            public void run() {
                invalidate();
            }
        };

        public DemoView(Context context) {
            super(context);
            for(int i = 0; i < 10; i++) {
                mRects.add(new Rect(this));
            }

            mPaint.setStyle(Paint.Style.FILL);
            mPaint.setColor(context.getResources().getColor(R.color.niceBlue));

            // Animate the color of the paint object without a subclass
            AdditiveObjectAnimator.animate(mPaint)
                    .setRepeatCount(ValueAnimator.INFINITE)
                    .setRepeatMode(ValueAnimator.REVERSE)
                    .setAnimationApplier(animationApplier)
                    .property(context.getResources().getColor(R.color.niceGreen), new ColorEvaluator(), paintColorProperty)
                    .start();

            // Use the custom subclass to animate size and corner radius of all rects
            Rect.AdditiveRectAnimator rectAnimator = new Rect.AdditiveRectAnimator();

            for(Rect rect : mRects) {
                rectAnimator = rectAnimator.target(rect)
                                           .setDuration(1000)
                                           .setRepeatCount(ValueAnimator.INFINITE).setRepeatMode(ValueAnimator.REVERSE)
                                           .size(DpConverter.converDpToPx(100))
                                           .cornerRadius(DpConverter.converDpToPx(50))
                                           .thenWithDelay(100);
            }
            rectAnimator.start();

            setOnTouchListener(new OnTouchListener() {
                float rotationTarget = 0;

                @Override
                public boolean onTouch(View view, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                        // moving the custom-drawn view additively around the canvas just like a normal view:
                        if (event.getX() >= getRootView().getWidth() / 2) {
                            rotationTarget += 10;
                        } else {
                            rotationTarget -= 10;
                        }
                        Rect.AdditiveRectAnimator animator = new Rect.AdditiveRectAnimator();
                        for(Rect rect : mRects) {
                            animator = animator.target(rect)
                                               .rotation(rotationTarget)
                                               .x(event.getX())
                                               .y(event.getY())
                                               .thenWithDelay(50);
                        }
                        animator.start();
                    }
                    return true;
                }
            });
        }

        @Override
        protected void onDetachedFromWindow() {
            super.onDetachedFromWindow();
            AdditiveObjectAnimator.cancelAnimations(mPaint);
            for(Rect rect : mRects) {
                Rect.AdditiveRectAnimator.cancelAnimations(rect);
            }
        }

        @Override protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            for(Rect rect : mRects) {
                canvas.save();

                // make sure we rotate around the center
                canvas.translate(rect.mX, rect.mY);
                canvas.rotate(rect.mRotation);

                float hs = rect.mSize / 2; // half size

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    // the canvas center is now at mRect.mX/mRect.mY, so we need to draw ourselves from -size/2 to size/2.
                    canvas.drawRoundRect(-hs, -hs, hs, hs, rect.mCornerRadius, rect.mCornerRadius, mPaint);
                } else {
                    // No rounded corners for API <= 21 :(
                    canvas.drawRect(-hs, -hs, hs, hs, mPaint);
                }

                canvas.restore();
            }
        }
    }


}
