package rx.android.eventbus;

import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Actions;
import rx.subjects.BehaviorSubject;
import rx.subjects.Subject;

final class ReplayEventSubject<T> extends Subject<T, T> {

    private final Subject<T, T> wrappedSubject;
    private final Action1<Throwable> onError;

    private static final class OnSubscribeFunc<T> implements OnSubscribe<T> {

        private final BehaviorSubject<T> subject;

        private OnSubscribeFunc(T defaultEvent) {
            if (defaultEvent == null) {
                subject = BehaviorSubject.create();
            } else {
                subject = BehaviorSubject.create(defaultEvent);
            }
        }

        @Override
        public void call(Subscriber<? super T> subscriber) {
            subject.subscribe(subscriber);
        }
    }

    static <T> ReplayEventSubject<T> create() {
        final Action1<Throwable> empty = Actions.empty();
        return new ReplayEventSubject<T>(new OnSubscribeFunc<T>(null), empty);
    }

    static <T> ReplayEventSubject<T> create(Action1<Throwable> onError) {
        return new ReplayEventSubject<T>(new OnSubscribeFunc<T>(null), onError);
    }

    static <T> ReplayEventSubject<T> create(T defaultEvent) {
        final Action1<Throwable> empty = Actions.empty();
        return new ReplayEventSubject<T>(new OnSubscribeFunc<T>(defaultEvent), empty);
    }

    static <T> ReplayEventSubject<T> create(T defaultEvent, Action1<Throwable> onError) {
        return new ReplayEventSubject<T>(new OnSubscribeFunc<T>(defaultEvent), onError);
    }

    private ReplayEventSubject(OnSubscribeFunc<T> onSubscribeFunc, Action1<Throwable> onError) {
        super(onSubscribeFunc);
        this.wrappedSubject = onSubscribeFunc.subject;
        this.onError = onError;
    }

    @Override
    public void onCompleted() {
        // never process onCompleted
    }

    @Override
    public void onError(Throwable e) {
        onError.call(e);
    }

    @Override
    public void onNext(T t) {
        wrappedSubject.onNext(t);
    }

    @Override
    public boolean hasObservers() {
        return wrappedSubject.hasObservers();
    }
}
