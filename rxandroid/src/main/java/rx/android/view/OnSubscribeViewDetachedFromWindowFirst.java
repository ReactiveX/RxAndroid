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
package rx.android.view;

import android.view.View;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;

/**
 * This emits an event when the given #{@code View} is detached from the window for the first time.
 */
final class OnSubscribeViewDetachedFromWindowFirst implements Observable.OnSubscribe<View> {
    private final View view;

    public OnSubscribeViewDetachedFromWindowFirst(View view) {
        this.view = view;
    }

    @Override
    public void call(final Subscriber<? super View> subscriber) {
        final SubscriptionAdapter adapter = new SubscriptionAdapter(subscriber, view);
        subscriber.add(adapter);
        view.addOnAttachStateChangeListener(adapter);
    }

    // This could be split into a couple of anonymous classes.
    // We pack it into one for the sake of memory efficiency.
    private static class SubscriptionAdapter implements View.OnAttachStateChangeListener,
            Subscription {
        private Subscriber<? super View> subscriber;
        private View view;

        public SubscriptionAdapter(Subscriber<? super View> subscriber, View view) {
            this.subscriber = subscriber;
            this.view = view;
        }

        @Override
        public void onViewAttachedToWindow(View v) {
        }

        @Override
        public void onViewDetachedFromWindow(View v) {
            if (!isUnsubscribed()) {
                Subscriber<? super View> originalSubscriber = subscriber;
                unsubscribe();
                originalSubscriber.onNext(v);
                originalSubscriber.onCompleted();
            }
        }

        @Override
        public void unsubscribe() {
            if (!isUnsubscribed()) {
                view.removeOnAttachStateChangeListener(this);
                view = null;
                subscriber = null;
            }
        }

        @Override
        public boolean isUnsubscribed() {
            return view == null;
        }
    }
}
