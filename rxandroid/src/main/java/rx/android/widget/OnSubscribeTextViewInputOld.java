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

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.internal.Assertions;
import rx.android.AndroidSubscriptions;
import rx.functions.Action0;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.TextView;

/** @deprecated this class will be removed soon */
@Deprecated
class OnSubscribeTextViewInputOld implements Observable.OnSubscribe<OnTextChangeEvent> {
    private final boolean emitInitialValue;
    private final TextView input;

    public OnSubscribeTextViewInputOld(final TextView input, final boolean emitInitialValue) {
        this.input = input;
        this.emitInitialValue = emitInitialValue;
    }

    @Override
    public void call(final Subscriber<? super OnTextChangeEvent> observer) {
        Assertions.assertUiThread();
        final TextWatcher watcher = new OnSubscribeTextViewInput.SimpleTextWatcher() {
            @Override
            public void afterTextChanged(final Editable editable) {
                observer.onNext(OnTextChangeEvent.create(input));
            }
        };

        final Subscription subscription = AndroidSubscriptions.unsubscribeInUiThread(new Action0() {
            @Override
            public void call() {
                input.removeTextChangedListener(watcher);
            }
        });

        if (emitInitialValue) {
            observer.onNext(OnTextChangeEvent.create(input));
        }

        input.addTextChangedListener(watcher);
        observer.add(subscription);
    }
}

