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

import android.util.Log;

import java.util.concurrent.atomic.AtomicReference;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.internal.Util;
import rx.android.subscribers.NullingSubscriber;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;
import rx.subscriptions.Subscriptions;

public final class LifecycleSubscriptions {
    private static final String TAG = LifecycleSubscriptions.class.getSimpleName();

    private final AtomicReference<LifecycleProducer> producer = new AtomicReference<LifecycleProducer>();
    private final LifecycleObservationCalculator observationCalculator;
    private final Thread thread = Thread.currentThread();
    // We never recycle the current node, so create one initially instead of from the pool here
    private SubscriptionNode current = new SubscriptionNode();

    private LifecycleSubscriptions(LifecycleObservationCalculator observationCalculator) {
        this.observationCalculator = observationCalculator;
    }

    public static LifecycleSubscriptions observeActivity() {
        return new LifecycleSubscriptions(LifecycleObservationCalculator.ACTIVITY);
    }

    public static LifecycleSubscriptions observeFragment() {
        return new LifecycleSubscriptions(LifecycleObservationCalculator.FRAGMENT);
    }

    public static LifecycleSubscriptions observeUntilStop() {
        return new LifecycleSubscriptions(LifecycleObservationCalculator.UNTIL_STOP);
    }

    public static LifecycleSubscriptions observe(LifecycleObservationCalculator bindingCalculator) {
        return new LifecycleSubscriptions(bindingCalculator);
    }

    public boolean setProducer(LifecycleProducer producer) {
        if (this.producer.compareAndSet(null, producer)) {
            producer.asObservable().subscribe(new LifecycleObserver());
            return true;
        }
        return false;
    }

    public Observable<LifecycleEvent> lifecycleObservable() {
        LifecycleProducer producer = this.producer.get();
        if (producer == null) {
            throw new IllegalStateException(
                    "must set producer before calling lifecycleObservable()");
        }
        return producer.asObservable();
    }

    public <T> Subscription subscribe(Observable<T> observable, Action1<? super T> onNext) {
        return with(observable)
                .subscribe(onNext);
    }

    public <T> Subscription subscribe(Observable<T> observable,
            Action1<? super T> onNext,
            Action1<Throwable> onError) {
        return with(observable)
                .subscribe(onNext, onError);
    }

    public <T> Subscription subscribe(Observable<T> observable,
            Action1<? super T> onNext,
            Action1<Throwable> onError,
            Action0 onCompleted) {
        return with(observable)
                .subscribe(onNext, onError, onCompleted);
    }

    public <T> Subscription subscribe(Observable<T> observable,
            Observer<? super T> observer) {
        return with(observable)
                .subscribe(observer);
    }

    public <T> Subscription subscribe(Observable<T> observable,
            Subscriber<? super T> subscriber) {
        return with(observable)
                .subscribe(subscriber);
    }

    public <T> SubscriptionBuilder<T> with(Observable<T> observable) {
        return new SubscriptionBuilder<T>(this, observable);
    }

    public LifecycleEvent observeUntil() {
        LifecycleEvent current = this.current.event;
        return observationCalculator.observeUntil(current);
    }

    <T> Subscription subscribe(Observable<T> observable,
            LifecycleEvent observeUntil,
            NullingSubscriber<? super T> subscriber) {
        assertThread();
        // First verify that event has not already passed
        if (!canSubscribe(observeUntil)) {
            Log.w(TAG, "not subscribing to observable. Binding LifecycleEvent has already passed");
            subscriber.unsubscribe();
            return Subscriptions.unsubscribed();
        }
        // Subscribe and add to composite
        final CompositeSubscription cs = current.insertAndPopulate(observeUntil).cs;
        return Util.subscribeWithComposite(observable, subscriber, cs);
    }

    private void assertThread() {
        if (thread != Thread.currentThread()) {
            throw new AssertionError("LifecycleSubscriptions must only be used on one thread");
        }
    }

    private boolean canSubscribe(LifecycleEvent event) {
        return current.ordinal() < ordinal(event);
    }

    private static int ordinal(LifecycleEvent event) {
        if (event != null) {
            return event.ordinal();
        }
        return -1;
    }

