package rx.android.samples;

import rx.android.eventbus.DefaultEventBus;
import rx.android.eventbus.EventBus;

import android.app.Application;
import android.os.StrictMode;

public class SamplesApplication extends Application {

    private final EventBus eventBus = new DefaultEventBus();

    @Override
    public void onCreate() {
        super.onCreate();
        StrictMode.enableDefaults();
    }

    public EventBus getEventBus() {
        return eventBus;
    }
}
