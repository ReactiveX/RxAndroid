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

import android.widget.SearchView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import rx.Observable;
import rx.Subscriber;
import rx.android.AndroidSubscriptions;
import rx.android.internal.Assertions;
import rx.functions.Action0;

public final class SearchViewQueryText {

    public static Observable<String> submit(final SearchView searchView) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(final Subscriber<? super String> subscriber) {
                Assertions.assertUiThread();
                final CompositeOnQueryTextSubmitListener composite = CachedListeners.getFromViewOrCreate(searchView);
                final SearchView.OnQueryTextListener listener = new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        subscriber.onNext(query);
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        return false;
                    }
                };

                composite.addOnQueryTextListener(listener);
                subscriber.add(AndroidSubscriptions.unsubscribeInUiThread(new Action0() {
                    @Override
                    public void call() {
                        composite.removeOnQueryTextListener(listener);
                    }
                }));
            }
        });
    }

    public static Observable<String> change(final SearchView searchView) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(final Subscriber<? super String> subscriber) {
                Assertions.assertUiThread();
                final CompositeOnQueryTextSubmitListener composite = CachedListeners.getFromViewOrCreate(searchView);
                final SearchView.OnQueryTextListener listener = new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        subscriber.onNext(newText);
                        return false;
                    }
                };

                composite.addOnQueryTextListener(listener);
                subscriber.add(AndroidSubscriptions.unsubscribeInUiThread(new Action0() {
                    @Override
                    public void call() {
                        composite.removeOnQueryTextListener(listener);
                    }
                }));
            }
        });
    }



    private static class CompositeOnQueryTextSubmitListener implements SearchView.OnQueryTextListener {

        private final List<SearchView.OnQueryTextListener> listeners = new ArrayList<SearchView.OnQueryTextListener>();

        public boolean addOnQueryTextListener(final SearchView.OnQueryTextListener listener) {
            return listeners.add(listener);
        }

        public boolean removeOnQueryTextListener(final SearchView.OnQueryTextListener listener) {
            return listeners.remove(listener);
        }

        @Override
        public boolean onQueryTextSubmit(String query) {
            for (SearchView.OnQueryTextListener listener: listeners) {
                listener.onQueryTextSubmit(query);
            }
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            for (SearchView.OnQueryTextListener listener: listeners) {
                listener.onQueryTextChange(newText);
            }
            return false;
        }

    }


    private static class CachedListeners {
        private static final Map<SearchView, CompositeOnQueryTextSubmitListener> cachedListeners =
                new WeakHashMap<SearchView, CompositeOnQueryTextSubmitListener>();

        public static CompositeOnQueryTextSubmitListener getFromViewOrCreate(final SearchView view) {
            final CompositeOnQueryTextSubmitListener cached = cachedListeners.get(view);
            if (cached != null) {
                return cached;
            }

            final CompositeOnQueryTextSubmitListener listener = new CompositeOnQueryTextSubmitListener();

            cachedListeners.put(view, listener);
            view.setOnQueryTextListener(listener);

            return listener;
        }
    }

}
