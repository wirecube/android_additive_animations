package additive_animations.demo;

import android.app.Application;
import android.util.TypedValue;

public class DpConverter {

    public static int converDpToPx(float dp) {
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, AAApplication.getContext().getResources().getDisplayMetrics());
        return Math.round(px);
    }
}
