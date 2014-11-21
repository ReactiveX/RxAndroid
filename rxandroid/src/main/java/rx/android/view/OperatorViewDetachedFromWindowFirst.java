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
 * An internal class that is used from #{@link ViewObservable#bindView}.
 * This emits an event when the given #{@code View} is detached from the window for the first time.
 */
public class OperatorViewDetachedFromWindowFirst implements Observable.OnSubscribe<View> {
    private final View view;

    public OperatorViewDetachedFromWindowFirst(View view) {
        this.view = view;
    }

    @Override
    public void call(final Subscriber<? super View> subscriber) {
        new ListenerSubscription(subscriber, view);
    }

    // This could be split into a couple of anonymous classes.
    // We pack it into one for the sake of memory efficiency.
    private static class ListenerSubscription implements View.OnAttachStateChangeListener, Subscription {
        private Subscriber<? super View> subscriber;
        private View view;

        public ListenerSubscription(Subscriber<? super View> subscriber, View view) {
            this.subscriber = subscriber;
            this.view = view;
            view.addOnAttachStateChangeListener(this);
            subscriber.add(this);
        }

        @Override
        public void onViewAttachedToWindow(View v) {
        }

        @Override
        public void onViewDetachedFromWindow(View v) {
            if (!isUnsubscribed()) {
                Subscriber<? super View> originalSubscriber = subscriber;
                clear();
                originalSubscriber.onNext(v);
                originalSubscriber.onCompleted();
            }
        }

        @Override
        public void unsubscribe() {
            if (!isUnsubscribed()) {
                clear();
            }
        }

        @Override
        public boolean isUnsubscribed() {
            return view == null;
        }

        private void clear() {
            view.removeOnAttachStateChangeListener(this);
            view = null;
            subscriber = null;
        }
    }
}
