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
import rx.android.internal.Assertions;

import static rx.android.schedulers.AndroidSchedulers.mainThread;

public final class ViewObservable {

    private ViewObservable() {
        throw new AssertionError("No instances");
    }

    public static Observable<OnClickEvent> clicks(final View view) {
        return clicks(view, false);
    }

    public static Observable<OnClickEvent> clicks(final View view, final boolean emitInitialValue) {
        return Observable.create(new OnSubscribeViewClick(view, emitInitialValue));
    }

    /**
     * Binds the given source sequence to the view.
     * <p>
     * This helper will schedule the given sequence to be observed on the main UI thread and ensure
     * that no notifications will be forwarded to the view in case it gets detached from its the window.
     * <p>
     * Unlike {@link rx.android.app.AppObservable#bindActivity} or {@link rx.android.app.AppObservable#bindFragment}, you don't have to unsubscribe the returned {@code Observable}
     * on the detachment. {@link #bindView} does it automatically.
     * That means that the subscriber doesn't see further sequence even if the view is recycled and
     * attached again.
     *
     * @param view the view to bind the source sequence to
     * @param source the source sequence
     */
    public static <T> Observable<T> bindView(View view, Observable<T> source) {
        if (view == null || source == null)
            throw new IllegalArgumentException("View and Observable must be given");
        Assertions.assertUiThread();
        return source.takeUntil(Observable.create(new OnSubscribeViewDetachedFromWindowFirst(view))).observeOn(mainThread());
    }
}
