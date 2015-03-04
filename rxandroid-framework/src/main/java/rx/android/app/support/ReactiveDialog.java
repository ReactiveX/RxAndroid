/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package rx.android.app.support;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.app.ReactiveDialogListener;
import rx.android.app.ReactiveDialogObserver;
import rx.android.app.ReactiveDialogResult;
import rx.android.app.internal.SubscriberVault;
import rx.functions.Func1;

/**
 * Wrapper for DialogFragment that allows to observe on the result of the user interaction.
 *
 * @param <T> The type of data expected as return value from the fragment, can be boolean for confirmation dialogs,
 *            or more complex for data input dialogs.
 */
public class ReactiveDialog<T> extends RxDialogFragment {

    private static final String REACTIVE_DIALOG_KEY = "REACTIVE_DIALOG_KEY";

    private static final SubscriberVault subscriberVault = new SubscriberVault();

    /**
     * Returns an observable for the dialog result.
     * The dialog is shown at subscription time.
     */
    public Observable<ReactiveDialogResult<T>> show(final FragmentManager manager) {
        return Observable.create(new Observable.OnSubscribe<ReactiveDialogResult<T>>() {
            @Override
            public void call(Subscriber<? super ReactiveDialogResult<T>> subscriber) {
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
                .filter(new Func1<ReactiveDialogResult<T>, Boolean>() {
                    @Override
                    public Boolean call(ReactiveDialogResult<T> tResult) {
                        return !tResult.isCanceled();
                    }
                })
                .map(new Func1<ReactiveDialogResult<T>, T>() {
                    @Override
                    public T call(ReactiveDialogResult<T> tResult) {
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
        Subscriber<ReactiveDialogResult<T>> subscriber = subscriberVault.get(getSubscriberKey());
        if (subscriber == null) {
            throw new IllegalStateException("No listener attached, you are probably trying to deliver a result after completion of the observable");
        }
        return new ReactiveDialogObserver<T>(subscriber, subscriberVault, getSubscriberKey());
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

}
