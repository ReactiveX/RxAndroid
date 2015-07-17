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

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

public final class Util {
    private Util() { throw new AssertionError("no instances"); }

    public static Subscription attachToComposite(Subscription subscription,
            CompositeSubscription cs) {
        Subscription sub = new ChildSubscription(subscription, cs);
        cs.add(sub);
        return sub;
    }

    public static <T> Subscription subscribeWithComposite(Observable<T> observable,
            Subscriber<? super T> subscriber,
            CompositeSubscription cs) {
        final Subscription actual = observable.subscribe(subscriber);
        final Subscription sub = attachToComposite(actual, cs);
        // NOTE: We have to do this in order to remove the subscription from the list if the
        // observable completes before the composite is unsubscribed
        subscriber.add(sub);
        return sub;
    }
}
