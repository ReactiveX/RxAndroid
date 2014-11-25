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
import android.widget.AbsListView;
import android.widget.AdapterView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.internal.Assertions;
import rx.android.AndroidSubscriptions;
import rx.functions.Action0;

class OnSubscribeAdapterViewOnItemClick implements Observable.OnSubscribe<OnItemClickEvent> {

    private final AdapterView<?> adapterView;

    public OnSubscribeAdapterViewOnItemClick(final AdapterView<?> adapterView) {
        this.adapterView = adapterView;
    }

    @Override
    public void call(final Subscriber<? super OnItemClickEvent> observer) {
        Assertions.assertUiThread();
        final CompositeOnClickListener composite = CachedListeners.getFromViewOrCreate(adapterView);

        final AbsListView.OnItemClickListener listener = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                observer.onNext(OnItemClickEvent.create(parent, view, position, id));
            }
        };

        final Subscription subscription = AndroidSubscriptions.unsubscribeInUiThread(new Action0() {
            @Override
            public void call() {
                composite.removeOnClickListener(listener);
            }
        });

        composite.addOnClickListener(listener);
        observer.add(subscription);
    }

    private static class CompositeOnClickListener implements AbsListView.OnItemClickListener {
        private final List<AbsListView.OnItemClickListener> listeners = new ArrayList<AbsListView.OnItemClickListener>();

        public boolean addOnClickListener(final AbsListView.OnItemClickListener listener) {
            return listeners.add(listener);
        }

        public boolean removeOnClickListener(final AbsListView.OnItemClickListener listener) {
            return listeners.remove(listener);
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            for (AdapterView.OnItemClickListener listener : listeners) {
                listener.onItemClick(parent, view, position, id);
            }
        }
    }

    private static class CachedListeners {
        private static final Map<AdapterView<?>, CompositeOnClickListener> sCachedListeners = new WeakHashMap<AdapterView<?>, CompositeOnClickListener>();

        public static CompositeOnClickListener getFromViewOrCreate(final AdapterView<?> view) {
            final CompositeOnClickListener cached = sCachedListeners.get(view);

            if (cached != null) {
                return cached;
            }

            final CompositeOnClickListener listener = new CompositeOnClickListener();

            sCachedListeners.put(view, listener);
            view.setOnItemClickListener(listener);

            return listener;
        }
    }
}
