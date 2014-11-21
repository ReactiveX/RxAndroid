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
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.TextView;

import rx.Observable;
import rx.android.widget.OnItemClickEvent;
import rx.android.widget.OnListViewScrollEvent;
import rx.android.widget.OperatorAdapterViewOnItemClick;
import rx.android.widget.OperatorCompoundButtonInput;
import rx.android.widget.OnSubscribeListViewScroll;
import rx.android.widget.OperatorTextViewInput;
import rx.android.widget.OnTextChangeEvent;

public class ViewObservable {

    public static Observable<OnClickEvent> clicks(final View view) {
        return clicks(view, false);
    }

    public static Observable<OnClickEvent> clicks(final View view, final boolean emitInitialValue) {
        return Observable.create(new OperatorViewClick(view, emitInitialValue));
    }

    public static Observable<OnTextChangeEvent> text(final TextView input) {
        return text(input, false);
    }

    public static Observable<OnTextChangeEvent> text(final TextView input, final boolean emitInitialValue) {
        return Observable.create(new OperatorTextViewInput(input, emitInitialValue));
    }

    public static Observable<OnCheckedChangeEvent> input(final CompoundButton button) {
        return input(button, false);
    }

    public static Observable<OnCheckedChangeEvent> input(final CompoundButton button, final boolean emitInitialValue) {
        return Observable.create(new OperatorCompoundButtonInput(button, emitInitialValue));
    }

    public static Observable<OnItemClickEvent> itemClicks(final AdapterView<?> adapterView) {
        return Observable.create(new OperatorAdapterViewOnItemClick(adapterView));
    }

    /**
     * Returns an observable that emits all the scroll events from the provided ListView.
     * Note that this will replace any listeners previously set through
     * {@link android.widget.AbsListView#setOnScrollListener(android.widget.AbsListView.OnScrollListener)} unless those
     * were set by this method or {@link rx.android.widget.OnSubscribeListViewScroll}.
     */
    public static Observable<OnListViewScrollEvent> listScrollEvents(final AbsListView listView) {
        return Observable.create(new OnSubscribeListViewScroll(listView));
    }
}
