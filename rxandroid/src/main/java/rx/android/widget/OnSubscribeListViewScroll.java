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

import android.widget.AbsListView;
import android.widget.AdapterView;
import rx.Observable;
import rx.Subscriber;
import rx.android.internal.Assertions;
import rx.android.AndroidSubscriptions;
import rx.functions.Action0;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

class OnSubscribeListViewScroll implements Observable.OnSubscribe<OnListViewScrollEvent> {

    private final AbsListView listView;

    public OnSubscribeListViewScroll(AbsListView listView) {
        this.listView = listView;
    }

    @Override
    public void call(final Subscriber<? super OnListViewScrollEvent> observer) {
        Assertions.assertUiThread();

        final CompositeOnScrollListener composite = CachedListeners.getFromViewOrCreate(listView);
        final AbsListView.OnScrollListener listener = new AbsListView.OnScrollListener() {
            int currentScrollState = SCROLL_STATE_IDLE;

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                this.currentScrollState = scrollState;
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                OnListViewScrollEvent event = OnListViewScrollEvent.create(view, this.currentScrollState, firstVisibleItem,
                    visibleItemCount, totalItemCount);
                observer.onNext(event);
            }
        };

        composite.addOnScrollListener(listener);
        observer.add(AndroidSubscriptions.unsubscribeInUiThread(new Action0() {
            @Override
            public void call() {
                composite.removeOnScrollListener(listener);
            }
        }));
    }

    private static class CompositeOnScrollListener implements AbsListView.OnScrollListener {

        private final List<AbsListView.OnScrollListener> listeners = new ArrayList<AbsListView.OnScrollListener>();

        public boolean addOnScrollListener(final AbsListView.OnScrollListener listener) {
            return listeners.add(listener);
        }

        public boolean removeOnScrollListener(final AbsListView.OnScrollListener listener) {
            return listeners.remove(listener);
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            for (AbsListView.OnScrollListener listener : listeners) {
                listener.onScrollStateChanged(view, scrollState);
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            for (AbsListView.OnScrollListener listener : listeners) {
                listener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
            }
        }
    }

    private static class CachedListeners {

        private static final Map<AdapterView<?>, CompositeOnScrollListener> sCachedListeners =
            new WeakHashMap<AdapterView<?>, CompositeOnScrollListener>();

        public static CompositeOnScrollListener getFromViewOrCreate(final AbsListView view) {
            final CompositeOnScrollListener cached = sCachedListeners.get(view);
            if (cached != null) {
                return cached;
            }

            final CompositeOnScrollListener listener = new CompositeOnScrollListener();

            sCachedListeners.put(view, listener);
            view.setOnScrollListener(listener);

            return listener;
        }
    }
}
