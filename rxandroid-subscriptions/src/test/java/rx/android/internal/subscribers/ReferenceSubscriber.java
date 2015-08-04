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
package rx.android.internal.subscribers;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class ReferenceSubscriber<T> extends NotifyingSubscriber<T> {
    private final AtomicReference<T> onNext;
    private final AtomicReference<Throwable> onError;
    private final AtomicBoolean onCompleted;
    private final AtomicInteger onNextCount = new AtomicInteger();

    public ReferenceSubscriber() {
        this(new AtomicReference<T>(), new AtomicReference<Throwable>(), new AtomicBoolean());
    }

    public ReferenceSubscriber(AtomicReference<T> onNext, AtomicReference<Throwable> onError,
            AtomicBoolean onCompleted) {
        this.onNext = onNext;
        this.onError = onError;
        this.onCompleted = onCompleted;
    }

    @Override public void onCompleted() {
        onCompleted.set(true);
    }

    @Override public void onError(Throwable e) {
        onError.set(e);
    }

    @Override public void onNext(T t) {
        onNextCount.incrementAndGet();
        onNext.set(t);
    }

    @Override protected void onUnsubscribe() {

    }

    public T getLatest() {
        return onNext.get();
    }

    public Throwable getError() {
        return onError.get();
    }

    public boolean isCompleted() {
        return onCompleted.get();
    }

    public int getOnNextCount() {
        return onNextCount.get();
    }
}
