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
import rx.functions.Func1;

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
    public Observable<Result<T>> show(final FragmentManager manager) {
        return Observable.create(new Observable.OnSubscribe<Result<T>>() {
            @Override
            public void call(Subscriber<? super Result<T>> subscriber) {
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
                show(manager, null);
            }
        });
    }

    /**
     * Returns an unwrapped version of the dialog observable.
     * Cancelled events are ignored to allow for simpler composition in the case of data input dialogs.
     */
    public Observable<T> showIgnoringCancelEvents(final FragmentManager manager) {
        return show(manager)
                .filter(new Func1<Result<T>, Boolean>() {
                    @Override
                    public Boolean call(Result<T> tResult) {
                        return !tResult.isCanceled();
                    }
                })
                .map(new Func1<Result<T>, T>() {
                    @Override
                    public T call(Result<T> tResult) {
                        return tResult.getValue();
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
        Subscriber<Result<T>> subscriber = subscriberVault.get(getSubscriberKey());
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
     * The wrapper takes care of wrapping values into a result object before passing them and removes itself from the vault upon completion or failure.
     */
    private class ReactiveDialogObserver implements ReactiveDialogListener<T> {

        private final Subscriber<? super Result<T>> subscriber;

        public ReactiveDialogObserver(Subscriber<? super Result<T>> subscriber) {
            this.subscriber = subscriber;
        }

        @Override
        public void onNext(T value) {
            subscriber.onNext(Result.asSuccess(value));
        }

        @Override
        public void onCompleteWith(T value) {
            subscriber.onNext(Result.asSuccess(value));
            subscriber.onCompleted();
            subscriberVault.remove(getSubscriberKey());
        }

        @Override
        public void onCancel() {
            subscriber.onNext(Result.<T>asCanceled());
            subscriber.onCompleted();
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

    public final static class Result<V> {

        private final V value;
        private final boolean canceled;

        static <V> Result<V> asSuccess(V value) {
            return new Result<V>(value, false);
        }

        static <V> Result<V> asCanceled() {
            return new Result<V>(null, true);
        }

        private Result(V value, boolean canceled) {
            this.value = value;
            this.canceled = canceled;
        }

        public V getValue() {
            return value;
        }

        public boolean isCanceled() {
            return canceled;
        }
    }
}
