package rx.android.lifecycle;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * This instance lives within the scope of an {@code Activity} in order to forward Lifecycle events
 * to {@link rx.subjects.BehaviorSubject}
 * <p/>
 * Thread safe, since it's <i>Thread-confined</i> to the main Thread.
 */
class OnSubscribeActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks, Observable.OnSubscribe<LifecycleEvent> {
    private final Activity activityToMonitor;
    private Subscriber<? super LifecycleEvent> subscriber;

//    private final Observable<Activity> obsActivity;

    public OnSubscribeActivityLifecycleCallbacks(Activity instance) {
        activityToMonitor = instance;
    }

    @Override
    public void call(Subscriber<? super LifecycleEvent> subscriber) {
        this.subscriber = subscriber;
    }

    @Override
    public void onActivityCreated(final Activity activity, Bundle bundle) {
        Observable
                .just(activity)
                .filter(filter)
                .subscribe(sendCreateEvent);
    }

    @Override
    public void onActivityStarted(Activity activity) {
        Observable
                .just(activity)
                .filter(filter)
                .subscribe(sendStartEvent);
    }

    @Override
    public void onActivityResumed(Activity activity) {
        Observable
                .just(activity)
                .filter(filter)
                .subscribe(sendResumeEvent);
    }

    @Override
    public void onActivityPaused(Activity activity) {
        Observable
                .just(activity)
                .filter(filter)
                .subscribe(sendPauseEvent);
    }

    @Override
    public void onActivityStopped(Activity activity) {
        Observable
                .just(activity)
                .filter(filter)
                .subscribe(sendStopEvent);
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        Observable
                .just(activity)
                .filter(filter)
                .doOnNext(sendDestroyEvent)
                .subscribe(unregisterActivityLifecycleCallbacks);

    }

    private final Func1<Activity, Boolean> filter =
            new Func1<Activity, Boolean>() {
                @Override
                public Boolean call(Activity callbackActivity) {
                    // Application notify all instances of ActivityLifecycleCallbacks when a particular
                    // event is emitted, we use identity comparison to filter out events related
                    // to the instance of Activity we're currently monitoring
                    return callbackActivity == activityToMonitor;
                }
            };


    // Actions
    private final Action1<Activity> sendCreateEvent = new Action1<Activity>() {
        @Override
        public void call(Activity activity) {
            subscriber.onNext(LifecycleEvent.CREATE);
        }
    };

    private final Action1<Activity> sendStartEvent = new Action1<Activity>() {
        @Override
        public void call(Activity activity) {
            if (subscriber != null) {
                subscriber.onNext(LifecycleEvent.START);
            }
        }
    };

    private final Action1<Activity> sendResumeEvent = new Action1<Activity>() {
        @Override
        public void call(Activity activity) {
            if (subscriber != null) {
                subscriber.onNext(LifecycleEvent.RESUME);
            }
        }
    };

    private final Action1<Activity> sendPauseEvent = new Action1<Activity>() {
        @Override
        public void call(Activity activity) {
            if (subscriber != null) {
                subscriber.onNext(LifecycleEvent.PAUSE);
            }
        }
    };

    private final Action1<Activity> sendStopEvent = new Action1<Activity>() {
        @Override
        public void call(Activity activity) {
            if (subscriber != null) {
                subscriber.onNext(LifecycleEvent.STOP);
            }
        }
    };

    private final Action1<Activity> sendDestroyEvent = new Action1<Activity>() {
        @Override
        public void call(Activity activity) {
            if (subscriber != null) {
                subscriber.onNext(LifecycleEvent.DESTROY);
            }
        }
    };

    private final Action1<Activity> unregisterActivityLifecycleCallbacks = new Action1<Activity>() {
        @Override
        public void call(Activity activity) {
            activity.getApplication().unregisterActivityLifecycleCallbacks(OnSubscribeActivityLifecycleCallbacks.this);
        }
    };
}
