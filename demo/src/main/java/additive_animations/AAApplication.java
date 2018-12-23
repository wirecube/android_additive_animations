package additive_animations;

import android.app.Application;
import android.content.Context;

import com.squareup.leakcanary.LeakCanary;

public class AAApplication extends Application {

    private static Context sContext;

    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);

        sContext = getApplicationContext();
    }

    public static Context getContext() {
        return sContext;
    }
}
