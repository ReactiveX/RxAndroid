package rx.resumable;

import rx.resumable.observer.ResumableObserver;

public interface ObserverFactory {
    ResumableObserver createObserver(int code);
}
