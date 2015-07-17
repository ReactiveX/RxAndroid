/*
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
package rx.android.subscribers;

import rx.Subscriber;
import rx.exceptions.OnErrorNotImplementedException;
import rx.functions.Action0;
import rx.functions.Action1;

/**
 * Helper class to encapsulate actions into a subscriber.
 *
 * @param <T>
 */
public class ActionSubscriber<T> extends Subscriber<T> {
    private final Action1<? super T> onNext;
    private final Action1<Throwable> onError;
    private final Action0 onCompleted;

    public ActionSubscriber(Action1<? super T> onNext) {
        this(onNext, null, null);
    }

    public ActionSubscriber(Action1<? super T> onNext, Action1<Throwable> onError) {
        this(onNext, onError, null);
    }

    public ActionSubscriber(Action1<? super T> onNext, Action1<Throwable> onError,
            Action0 onCompleted) {
        this.onNext = onNext;
        this.onError = onError;
        this.onCompleted = onCompleted;
    }

    @Override
    public void onNext(T t) {
        if (onNext != null) {
            onNext.call(t);
        }
    }

    @Override
    public void onError(Throwable e) {
        if (onError != null) {
            onError.call(e);
        } else {
            throw new OnErrorNotImplementedException(e);
        }
    }

    @Override
    public void onCompleted() {
        if (onCompleted != null) {
            onCompleted.call();
        }
    }
}
