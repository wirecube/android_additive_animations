package additive_animations.fragments;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.bartoszlipinski.viewpropertyobjectanimator.ViewPropertyObjectAnimator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import additive_animations.AdditiveAnimationsShowcaseActivity;
import at.wirecube.additiveanimations.additive_animator.AdditiveAnimator;
import at.wirecube.additiveanimations.additiveanimationsdemo.R;

public class MultipleViewsAnimationDemoFragment extends Fragment {
    FrameLayout rootView;
    int rotation = 0;

    List<View> views = new ArrayList<>();

    @SuppressLint("ClickableViewAccessibility")
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = (FrameLayout) inflater.inflate(R.layout.fragment_multiple_views_demo, container, false);
        views = Arrays.asList(
                rootView.findViewById(R.id.animated_view3), rootView.findViewById(R.id.animated_view5),
                rootView.findViewById(R.id.animated_view6), rootView.findViewById(R.id.animated_view7),
                rootView.findViewById(R.id.animated_view8), rootView.findViewById(R.id.animated_view9),
                rootView.findViewById(R.id.animated_view10), rootView.findViewById(R.id.animated_view11),
                rootView.findViewById(R.id.animated_view12), rootView.findViewById(R.id.animated_view13),
                rootView.findViewById(R.id.animated_view14), rootView.findViewById(R.id.animated_view15),
                rootView.findViewById(R.id.animated_view16), rootView.findViewById(R.id.animated_view17),
                rootView.findViewById(R.id.animated_view18), rootView.findViewById(R.id.animated_view19),
                rootView.findViewById(R.id.animated_view20), rootView.findViewById(R.id.animated_view21),
                rootView.findViewById(R.id.animated_view22), rootView.findViewById(R.id.animated_view23),
                rootView.findViewById(R.id.animated_view24), rootView.findViewById(R.id.animated_view25));

        for(View v : views) {
            v.setAlpha(0.2f);
        }

        final int blue = getResources().getColor(R.color.niceBlue);
        final int pink = getResources().getColor(R.color.nicePink);

        rootView.setOnTouchListener(new View.OnTouchListener() {
            Date lastTouchEvent = new Date();
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN || event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_UP) {
                    if((new Date().getTime() - lastTouchEvent.getTime()) < 200 && event.getAction() != MotionEvent.ACTION_UP) {
                        // Throttle a little - a single call of this function enqueues about 100 animators,
                        // which means we have about 6000 animators after one second of dragging around.
                        // Unfortunately, that's too much for the current animation system to handle,
                        // because creating ValueAnimators is a very expensive operation.
                        // There is some work on using a single shared value animator for all AdditiveAnimator instances,
                        // but it's extremely difficult to handle repeat modes correctly, and performance is not really an issue at the moment anyway.
                        return true;
                    }
                    lastTouchEvent = new Date();
                    float x = event.getX();
                    float y = event.getY();

                    if(event.getAction() == MotionEvent.ACTION_UP && AdditiveAnimationsShowcaseActivity.ADDITIVE_ANIMATIONS_ENABLED) {
                        // snap to 360° only when using additive animations - you won't ever see the views rotate without additive animations otherwise.
                        rotation = 0;
                    } else if(x < rootView.getWidth()/2.0) {
                        rotation -= 30;
                    } else {
                        rotation += 30;
                    }

                    long animationStagger = 50;
                    if(AdditiveAnimationsShowcaseActivity.ADDITIVE_ANIMATIONS_ENABLED) {
                        // what this line does:
                        // 1. Create a new additive animator that targets all View objects in the views array.
                        //    The animationStagger describes how much delay should occur between the animations of each animated View.
                        // 2. Adding some animations to the view: x(), y(), rotation().
                        //    These animations apply to all views that were previously targeted.
                        // 3. thenWithDelay() enqueues new animations after the specified delay.
                        //    All targets that we previously set are still valid, and the stagger between the views is preserved.
                        //    Example: views[0] started with 0ms delay, views[1] started with 50ms delay.
                        //             By calling thenWithDelay(200), the next animations for views[0] will run at 200ms, the ones for view[1] will run at 250ms.
                        // 4. start() starts the entire block on animations.
                        AdditiveAnimator.animate(views, animationStagger)
                                .x(x).y(y).rotation(rotation)
                                .thenWithDelay(200).scale(1.5f).backgroundColor(blue)
                                .thenWithDelay(200).scale(1.f).backgroundColor(pink)
                                .start();
                    } else {
                        // This approximates the animation code from above, but is much more verbose and doesn't even really work:
                        // ValueAnimator can't handle startDelays very gracefully, so you'll see a lot of jumping when dragging your finger across the screen.
                        // We also can't do background color, and the two scale animations will overwrite one another.
                        for(int i = 0; i < views.size(); i++) {
                            ViewPropertyObjectAnimator.animate(views.get(i))
                                    .setStartDelay(animationStagger * i)
                                    .setDuration(1000)
                                    .x(x).y(y).rotation(rotation)
                                    .start();

                            ViewPropertyObjectAnimator.animate(views.get(i))
                                    .setStartDelay(animationStagger * i + 200)
                                    .setDuration(1000)
                                    .scales(1.5f)
                                    // no support for background color :/
                                    .start();

                            ViewPropertyObjectAnimator.animate(views.get(i))
                                    .setStartDelay(animationStagger * i + 200 + 200)
                                    .setDuration(1000)
                                    .scales(1.f)
                                    // no support for background color :/
                                    .start();
                        }
                    }
                }
                return true;
            }
        });
        return rootView;
    }
}
