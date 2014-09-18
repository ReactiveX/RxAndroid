package rx.resumable.operators;

import rx.Observable;
import rx.Observer;

public interface ObserverOperator<T> extends Observer<T>, Observable.OnSubscribe<T> {
}
