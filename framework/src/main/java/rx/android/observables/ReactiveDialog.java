/**
 * Copyright 2014 Novoda, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package rx.android.observables;

import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.os.Bundle;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.exception.CancelledException;

/**
 * Wrapper for DialogFragment that allows to observe on the result of the user interaction.
 *
 * @param <T> The type of data expected as return value from the fragment, can be boolean for confirmation dialogs,
 *            or more complex for data input dialogs.
 */
public class ReactiveDialog<T> extends DialogFragment {

    private static final String REACTIVE_DIALOG_KEY = "REACTIVE_DIALOG_KEY";

    private static final SubscriberVault subscriberVault = new SubscriberVault();

    public interface ReactiveDialogListener<V> extends Observer<V> {
        void onCompleteWith(V value);

        void onCancel();
    }

    /**
     * Returns an observable for the dialog result.
     * The dialog is shown at subscription time.
     */
    public Observable<T> show(final FragmentManager manager) {
        return Observable.create(new Observable.OnSubscribe<T>() {
            @Override
            public void call(rx.Subscriber<? super T> subscriber) {
                final long key = subscriberVault.store(subscriber);
                storeSubscriberKey(key);
                subscriber.add(new Subscription() {
                    @Override
                    public void unsubscribe() {
                        subscriberVault.remove(key);
                    }

                    @Override
                    public boolean isUnsubscribed() {
                        return !subscriberVault.containsKey(key);
                    }
                });
                show(manager, getClass().getSimpleName());
            }
        });
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        getListener().onCancel();
    }

    /**
     * Get the wrapped subscriber for the observable.
     */
    protected ReactiveDialogListener<T> getListener() {
        Subscriber<T> subscriber = subscriberVault.get(getSubscriberKey());
        if (subscriber == null) {
            throw new IllegalStateException("No listener attached, you are probably trying to deliver a result after completion of the observable");
        }
        return new ReactiveDialogObserver(subscriber);
    }

    private void storeSubscriberKey(long key) {
        if (getArguments() == null) {
            setArguments(new Bundle());
        }
        getArguments().putLong(REACTIVE_DIALOG_KEY, key);
    }

    private long getSubscriberKey() {
        return getArguments().getLong(REACTIVE_DIALOG_KEY);
    }

    /**
     * A wrapper for the subscriber from the observable.
     * The wrapper add specialised failures such as CancelledException and removes itself from the vault upon completion or failure.
     */
    private class ReactiveDialogObserver implements ReactiveDialogListener<T> {

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
