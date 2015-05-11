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

final class OnSubscribeViewFocusChange implements Observable.OnSubscribe<OnFocusChangeEvent> {
    private final boolean emitInitialValue;
    private final View view;

    public OnSubscribeViewFocusChange(final View view, final boolean emitInitialValue) {
        this.emitInitialValue = emitInitialValue;
        this.view = view;
    }

    @Override
    public void call(final Subscriber<? super OnFocusChangeEvent> observer) {
        Assertions.assertUiThread();
        final CompositeOnFocusChangeListener composite = CachedListeners.getFromViewOrCreate(view);

        final View.OnFocusChangeListener listener = new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                observer.onNext(OnFocusChangeEvent.create(view, hasFocus));
            }
        };

        final Subscription subscription = AndroidSubscriptions.unsubscribeInUiThread(new Action0() {
            @Override
            public void call() {
                composite.removeOnFocusChangeListener(listener);
            }
        });

        if (emitInitialValue) {
            observer.onNext(OnFocusChangeEvent.create(view));
        }

        composite.addOnFocusChangeListener(listener);
        observer.add(subscription);
    }

    private static class CompositeOnFocusChangeListener implements View.OnFocusChangeListener {
        private final List<View.OnFocusChangeListener> listeners = new ArrayList<View.OnFocusChangeListener>();

        public boolean addOnFocusChangeListener(final View.OnFocusChangeListener listener) {
            return listeners.add(listener);
        }

        public boolean removeOnFocusChangeListener(final View.OnFocusChangeListener listener) {
            return listeners.remove(listener);
        }

        @Override
        public void onFocusChange(final View view, final boolean hasFocus) {
            for (View.OnFocusChangeListener listener : listeners) {
                listener.onFocusChange(view, hasFocus);
            }
        }
    }

    private static class CachedListeners {
        private static final Map<View, CompositeOnFocusChangeListener> sCachedListeners = new WeakHashMap<View, CompositeOnFocusChangeListener>();

        public static CompositeOnFocusChangeListener getFromViewOrCreate(final View view) {
            final CompositeOnFocusChangeListener cached = sCachedListeners.get(view);

            if (cached != null) {
                return cached;
            }

            final CompositeOnFocusChangeListener listener = new CompositeOnFocusChangeListener();

            sCachedListeners.put(view, listener);
            view.setOnFocusChangeListener(listener);

            return listener;
        }
    }
}
