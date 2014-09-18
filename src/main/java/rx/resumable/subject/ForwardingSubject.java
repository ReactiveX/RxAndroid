package rx.resumable.subject;

import rx.resumable.operators.ObserverOperator;

public class ForwardingSubject<T> extends rx.subjects.Subject<T, T> {

    private final ObserverOperator<T> subscribeFunc;

    public ForwardingSubject(ObserverOperator<T> subscribeFunc) {
        super(subscribeFunc);
        this.subscribeFunc = subscribeFunc;
    }

    @Override
    public void onCompleted() {
        subscribeFunc.onCompleted();
    }

    @Override
    public void onError(Throwable throwable) {
        subscribeFunc.onError(throwable);
    }

    @Override
    public void onNext(T t) {
        subscribeFunc.onNext(t);
    }
}
