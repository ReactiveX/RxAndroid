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
package rx.android;

import android.os.Handler;
import rx.Scheduler.Worker;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.subscriptions.Subscriptions;
import android.os.Looper;

public final class AndroidSubscriptions {
    private AndroidSubscriptions() {
        throw new AssertionError("No instances");
    }

    /**
     * Create a {@link Subscription} that always runs the specified {@code unsubscribe} on the
     * UI thread.
     */
    public static Subscription unsubscribeInUiThread(final Action0 unsubscribe) {
        return Subscriptions.create(new Action0() {
            @Override
            public void call() {
                if (Looper.getMainLooper() == Looper.myLooper()) {
                    unsubscribe.call();
                } else {
                    final Worker inner = AndroidSchedulers.mainThread().createWorker();
                    inner.schedule(new Action0() {
                        @Override
                        public void call() {
                            unsubscribe.call();
                            inner.unsubscribe();
                        }
                    });
                }
            }
        });
    }

    /**
     * Create a {@link Subscription} that always runs <code>unsubscribe</code> in the thread,
     * associated with given {@link Handler}.
     */
    public static Subscription unsubscribeInHandlerThread(final Action0 unsubscribe, final Handler handler) {
        return Subscriptions.create(new Action0() {
            @Override
            public void call() {
                if (handler.getLooper() == Looper.myLooper()) {
                    unsubscribe.call();
                } else {
                    final Worker inner = AndroidSchedulers.handlerThread(handler).createWorker();
                    inner.schedule(new Action0() {
                        @Override
                        public void call() {
                            unsubscribe.call();
                            inner.unsubscribe();
                        }
                    });
                }
            }
        });
    }
}