    private final class LifecycleObserver implements Observer<LifecycleEvent> {
        @Override public void onCompleted() {
            // should never be called
            assertThread();
            current = current.drain(LifecycleEvent.DETACH);
        }

        @Override public void onError(Throwable throwable) {
            // should never be called
            assertThread();
            current = current.drain(LifecycleEvent.DETACH);
        }

        @Override public void onNext(LifecycleEvent lifecycleEvent) {
            assertThread();
            current = current.drain(lifecycleEvent);
        }
    }

    private static final class SubscriptionNode implements Subscription {
        private static final Object sPoolSync = new Object();
        private static SubscriptionNode sPool;
        private static int sPoolSize = 0;
        private static final int MAX_POOL_SIZE = 10;

        SubscriptionNode prev;
        SubscriptionNode next;
        LifecycleEvent event;
        CompositeSubscription cs;

        int ordinal() {
            return LifecycleSubscriptions.ordinal(event);
        }

        SubscriptionNode insertAndPopulate(LifecycleEvent event) {
            SubscriptionNode node = insert(event);
            if (node.cs == null) {
                node.cs = new CompositeSubscription();
            }
            return node;
        }

        SubscriptionNode insert(LifecycleEvent event) {
            final int ordinal = LifecycleSubscriptions.ordinal(event);
            if (ordinal == ordinal()) {
                return this;
            } else if (ordinal < ordinal()) {
                SubscriptionNode current = this;
                while (current.prev != null) {
                    if (ordinal == current.prev.ordinal()) {
                        return current.prev;
                    } else if (ordinal > current.prev.ordinal()) {
                        SubscriptionNode node = obtain();
                        node.event = event;
                        node.prev = current.prev;
                        node.next = current;
                        current.prev.next = node;
                        current.prev = node;
                        return node;
                    }
                    current = current.prev;
                }
                SubscriptionNode node = obtain();
                node.event = event;
                node.next = current;
                current.prev = node;
                return node;
            } else {
                SubscriptionNode current = this;
                while (current.next != null) {
                    if (ordinal == current.next.ordinal()) {
                        return current.next;
                    } else if (ordinal < current.next.ordinal()) {
                        SubscriptionNode node = obtain();
                        node.event = event;
                        node.next = current.next;
                        node.prev = current;
                        current.next.prev = node;
                        current.next = node;
                        return node;
                    }
                    current = current.next;
                }
                SubscriptionNode node = obtain();
                node.event = event;
                node.prev = current;
                current.next = node;
                return node;
            }
        }

        SubscriptionNode drain(LifecycleEvent event) {
            SubscriptionNode current = insert(event);
            SubscriptionNode head = findHead(current);
            while (head != current) {
                SubscriptionNode next = head.next;
                head.recycle();
                head = next;
            }
            current.prev = null;
            current.unsubscribe();
            return current;
        }

        @Override public boolean isUnsubscribed() {
            return cs == null || cs.isUnsubscribed();
        }

        @Override public void unsubscribe() {
            if (cs != null) {
                if (Log.isLoggable(TAG, Log.VERBOSE)) { Log.v(TAG, "unsubscribing " + this); }
                cs.unsubscribe();
                cs = null;
            }
        }

        @Override public String toString() {
            return "SubscriptionNode{" +
                    "event=" + event +
                    '}';
        }

        private void recycle() {
            next = null;
            prev = null;
            event = null;
            unsubscribe();

            synchronized (sPoolSync) {
                if (sPoolSize < MAX_POOL_SIZE) {
                    next = sPool;
                    sPool = this;
                    sPoolSize++;
                }
            }
        }

        private static SubscriptionNode findHead(SubscriptionNode node) {
            SubscriptionNode head = node;
            while (head.prev != null) {
                head = head.prev;
            }
            return head;
        }

        private static SubscriptionNode obtain() {
            synchronized (sPoolSync) {
                if (sPool != null) {
                    SubscriptionNode n = sPool;
                    sPool = n.next;
                    n.next = null;
                    sPoolSize--;
                    return n;
                }
            }
            return new SubscriptionNode();
        }
    }
}
