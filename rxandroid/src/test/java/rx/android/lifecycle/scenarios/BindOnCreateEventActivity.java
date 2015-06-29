package rx.android.lifecycle.scenarios;

import android.os.Bundle;
import android.widget.Toast;

import rx.Subscription;
import rx.android.lifecycle.LifecycleEvent;
import rx.android.lifecycle.LifecycleObservable;
import rx.android.view.OnClickEvent;
import rx.android.view.ViewObservable;
import rx.functions.Action1;

public class BindOnCreateEventActivity extends BaseLifecycleActivity {
    private Subscription subscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        subscription = LifecycleObservable.bindActivityUntilLifecycle(this,
                ViewObservable.clicks(button),
                LifecycleEvent.DESTROY)
                .subscribe(new Action1<OnClickEvent>() {
                    @Override
                    public void call(OnClickEvent onClickEvent) {
                        Toast.makeText(BindOnCreateEventActivity.this,
                                "Clicked button!",
                                Toast.LENGTH_SHORT)
                                .show();
                    }
                });

        button.performClick();
    }

    @Override
    public Subscription getSubscription() {
        return subscription;
    }
}
