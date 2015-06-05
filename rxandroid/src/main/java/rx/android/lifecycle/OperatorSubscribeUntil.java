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

package rx.android.lifecycle;

import rx.Observable;
import rx.Subscriber;
import rx.observers.SerializedSubscriber;

/**
 * Returns an Observable that emits the items from the source Observable until another Observable
 * emits an item.
 * <p>
 * Unlike takeUntil, this choose to unsubscribe the parent rather than calling onComplete().
 */
final class OperatorSubscribeUntil<T, R> implements Observable.Operator<T, T> {

    private final Observable<? extends R> other;

    public OperatorSubscribeUntil(final Observable<? extends R> other) {
        this.other = other;
    }

    @Override
    public Subscriber<? super T> call(final Subscriber<? super T> child) {
        boolean pitfall = true; // FIXME: operator pitfalls: don't unsubscribe the downstream
        final Subscriber<T> serial = new SerializedSubscriber<T>(child, pitfall);
        
        final Subscriber<T> main = new Subscriber<T>(serial, false) {
            @Override
            public void onNext(T t) {
                serial.onNext(t);
            }
            @Override
            public void onError(Throwable e) {
                try {
                    serial.onError(e);
                } finally {
                    serial.unsubscribe();
                }
            }
            @Override
            public void onCompleted() {
                try {
                    serial.onCompleted();
                } finally {
                    serial.unsubscribe();
                }
            }
        };
        
        final Subscriber<R> so = new Subscriber<R>() {
            @Override
            public void onStart() {
                request(Long.MAX_VALUE);
            }
            
            @Override
            public void onCompleted() {
                serial.unsubscribe();
            }

            @Override
            public void onError(Throwable e) {
                main.onError(e);
            }

            @Override
            public void onNext(R t) {
                onCompleted();
            }

        };

        serial.add(main);
        serial.add(so);
        
        child.add(serial);
        
        other.unsafeSubscribe(so);

        return main;
    }
}
