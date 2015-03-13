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
package rx.android.widget;

import android.view.View;
import android.widget.CompoundButton;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.AndroidSubscriptions;
import rx.android.internal.Assertions;
import rx.functions.Action0;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

class OnSubscribeCompoundButtonInput implements Observable.OnSubscribe<Boolean> {
    private final boolean emitInitialValue;
    private final CompoundButton button;

    public OnSubscribeCompoundButtonInput(final CompoundButton button, final boolean emitInitialValue) {
        this.emitInitialValue = emitInitialValue;
        this.button = button;
    }

    @Override
    public void call(final Subscriber<? super Boolean> observer) {
        Assertions.assertUiThread();
        final CompositeOnCheckedChangeListener composite = CachedListeners.getFromViewOrCreate(button);

        final CompoundButton.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton view, final boolean checked) {
                observer.onNext(button.isChecked());
            }
        };

        final Subscription subscription = AndroidSubscriptions.unsubscribeInUiThread(new Action0() {
            @Override
            public void call() {
                composite.removeOnCheckedChangeListener(listener);
            }
        });

        if (emitInitialValue) {
            observer.onNext(button.isChecked());
        }

        composite.addOnCheckedChangeListener(listener);
        observer.add(subscription);
    }

    static class CompositeOnCheckedChangeListener implements CompoundButton.OnCheckedChangeListener {
        private final List<CompoundButton.OnCheckedChangeListener> listeners = new ArrayList<CompoundButton.OnCheckedChangeListener>();

        public boolean addOnCheckedChangeListener(final CompoundButton.OnCheckedChangeListener listener) {
            return listeners.add(listener);
        }

        public boolean removeOnCheckedChangeListener(final CompoundButton.OnCheckedChangeListener listener) {
            return listeners.remove(listener);
        }

        @Override
        public void onCheckedChanged(final CompoundButton button, final boolean checked) {
            for (final CompoundButton.OnCheckedChangeListener listener : listeners) {
                listener.onCheckedChanged(button, checked);
            }
        }
    }

    static class CachedListeners {
        private static final Map<View, CompositeOnCheckedChangeListener> cachedListeners = new WeakHashMap<View, CompositeOnCheckedChangeListener>();

        public static CompositeOnCheckedChangeListener getFromViewOrCreate(final CompoundButton button) {
            final CompositeOnCheckedChangeListener cached = cachedListeners.get(button);

            if (cached != null) {
                return cached;
            }

            final CompositeOnCheckedChangeListener listener = new CompositeOnCheckedChangeListener();

            cachedListeners.put(button, listener);
            button.setOnCheckedChangeListener(listener);

            return listener;
        }
    }
}


