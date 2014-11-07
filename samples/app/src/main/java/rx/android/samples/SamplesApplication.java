package rx.android.samples;

import android.app.Application;
import android.os.StrictMode;

public class SamplesApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        StrictMode.enableDefaults();
    }
}
