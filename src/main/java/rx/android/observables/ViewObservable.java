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
package rx.android.observables;

import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.TextView;

import rx.Observable;
import rx.android.events.OnCheckedChangeEvent;
import rx.android.events.OnClickEvent;
import rx.android.events.OnItemClickEvent;
import rx.android.events.OnTextChangeEvent;
import rx.android.operators.OperatorAdapterViewOnItemClick;
import rx.android.operators.OperatorCompoundButtonInput;
import rx.android.operators.OperatorTextViewInput;
import rx.android.operators.OperatorViewClick;

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

}
