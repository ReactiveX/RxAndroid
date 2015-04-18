package rx.android.lifecycle.scenarios;

import android.widget.Toast;

import rx.Subscription;
import rx.android.lifecycle.LifecycleEvent;
import rx.android.lifecycle.LifecycleObservable;
import rx.android.view.OnClickEvent;
import rx.android.view.ViewObservable;
import rx.functions.Action1;

public class BindOnStopEventActivity extends BaseLifecycleActivity {
    private Subscription subscription;

    @Override
    protected void onStop() {
        super.onStop();
        subscription = LifecycleObservable.bindActivityLifecycle(this,
                ViewObservable.clicks(button),
                LifecycleEvent.STOP)
                .subscribe(new Action1<OnClickEvent>() {
                    @Override
                    public void call(OnClickEvent onClickEvent) {
                        Toast.makeText(BindOnStopEventActivity.this,
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
