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
import rx.Subscriber;
import rx.Subscription;
import rx.android.internal.Assertions;
import rx.android.AndroidSubscriptions;
import rx.functions.Action0;

/** @deprecated this class will be removed soon */
@Deprecated
final class OnSubscribeViewClickOld implements Observable.OnSubscribe<OnClickEvent> {
    private final boolean emitInitialValue;
    private final View view;

    public OnSubscribeViewClickOld(final View view, final boolean emitInitialValue) {
        this.emitInitialValue = emitInitialValue;
        this.view = view;
    }

    @Override
    public void call(final Subscriber<? super OnClickEvent> observer) {
        Assertions.assertUiThread();
        final OnSubscribeViewClick.CompositeOnClickListener composite = OnSubscribeViewClick.CachedListeners.getFromViewOrCreate(view);

        final View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(final View clicked) {
                observer.onNext(OnClickEvent.create(view));
            }
        };

        final Subscription subscription = AndroidSubscriptions.unsubscribeInUiThread(new Action0() {
            @Override
            public void call() {
                composite.removeOnClickListener(listener);
            }
        });

        if (emitInitialValue) {
            observer.onNext(OnClickEvent.create(view));
        }

        composite.addOnClickListener(listener);
        observer.add(subscription);
    }
}
