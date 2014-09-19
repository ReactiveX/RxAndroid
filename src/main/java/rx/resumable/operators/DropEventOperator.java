package rx.resumable.operators;

import rx.Observer;
import rx.Subscriber;
import rx.Subscription;

public class DropEventOperator<T> implements ObserverOperator<T> {

    private final EventForwardingListener listener;
    private Observer<? super T> observer;

    public DropEventOperator(EventForwardingListener listener) {
        this.listener = listener;
    }

    @Override
    public void call(Subscriber<? super T> subscriber) {
        this.observer = subscriber;
        subscriber.add(new Subscription() {
            @Override
            public void unsubscribe() {
                observer = null;
            }

            @Override
            public boolean isUnsubscribed() {
                return observer == null;
            }
        });
    }

    public Observer<? super T> getObserver() {
        return observer;
    }

    public boolean hasObserver() {
        return observer != null;
    }
    @Override
    public final void onCompleted() {
        listener.allEventsForwarded();
        if (hasObserver()) {
            observer.onCompleted();
        }
    }

    @Override
    public final void onError(final Throwable throwable) {
        listener.allEventsForwarded();
        if (hasObserver()) {
            observer.onError(throwable);
        }
    }

    @Override
    public final void onNext(final T t) {
        if (hasObserver()) {
            observer.onNext(t);
        }
    }
}
