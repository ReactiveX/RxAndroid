package rx.android.observables;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;

import java.util.UUID;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.exception.CancelledException;
import rx.resumable.SubscriberVault;

public class ReactiveDialog<T> extends DialogFragment {

    private static final String REACTIVE_DIALOG_KEY = "REACTIVE_DIALOG_KEY";

    private static final SubscriberVault subscriberVault = new SubscriberVault();

    protected interface ReactiveDialogListener<T> extends Observer<T> {
        void onCompleteWith(T value);

        void onCancel();
    }

    public Observable<T> show(final FragmentManager manager) {
        return Observable.create(new Observable.OnSubscribe<T>() {
            @Override
            public void call(rx.Subscriber<? super T> subscriber) {
                UUID key = subscriberVault.store(subscriber);
                storeSubscriberKey(key);
                show(manager, getClass().getSimpleName());
            }
        });
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        getListener().onCancel();
    }

    protected ReactiveDialogListener<T> getListener() {
        Subscriber<Object> subscriber = subscriberVault.get(getSubscriberKey());
        if (subscriber == null) {
            throw new IllegalStateException("No listener attached, you are probably trying to deliver a result after completion of the observable");
        }
        return new ReactiveDialogObserver<T>(subscriber);
    }

    private void storeSubscriberKey(UUID key) {
        if (getArguments() == null) {
            setArguments(new Bundle());
        }
        getArguments().putSerializable(REACTIVE_DIALOG_KEY, key);
    }

    private UUID getSubscriberKey() {
        return (UUID) getArguments().getSerializable(REACTIVE_DIALOG_KEY);
    }

    private class ReactiveDialogObserver<T> implements ReactiveDialogListener<T> {

        private final Subscriber<? super T> subscriber;

        public ReactiveDialogObserver(Subscriber<? super T> subscriber) {
            this.subscriber = subscriber;
        }

        @Override
        public void onNext(T value) {
            subscriber.onNext(value);
        }

        @Override
        public void onCompleteWith(T value) {
            subscriber.onNext(value);
            subscriber.onCompleted();
            subscriberVault.remove(getSubscriberKey());
        }

        @Override
        public void onCancel() {
            subscriber.onError(new CancelledException());
            subscriberVault.remove(getSubscriberKey());
        }

        @Override
        public void onError(Throwable throwable) {
            subscriber.onError(throwable);
            subscriberVault.remove(getSubscriberKey());
        }

        @Override
        public void onCompleted() {
            subscriber.onCompleted();
            subscriberVault.remove(getSubscriberKey());
        }
    }

}
