package rx.resumable.operators;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import rx.Observer;
import rx.Subscriber;
import rx.Subscription;

public class EventCachingOperator<T> implements ObserverOperator<T> {

    private final List<Event> cachedEvents = new ArrayList<Event>();
    private final EventForwardingListener listener;

    private Observer<? super T> observer;

    public EventCachingOperator(EventForwardingListener listener) {
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
        sendCachedEvents();
    }

    public Observer<? super T> getObserver() {
        return observer;
    }

    public boolean hasObserver() {
        return observer != null;
    }

    private void sendCachedEvents() {
        Iterator<Event> iterator = cachedEvents.iterator();
        while (iterator.hasNext()) {
            Event event = iterator.next();
            iterator.remove();
            event.send();
        }
    }

    @Override
    public final void onCompleted() {
        if (hasObserver()) {
            listener.allEventsForwarded();
            observer.onCompleted();
        } else {
            cachedEvents.add(new Event() {
                @Override
                public void send() {
                    listener.allEventsForwarded();
                    observer.onCompleted();
                }
            });
        }
    }

    @Override
    public final void onError(final Throwable throwable) {
        if (hasObserver()) {
            listener.allEventsForwarded();
            observer.onError(throwable);
        } else {
            cachedEvents.add(new Event() {
                @Override
                public void send() {
                    listener.allEventsForwarded();
                    observer.onError(throwable);
                }
            });
        }
    }

    @Override
    public final void onNext(final T t) {
        if (hasObserver()) {
            observer.onNext(t);
        } else {
            cachedEvents.add(new Event() {
                @Override
                public void send() {
                    observer.onNext(t);
                }
            });
        }
    }

    private interface Event {
        void send();
    }
}
