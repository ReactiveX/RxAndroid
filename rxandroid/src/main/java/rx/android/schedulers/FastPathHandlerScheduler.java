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
package rx.android.schedulers;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.TimeUnit;

import rx.Scheduler;
import rx.Subscription;
import rx.android.plugins.RxAndroidPlugins;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

/**
 * A {@link Scheduler} backed by a {@link Handler}. This scheduler is optimized to call the action
 * directly if immediate and scheduled from the target thread. Because of this, it could operate
 * synchronously.
 */
public final class FastPathHandlerScheduler extends Scheduler {
    private final Scheduler actual;
    private final Handler handler;

    /** Create a {@link FastPathHandlerScheduler} which uses {@code handler} to execute actions. */
    public static FastPathHandlerScheduler from(Handler handler) {
        return new FastPathHandlerScheduler(handler);
    }

    FastPathHandlerScheduler(Handler handler) {
        this.actual = HandlerScheduler.from(handler);
        this.handler = handler;
    }

    @Override public Worker createWorker() {
        return new HandlerWorker(actual.createWorker(), handler);
    }

    private static final class HandlerWorker extends Worker {
        private final Worker actual;
        private final Handler handler;

        private HandlerWorker(Worker actual, Handler handler) {
            this.actual = actual;
            this.handler = handler;
        }

        @Override public Subscription schedule(Action0 action) {
            return schedule(action, 0L, TimeUnit.MILLISECONDS);
        }

        @Override public Subscription schedule(Action0 action, long delayTime, TimeUnit unit) {
            // Check if unsubscribed
            if (actual.isUnsubscribed()) {
                return Subscriptions.unsubscribed();
            }

            // Fast path if action is immediate and we are on the target thread
            if (delayTime <= 0L && Looper.myLooper() == handler.getLooper()) {
                // Schedulers hook on action, only for fast path. If actual worker handles, this is done already.
                action = RxAndroidPlugins.getInstance().getSchedulersHook().onSchedule(action);
                action.call();
                return Subscriptions.unsubscribed();
            }

            return actual.schedule(action, delayTime, unit);
        }

        @Override public void unsubscribe() {
            actual.unsubscribe();
        }

        @Override public boolean isUnsubscribed() {
            return actual.isUnsubscribed();
        }
    }
}
