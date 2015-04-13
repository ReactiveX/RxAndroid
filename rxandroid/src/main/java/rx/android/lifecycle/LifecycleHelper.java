package rx.android.lifecycle;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import rx.subjects.BehaviorSubject;

/**
 * This instance lives within the scope of an {@code Activity} in order to forward Lifecycle events
 * to {@link rx.subjects.BehaviorSubject}
 *
 * Thread safe, since it's <i>Thread-confined</i> to the main Thread.
 *
 * @author Nabil Hachicha.
 */
class LifecycleHelper implements Application.ActivityLifecycleCallbacks {
    private final Activity activityToMonitor;
    private final BehaviorSubject<LifecycleEvent> lifecycleSubject;
    /**
     * Help track if {@link #onActivityCreated(Activity, Bundle)} was called
     * This is Thread safe, since it's confined to the <i>main Thread</i>
     */
    private boolean isCreateCalled = false;
    /**
     * Help track if {@link #onActivityStarted(Activity)} was called
     */
    private boolean isStarted = false;

    public LifecycleHelper(Activity instance, BehaviorSubject<LifecycleEvent> lifecycle) {
        activityToMonitor = instance;
        lifecycleSubject = lifecycle;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle bundle) {
        // We may have independents instance of the same Activity in the activity stack
        // this is why we use identity comparison
        if (activity == activityToMonitor) {
            lifecycleSubject.onNext(LifecycleEvent.CREATE);
            isCreateCalled = true;
        }
    }

    @Override
    public void onActivityStarted(Activity activity) {
        if (activity == activityToMonitor) {
            if (!isCreateCalled) {//client bound onStart, sending missed event
                isCreateCalled = true;
                lifecycleSubject.onNext(LifecycleEvent.CREATE);
            }
            lifecycleSubject.onNext(LifecycleEvent.START);
            isStarted = true;
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {
        if (activity == activityToMonitor) {
            if (!isStarted) {//client bound onStart, sending missed event
                isStarted = true;
                lifecycleSubject.onNext(LifecycleEvent.START);
            }
            lifecycleSubject.onNext(LifecycleEvent.RESUME);
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        if (activity == activityToMonitor) {
            lifecycleSubject.onNext(LifecycleEvent.PAUSE);
        }
    }

    @Override
    public void onActivityStopped(Activity activity) {
        if (activity == activityToMonitor) {
            lifecycleSubject.onNext(LifecycleEvent.STOP);
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {}

    @Override
    public void onActivityDestroyed(Activity activity) {
        if (activity == activityToMonitor) {
            lifecycleSubject.onNext(LifecycleEvent.DESTROY);
            //No more callback after this, it's safe to unregister this listener
            activity.getApplication().unregisterActivityLifecycleCallbacks(this);
        }
    }
}
