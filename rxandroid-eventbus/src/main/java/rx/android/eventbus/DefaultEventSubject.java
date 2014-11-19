package rx.android.eventbus;

import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Actions;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

final class DefaultEventSubject<T> extends Subject<T, T> {

    private final Subject<T, T> wrappedSubject;
    private final Action1<Throwable> onError;

    private static final class OnSubscribeFunc<T> implements OnSubscribe<T> {

        private final PublishSubject<T> subject = PublishSubject.create();

        @Override
        public void call(Subscriber<? super T> subscriber) {
            subject.subscribe(subscriber);
        }
    }

    static <T> DefaultEventSubject<T> create() {
        final Action1<Throwable> empty = Actions.empty();
        return new DefaultEventSubject<T>(new OnSubscribeFunc<T>(), empty);
    }

    static <T> DefaultEventSubject<T> create(Action1<Throwable> onError) {
        return new DefaultEventSubject<T>(new OnSubscribeFunc<T>(), onError);
    }

    private DefaultEventSubject(OnSubscribeFunc<T> onSubscribeFunc, Action1<Throwable> onError) {
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
