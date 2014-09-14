package rx.operators;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.functions.Action0;
import rx.observers.EmptyObserver;
import rx.subscriptions.Subscriptions;

public class OperatorSynchronousUnsubscribe<T> implements Observable.Operator<T, T> {
    private static final Observer EMPTY_OBSERVER = new EmptyObserver();

    @Override
    public Subscriber<? super T> call(final Subscriber<? super T> parent) {
        final InnerSubscriber inner = new InnerSubscriber(parent);

        parent.add(Subscriptions.create(new CleanUpAction(inner)));
        parent.add(inner);

        return inner;
    }

    private class CleanUpAction implements Action0 {
        private final InnerSubscriber subscriber;

        private CleanUpAction(final InnerSubscriber subscriber) {
            this.subscriber = subscriber;
        }

        @Override
        public void call() {
            subscriber.cleanup();
        }
    }

    private class InnerSubscriber extends Subscriber<T> {
        private Observer<? super T> observer;

        private InnerSubscriber(final Observer<? super T> observer) {
            this.observer = observer;
        }

        @SuppressWarnings("unchecked")
        public void cleanup() {
            observer = (Observer<T>) EMPTY_OBSERVER;
        }

        @Override
        public void onCompleted() {
            observer.onCompleted();
        }

        @Override
        public void onError(final Throwable throwable) {
            observer.onError(throwable);
        }

        @Override
        public void onNext(final T t) {
            observer.onNext(t);
        }
    }
}
