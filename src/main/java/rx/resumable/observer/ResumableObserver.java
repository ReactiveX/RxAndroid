package rx.resumable.observer;

import rx.Observer;

public interface ResumableObserver<T> extends Observer<T> {

    int getId();
}
