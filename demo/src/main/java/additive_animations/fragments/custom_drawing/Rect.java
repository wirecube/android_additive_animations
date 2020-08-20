package additive_animations.fragments.custom_drawing;

import android.graphics.Paint;
import android.view.View;

import additive_animations.helper.DpConverter;
import at.wirecube.additiveanimations.additiveanimationsdemo.R;

public class Rect {
    private View mView;
    final Paint mPaint;
    float mRotation = 0;
    float mX = DpConverter.converDpToPx(60);
    float mY = DpConverter.converDpToPx(120);
    float mSize = DpConverter.converDpToPx(100);
    float mCornerRadius = 0;

    public Rect(View parent) {
        mView = parent;
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(parent.getContext().getResources().getColor(R.color.niceBlue));
    }

    public View getView() {
        return mView;
    }

    public void clearView() {
        mView = null;
    }
}
