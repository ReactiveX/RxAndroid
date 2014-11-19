package rx.android.samples.eventbus;

import rx.android.eventbus.EventBus;
import rx.android.samples.SamplesApplication;

import android.app.IntentService;
import android.content.Intent;
import android.os.SystemClock;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class EventGeneratorService extends IntentService {

    public EventGeneratorService() {
        super("BackgroundService");

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        final EventBus eventBus = ((SamplesApplication) getApplication()).getEventBus();

        for (int i = 0; i < 10; i++) {
            SystemClock.sleep(2000);
            eventBus.publish(EventQueue.STRING_QUEUE, "event " + i);
        }
    }

}
