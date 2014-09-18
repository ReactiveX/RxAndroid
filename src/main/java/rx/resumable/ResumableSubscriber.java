package rx.resumable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import rx.Observable;
import rx.Subscription;
import rx.resumable.observer.ResumableObserver;
import rx.resumable.operators.DropEventOperator;
import rx.resumable.operators.EventCachingOperator;
import rx.resumable.operators.EventForwardingListener;
import rx.resumable.subject.ForwardingSubject;

public class ResumableSubscriber {

    private final List<SubscriptionWithId> subscriptions = new ArrayList<SubscriptionWithId>();
    private final ResumableReference resumableReference;
    private final ObservableVault observableVault;
    private final ObserverFactory factory;

    public ResumableSubscriber(ResumableReference resumableReference, ObserverFactory factory, ObservableVault observableVault) {
        this.resumableReference = resumableReference;
        this.observableVault = observableVault;
        this.factory = factory;
    }

    public void resume() {
        Map<Integer, Observable> observableMap = observableVault.getImmutableObservablesFor(resumableReference);
        for (Map.Entry<Integer, Observable> observableEntry : observableMap.entrySet()) {
            int observerId = observableEntry.getKey();
            Observable observable = observableEntry.getValue();
            final Subscription subscription = observable.subscribe(factory.createObserver(observerId));
            subscriptions.add(new SubscriptionWithId(subscription, observerId));
        }
    }

    public void pause() {
        for (Subscription subscription : subscriptions) {
            subscription.unsubscribe();
        }
        subscriptions.clear();
    }

    public <T> void subscribe(Observable<? extends T> observable, ResumableObserver<T> observer) {
        ForwardingSubject<T> proxy = new ForwardingSubject<T>(new EventCachingOperator<T>(new CleanObservable(observer.getId())));
        observable.subscribe(proxy);
        subscribe(observer, proxy);
    }

    public <T> void subscribeWithReplay(Observable<? extends T> observable, ResumableObserver<T> observer) {
        Observable<? extends T> replayEnabled = observable.cache();
        subscribe(observer, replayEnabled);
    }

    public <T> void subscribeWithDrop(Observable<? extends T> observable, ResumableObserver<T> observer) {
        ForwardingSubject<T> proxy = new ForwardingSubject<T>(new DropEventOperator<T>(new CleanObservable(observer.getId())));
        observable.subscribe(proxy);
        subscribe(observer, proxy);
    }

    private <T> void subscribe(final ResumableObserver<T> observer, final Observable<? extends T> observable) {
        observableVault.put(resumableReference, observer.getId(), observable);
        final SubscriptionWithId subscription = new SubscriptionWithId(observable.subscribe(observer), observer.getId());
        subscriptions.add(subscription);
    }

    public void unsubscribe(final int observerId) {
        for (Iterator<SubscriptionWithId> iterator = subscriptions.iterator(); iterator.hasNext(); ) {
            SubscriptionWithId subscription = iterator.next();
            if (subscription.getId() == observerId) {
                subscription.unsubscribe();
                iterator.remove();
            }
        }
        observableVault.remove(resumableReference, observerId);
    }

    private class CleanObservable implements EventForwardingListener {

        private final int id;

        public CleanObservable(int id) {
            this.id = id;
        }

        @Override
        public void allEventsForwarded() {
            observableVault.remove(resumableReference, id);
        }
    }

    private static final class SubscriptionWithId implements Subscription {
        private final Subscription subscription;
        private final int id;

        private SubscriptionWithId(final Subscription subscription, final int id) {
            this.subscription = subscription;
            this.id = id;
        }

        public int getId() {
            return id;
        }

        @Override
        public void unsubscribe() {
            subscription.unsubscribe();
        }

        @Override
        public boolean isUnsubscribed() {
            return subscription.isUnsubscribed();
        }
    }

}
