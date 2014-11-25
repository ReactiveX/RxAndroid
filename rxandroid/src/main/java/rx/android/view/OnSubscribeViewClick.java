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
import rx.android.internal.Assertions;
import rx.android.AndroidSubscriptions;
import rx.functions.Action0;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

final class OnSubscribeViewClick implements Observable.OnSubscribe<OnClickEvent> {
    private final boolean emitInitialValue;
    private final View view;

    public OnSubscribeViewClick(final View view, final boolean emitInitialValue) {
        this.emitInitialValue = emitInitialValue;
        this.view = view;
    }

    @Override
    public void call(final Subscriber<? super OnClickEvent> observer) {
        Assertions.assertUiThread();
        final CompositeOnClickListener composite = CachedListeners.getFromViewOrCreate(view);

        final View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(final View clicked) {
                observer.onNext(OnClickEvent.create(view));
            }
        };

        final Subscription subscription = AndroidSubscriptions.unsubscribeInUiThread(new Action0() {
            @Override
            public void call() {
                composite.removeOnClickListener(listener);
            }
        });

        if (emitInitialValue) {
            observer.onNext(OnClickEvent.create(view));
        }

        composite.addOnClickListener(listener);
        observer.add(subscription);
    }

    private static class CompositeOnClickListener implements View.OnClickListener {
        private final List<View.OnClickListener> listeners = new ArrayList<View.OnClickListener>();

        public boolean addOnClickListener(final View.OnClickListener listener) {
            return listeners.add(listener);
        }

        public boolean removeOnClickListener(final View.OnClickListener listener) {
            return listeners.remove(listener);
        }

        @Override
        public void onClick(final View view) {
            for (final View.OnClickListener listener : listeners) {
                listener.onClick(view);
            }
        }
    }

    private static class CachedListeners {
        private static final Map<View, CompositeOnClickListener> sCachedListeners = new WeakHashMap<View, CompositeOnClickListener>();

        public static CompositeOnClickListener getFromViewOrCreate(final View view) {
            final CompositeOnClickListener cached = sCachedListeners.get(view);

            if (cached != null) {
                return cached;
            }

            final CompositeOnClickListener listener = new CompositeOnClickListener();

            sCachedListeners.put(view, listener);
            view.setOnClickListener(listener);

            return listener;
        }
    }
}
