package rx.android.samples.eventbus;

import rx.Subscriber;
import rx.Subscription;
import rx.android.eventbus.EventBus;
import rx.android.samples.R;
import rx.android.samples.SamplesApplication;
import rx.subscriptions.Subscriptions;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

public class EventBusActivity extends Activity {

    private Subscription subscription = Subscriptions.empty();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_bus);

        final EventBus eventBus = ((SamplesApplication) getApplication()).getEventBus();
        final TextView textView = (TextView) findViewById(android.R.id.text1);

        subscription = eventBus.subscribe(Events.STRING_QUEUE, new Subscriber<String>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(String s) {
                textView.setText(s);
            }
        });

        startService(new Intent(this, EventGeneratorService.class));
    }

    @Override
    protected void onDestroy() {
        subscription.unsubscribe();
        super.onDestroy();
    }
}
