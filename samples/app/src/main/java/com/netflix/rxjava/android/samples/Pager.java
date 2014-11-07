package com.netflix.rxjava.android.samples;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Func1;
import rx.subjects.PublishSubject;
import rx.subscriptions.Subscriptions;

public final class Pager<T> {

    private static final Observable EMPTY_PAGE = Observable.empty();

    private final PublishSubject<Observable<T>> pages = PublishSubject.create();
    private final NextPageFunc<T> nextPageFunc;
    private Observable<T> nextPage = emptyPage();
    private Subscription subscription = Subscriptions.empty();

    public static <T> Observable<T> emptyPage() {
        return EMPTY_PAGE;
    }

    public static <T> Pager<T> create(NextPageFunc<T> nextPageFunc) {
        return new Pager<T>(nextPageFunc);
    }

    private Pager(NextPageFunc<T> nextPageFunc) {
        this.nextPageFunc = nextPageFunc;
    }

    public Observable<T> page(final Observable<T> source) {
        return Observable.create(new Observable.OnSubscribe<T>() {
            @Override public void call(final Subscriber<? super T> subscriber) {
                subscription = Observable.switchOnNext(pages).subscribe(new Subscriber<T>() {
                    @Override public void onCompleted() {
                        subscriber.onCompleted();
                    }

                    @Override public void onError(Throwable e) {
                        subscriber.onError(e);
                    }

                    @Override public void onNext(T result) {
                        nextPage = nextPageFunc.call(result);
                        subscriber.onNext(result);
                    }
                });
                pages.onNext(source);
            }
        });
    }

    public boolean hasNext() {
        return nextPage != emptyPage();
    }

    public void next() {
        if (!subscription.isUnsubscribed()) {
            if (hasNext()) {
                pages.onNext(nextPage);
            } else {
                pages.onCompleted();
            }
        }
    }

    interface NextPageFunc<CollT> extends Func1<CollT, Observable<CollT>> {
    }
}
