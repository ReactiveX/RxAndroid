package rx.android.samples.eventbus;

import rx.android.eventbus.EventBus;
import rx.android.samples.SamplesApplication;

import android.app.IntentService;
import android.content.Intent;
import android.os.SystemClock;

public class EventGeneratorService extends IntentService {

    public EventGeneratorService() {
        super("BackgroundService");

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final EventBus eventBus = ((SamplesApplication) getApplication()).getEventBus();

        for (int i = 0; i < 10; i++) {
            SystemClock.sleep(2000);
            eventBus.publish(Events.STRING_QUEUE, "event " + i);
        }
    }

}
