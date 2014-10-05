/**
 * Copyright 2014 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package rx.android.observables;

import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.TextView;

import rx.Observable;
import rx.android.events.OnItemClickEvent;
import rx.operators.OperatorAdapterViewOnItemClick;
import rx.operators.OperatorCompoundButtonInput;
import rx.operators.OperatorTextViewInput;
import rx.operators.OperatorViewClick;

public class ViewObservable {

    public static <T extends View> Observable<T> clicks(final T view, final boolean emitInitialValue) {
        return Observable.create(new OperatorViewClick<T>(view, emitInitialValue));
    }

    public static <T extends TextView> Observable<T> text(final T input) {
        return text(input, false);
    }

    public static <T extends TextView> Observable<T> text(final T input, final boolean emitInitialValue) {
        return Observable.create(new OperatorTextViewInput<T>(input, emitInitialValue));
    }

    public static Observable<Boolean> input(final CompoundButton button, final boolean emitInitialValue) {
        return Observable.create(new OperatorCompoundButtonInput(button, emitInitialValue));
    }

    public static <T extends AdapterView<? extends Adapter>> Observable<OnItemClickEvent> itemClicks(final T list) {
        return Observable.create(new OperatorAdapterViewOnItemClick<T>(list));
    }

}

