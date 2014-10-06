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

import rx.Observable;
import rx.android.events.OnCheckedChangeEvent;
import rx.android.events.OnClickEvent;
import rx.android.events.OnTextChangeEvent;
import rx.operators.OperatorCompoundButtonInput;
import rx.operators.OperatorTextViewInput;
import rx.operators.OperatorViewClick;

import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

public class ViewObservable {

    public static <T extends View> Observable<OnClickEvent> clicks(final T view, final boolean emitInitialValue) {
        return Observable.create(new OperatorViewClick(view, emitInitialValue));
    }

    public static <T extends TextView> Observable<OnTextChangeEvent> text(final T input) {
        return text(input, false);
    }

    public static <T extends TextView> Observable<OnTextChangeEvent> text(final T input, final boolean emitInitialValue) {
        return Observable.create(new OperatorTextViewInput(input, emitInitialValue));
    }

    public static <T extends CompoundButton> Observable<OnCheckedChangeEvent> input(final T button, final boolean emitInitialValue) {
        return Observable.create(new OperatorCompoundButtonInput(button, emitInitialValue));
    }

}
