package rx.android.app;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import rx.Observable;
import rx.android.lifecycle.LifecycleEvent;
import rx.subjects.BehaviorSubject;

/**
 * A Service with reactive extensions.
 */
public abstract class RxIntentService extends IntentService {

    private final BehaviorSubject<LifecycleEvent> lifecycleSubject = BehaviorSubject.create();

    public RxIntentService(String name) {
        super(name);
    }

    public Observable<LifecycleEvent> lifecycle() {
        return lifecycleSubject.asObservable();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        lifecycleSubject.onNext(LifecycleEvent.CREATE);
    }

    @Override
    public IBinder onBind(Intent intent) {
        lifecycleSubject.onNext(LifecycleEvent.BIND);
        return null;
    }

    @Override
    public void onRebind(Intent intent) {
        super.onRebind(intent);
        lifecycleSubject.onNext(LifecycleEvent.BIND);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        lifecycleSubject.onNext(LifecycleEvent.UNBIND);
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        lifecycleSubject.onNext(LifecycleEvent.DESTROY);
        super.onDestroy();
    }
}
