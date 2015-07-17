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
package rx.android.internal;

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

final class ChildSubscription implements Subscription {
    private static final AtomicIntegerFieldUpdater<ChildSubscription> UNSUBSCRIBED_UPDATER
            = AtomicIntegerFieldUpdater.newUpdater(ChildSubscription.class, "unsubscribed");

    private final Subscription actual;
    private final CompositeSubscription parent;
    private volatile int unsubscribed;

    ChildSubscription(Subscription actual, CompositeSubscription parent) {
        this.actual = actual;
        this.parent = parent;
    }

    @Override
    public void unsubscribe() {
        if (UNSUBSCRIBED_UPDATER.compareAndSet(this, 0, 1)) {
            actual.unsubscribe();
            parent.remove(this);
        }
    }

    @Override
    public boolean isUnsubscribed() {
        final boolean unsubscribed = actual.isUnsubscribed();
        if (unsubscribed && UNSUBSCRIBED_UPDATER.compareAndSet(this, 0, 1)) {
            parent.remove(this);
        }
        return unsubscribed;
    }
}
