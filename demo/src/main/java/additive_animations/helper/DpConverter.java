package additive_animations.helper;

import android.util.TypedValue;

import additive_animations.AAApplication;

public class DpConverter {

    public static int converDpToPx(float dp) {
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, AAApplication.getContext().getResources().getDisplayMetrics());
        return Math.round(px);
    }
}
