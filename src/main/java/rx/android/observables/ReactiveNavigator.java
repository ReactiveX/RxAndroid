package rx.android.observables;

import android.app.Activity;
import android.content.Intent;
import android.util.SparseArray;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.exception.CancelledException;
import rx.android.exception.FailedException;

public class ReactiveNavigator {

    private final ActivityStarter activityStarter;

    private static final SparseArray<Observer<? super Intent>> activityResultObservers = new SparseArray<Observer<? super Intent>>();

    public ReactiveNavigator(ActivityStarter activityStarter) {
        this.activityStarter = activityStarter;
    }

    public Observable<Intent> toActivityForResult(final Intent intent, final int requestCode) {
        return Observable.create(new Observable.OnSubscribe<Intent>() {
            @Override
            public void call(Subscriber<? super Intent> subscriber) {
                subscriber.add(new Subscription() {
                    @Override
                    public void unsubscribe() {
                        activityResultObservers.remove(requestCode);
                    }

                    @Override
                    public boolean isUnsubscribed() {
                        return activityResultObservers.get(requestCode) == null;
                    }
                });
                activityResultObservers.put(requestCode, subscriber);
                activityStarter.startActivityForResult(intent, requestCode);
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Observer<? super Intent> observer = activityResultObservers.get(requestCode);
        if (observer == null) {
            return;
        }
        switch (resultCode) {
            case Activity.RESULT_OK:
                observer.onNext(data);
                observer.onCompleted();
                activityResultObservers.remove(requestCode);
                break;
            case Activity.RESULT_CANCELED:
                observer.onError(new CancelledException());
                activityResultObservers.remove(requestCode);
                break;
            default:
                observer.onError(new FailedException());
                break;
        }
    }

}
