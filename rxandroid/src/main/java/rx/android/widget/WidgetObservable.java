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
import android.widget.CompoundButton;
import android.widget.TextView;

import rx.Observable;
import rx.android.view.OnCheckedChangeEvent;

public final class WidgetObservable {
    private WidgetObservable() {
        throw new AssertionError("No instances");
    }

    /** @deprecated this method will soon be replaced by renamed {@link #forText} */
    @Deprecated
    public static Observable<OnTextChangeEvent> text(final TextView input) {
        return text(input, false);
    }

    /** @deprecated this method will soon be replaced by renamed {@link #forText} */
    @Deprecated
    public static Observable<OnTextChangeEvent> text(final TextView input, final boolean emitInitialValue) {
        return Observable.create(new OnSubscribeTextViewInputOld(input, emitInitialValue));
    }

    public static <T extends TextView> Observable<T> forText(final T textView) {
        return forText(textView, false);
    }

    public static <T extends TextView> Observable<T> forText(final T textView, final boolean emitInitialValue) {
        return Observable.create(new OnSubscribeTextViewInput<T>(textView, emitInitialValue));
    }

    /** @deprecated this method will soon be removed, use {@link #toggle} instead */
    @Deprecated
    public static Observable<OnCheckedChangeEvent> input(final CompoundButton button) {
        return input(button, false);
    }

    /** @deprecated this method will soon be removed, use {@link #toggle} instead */
    @Deprecated
    public static Observable<OnCheckedChangeEvent> input(final CompoundButton button, final boolean emitInitialValue) {
        return Observable.create(new OnSubscribeCompoundButtonInputOld(button, emitInitialValue));
    }

    public static Observable<Boolean> toggle(final CompoundButton view) {
        return toggle(view, false);
    }

    public static Observable<Boolean> toggle(final CompoundButton view, final boolean emitInitialValue) {
        return Observable.create(new OnSubscribeCompoundButtonInput(view, emitInitialValue));
    }

    public static Observable<OnItemClickEvent> itemClicks(final AdapterView<?> adapterView) {
        return Observable.create(new OnSubscribeAdapterViewOnItemClick(adapterView));
    }

    /**
     * Returns an observable that emits all the scroll events from the provided ListView.
     * Note that this will replace any listeners previously set through
     * {@link android.widget.AbsListView#setOnScrollListener(android.widget.AbsListView.OnScrollListener)} unless those
     * were set by this method or {@link OnSubscribeListViewScroll}.
     */
    public static Observable<OnListViewScrollEvent> listScrollEvents(final AbsListView listView) {
        return Observable.create(new OnSubscribeListViewScroll(listView));
    }
}
