package rx.android.lifecycle;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import rx.Observable;
import rx.Subscriber;

/**
 * This instance lives within the scope of an {@code Activity} in order to forward Lifecycle events
 * to {@link rx.subjects.BehaviorSubject}
 * <p/>
 * Thread safe, since it's <i>Thread-confined</i> to the main Thread.
 */
class OnSubscribeActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks, Observable.OnSubscribe<LifecycleEvent> {
    private final Activity activityToMonitor;
    private Subscriber<? super LifecycleEvent> subscriber;

    public OnSubscribeActivityLifecycleCallbacks(Activity instance) {
        activityToMonitor = instance;
    }

    @Override
    public void call(Subscriber<? super LifecycleEvent> subscriber) {
        this.subscriber = subscriber;
    }

    @Override
    public void onActivityCreated(final Activity activity, Bundle bundle) {
        sendEvent(activity, LifecycleEvent.CREATE);
    }

    @Override
    public void onActivityStarted(Activity activity) {
        sendEvent(activity, LifecycleEvent.START);
    }

    @Override
    public void onActivityResumed(Activity activity) {
        sendEvent(activity, LifecycleEvent.RESUME);
    }

    @Override
    public void onActivityPaused(Activity activity) {
        sendEvent(activity, LifecycleEvent.PAUSE);
    }

    @Override
    public void onActivityStopped(Activity activity) {
        sendEvent(activity, LifecycleEvent.STOP);
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        sendEventAndUnregister(activity, LifecycleEvent.DESTROY);

    }

    private void sendEvent(Activity activity, LifecycleEvent event) {
        // Application notify all instances of ActivityLifecycleCallbacks when a particular
        // event is emitted, we use identity comparison to filter out events related
        // to the instance of Activity we're currently monitoring
        if (activity == activityToMonitor && subscriber != null) {
            subscriber.onNext(event);
        }
    }

    private void sendEventAndUnregister(Activity activity, LifecycleEvent event) {
        if (activity == activityToMonitor && subscriber != null) {
            subscriber.onNext(event);
            activity.getApplication().unregisterActivityLifecycleCallbacks(this);
        }
    }

}
