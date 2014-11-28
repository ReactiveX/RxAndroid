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

import android.widget.CompoundButton;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.view.OnCheckedChangeEvent;
import rx.android.internal.Assertions;
import rx.android.AndroidSubscriptions;
import rx.functions.Action0;

/** @deprecated this class will be removed soon */
@Deprecated
class OnSubscribeCompoundButtonInputOld implements Observable.OnSubscribe<OnCheckedChangeEvent> {
    private final boolean emitInitialValue;
    private final CompoundButton button;

    public OnSubscribeCompoundButtonInputOld(final CompoundButton button, final boolean emitInitialValue) {
        this.emitInitialValue = emitInitialValue;
        this.button = button;
    }

    @Override
    public void call(final Subscriber<? super OnCheckedChangeEvent> observer) {
        Assertions.assertUiThread();
        final OnSubscribeCompoundButtonInput.CompositeOnCheckedChangeListener composite =
                OnSubscribeCompoundButtonInput.CachedListeners.getFromViewOrCreate(button);

        final CompoundButton.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(final CompoundButton view, final boolean checked) {
                observer.onNext(OnCheckedChangeEvent.create(button, checked));
            }
        };

        final Subscription subscription = AndroidSubscriptions.unsubscribeInUiThread(new Action0() {
            @Override
            public void call() {
                composite.removeOnCheckedChangeListener(listener);
            }
        });

        if (emitInitialValue) {
            observer.onNext(OnCheckedChangeEvent.create(button));
        }

        composite.addOnCheckedChangeListener(listener);
        observer.add(subscription);
    }
}


