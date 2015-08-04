/*
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
package rx.android.lifecycle;

import rx.Observable;
import rx.Observer;
import rx.Scheduler;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.subscribers.ActionSubscriber;
import rx.android.subscribers.NullingSubscriber;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.observers.Subscribers;

public final class SubscriptionBuilder<T> {
    private final LifecycleSubscriptions subscriptions;
    private final Observable<T> observable;
    private Scheduler subscribeOn;
    private LifecycleEvent observeUntil;
    private Scheduler observeOn;

    SubscriptionBuilder(LifecycleSubscriptions subscriptions, Observable<T> observable) {
        this.subscriptions = subscriptions;
        this.observable = observable;
    }

    public SubscriptionBuilder<T> subscribeOn(Scheduler subscribeOn) {
        this.subscribeOn = subscribeOn;
        return this;
    }

    public SubscriptionBuilder<T> observeUntil(LifecycleEvent event) {
        this.observeUntil = event;
        return this;
    }

    public SubscriptionBuilder<T> observeOnMainThread() {
        return observeOn(AndroidSchedulers.mainThread());
    }

    public SubscriptionBuilder<T> observeOn(Scheduler observeOn) {
        this.observeOn = observeOn;
        return this;
    }

    public Subscription subscribe(Action1<? super T> onNext) {
        return subscribe(new ActionSubscriber<T>(onNext));
    }

    public Subscription subscribe(Action1<? super T> onNext, Action1<Throwable> onError) {
        return subscribe(new ActionSubscriber<T>(onNext, onError));
    }

    public Subscription subscribe(Action1<? super T> onNext, Action1<Throwable> onError,
            Action0 onCompleted) {
        return subscribe(new ActionSubscriber<T>(onNext, onError, onCompleted));
    }

    public Subscription subscribe(Observer<? super T> observer) {
        return subscribe(Subscribers.from(observer));
    }

    public Subscription subscribe(Subscriber<? super T> subscriber) {
        // ObserveUntil
        LifecycleEvent observeUntil = this.observeUntil;
        if (observeUntil == null) {
            observeUntil = subscriptions.observeUntil();
        }

        // SubscribeOn
        Scheduler subscribeOn = this.subscribeOn;
        Observable<T> observable = subscribeOn == null
                ? this.observable
                : this.observable.subscribeOn(subscribeOn);

        // ObserveOn
        Scheduler observeOn = this.observeOn;
        observable = observeOn == null
                ? observable
                : observable.observeOn(observeOn);

        // Actual subscribe
        return subscriptions.subscribe(observable,
                observeUntil,
                NullingSubscriber.create(subscriber));
    }
}
