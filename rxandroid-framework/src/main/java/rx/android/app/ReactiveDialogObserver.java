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
package rx.android.app;

import rx.Subscriber;
import rx.android.app.internal.SubscriberVault;

/**
 * A wrapper for the subscriber from the observable.
 * The wrapper takes care of wrapping values into a result object before passing them and removes itself from the vault upon completion or failure.
 *
 * @param <V> The type of data expected as return value from the dialog.
 */
public final class ReactiveDialogObserver<V> implements ReactiveDialogListener<V> {

    private final Subscriber<? super ReactiveDialogResult<V>> subscriber;

    private final SubscriberVault subscriberVault;

    private final long subscriberKey;

    public ReactiveDialogObserver(Subscriber<? super ReactiveDialogResult<V>> subscriber, SubscriberVault subscriberVault, long subscriberKey) {
        this.subscriber = subscriber;
        this.subscriberVault = subscriberVault;
        this.subscriberKey = subscriberKey;
    }

    @Override
    public void onNext(V value) {
        subscriber.onNext(ReactiveDialogResult.asSuccess(value));
    }

    @Override
    public void onCompleteWith(V value) {
        subscriber.onNext(ReactiveDialogResult.asSuccess(value));
        subscriber.onCompleted();
        subscriberVault.remove(subscriberKey);
    }

    @Override
    public void onCancel() {
        subscriber.onNext(ReactiveDialogResult.<V>asCanceled());
        subscriber.onCompleted();
        subscriberVault.remove(subscriberKey);
    }

    @Override
    public void onError(Throwable throwable) {
        subscriber.onError(throwable);
        subscriberVault.remove(subscriberKey);
    }

    @Override
    public void onCompleted() {
        subscriber.onCompleted();
        subscriberVault.remove(subscriberKey);
    }
}
