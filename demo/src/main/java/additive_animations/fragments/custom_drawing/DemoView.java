package additive_animations.fragments.custom_drawing;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import additive_animations.helper.DpConverter;
import at.wirecube.additiveanimations.additive_animator.AdditiveObjectAnimator;
import at.wirecube.additiveanimations.additive_animator.ViewAnimationApplier;
import at.wirecube.additiveanimations.additiveanimationsdemo.R;
import at.wirecube.additiveanimations.helper.FloatProperty;
import at.wirecube.additiveanimations.helper.evaluators.ColorEvaluator;

public class DemoView extends View {

    final List<Rect> mRects = new ArrayList<>();
    final List<Paint> mPaints = new ArrayList<>();

    // Animating a non-view property using AdditiveObjectAnimator just requires a getter and setter for the property:
    private FloatProperty<Paint> mPaintColorProperty = new FloatProperty<Paint>("PaintColor") {
        @Override
        public Float get(Paint paint) {
            return Float.valueOf(paint.getColor());
        }

        @Override
        public void set(Paint object, Float value) {
            object.setColor(value.intValue());
        }
    };

    public DemoView(Context context) {
        super(context);
        for(int i = 0; i < 5; i++) {
            Rect rect = new Rect(this);
            mRects.add(rect);
            mPaints.add(rect.mPaint);
        }

        // Helper to tell AdditiveObjectAnimator how to make our changes visible.
        // In our case, we just want to invalidate ourselves to trigger a redraw of the canvas.
        Runnable animationApplier = new ViewAnimationApplier(this);

        long delayBetweenAnimations = 100;

        // Use the custom subclass to animate size and corner radius of all rects
        new AdditiveRectAnimator().setDuration(1000).setRepeatCount(ValueAnimator.INFINITE).setRepeatMode(ValueAnimator.REVERSE)
                .targets(mRects, delayBetweenAnimations)
                .size(DpConverter.converDpToPx(80))
                .cornerRadius(DpConverter.converDpToPx(50))
                .start();

        // Default object animator to animate all the paints:
        new AdditiveObjectAnimator<Paint>()
                .setDuration(1000)
                .setRepeatCount(ValueAnimator.INFINITE)
                .setRepeatMode(ValueAnimator.REVERSE)
                .setAnimationApplier(animationApplier)
                .targets(mPaints, delayBetweenAnimations)
                .property(context.getResources().getColor(R.color.niceGreen), new ColorEvaluator(), mPaintColorProperty)
                .start();

        setOnTouchListener(new OnTouchListener() {
            float rotationTarget = 0;

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE) {
                    if (event.getX() >= getRootView().getWidth() / 2) {
                        rotationTarget += 10;
                    } else {
                        rotationTarget -= 10;
                    }
                    // moving the custom-drawn view additively around the canvas just like a normal view:
                    new AdditiveRectAnimator().targets(mRects, 50).x(event.getX()).y(event.getY()).rotation(rotationTarget).start();
                }
                return true;
            }
        });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        AdditiveRectAnimator.cancelAnimations(mRects);
        AdditiveRectAnimator.cancelAnimations(mPaints);
    }

    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        for(Rect rect : mRects) {
            canvas.save();

            // make sure we rotate around the center
            canvas.translate(rect.mX, rect.mY);
            canvas.rotate(rect.mRotation);

            float hs = rect.mSize / 2;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                // the canvas center is now at mRect.mX/mRect.mY, so we need to draw ourselves from -size/2 to size/2.
                canvas.drawRoundRect(-hs, -hs, hs, hs, rect.mCornerRadius, rect.mCornerRadius, rect.mPaint);
            } else {
                // No rounded corners for API <= 21 :(
                canvas.drawRect(-hs, -hs, hs, hs, rect.mPaint);
            }

            canvas.restore();
        }
    }
}
