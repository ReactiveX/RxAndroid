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

import java.util.concurrent.TimeUnit;

import rx.Scheduler;
import rx.Subscription;
import rx.android.plugins.RxAndroidPlugins;
import rx.functions.Action0;
import rx.internal.schedulers.ScheduledAction;
import rx.subscriptions.CompositeSubscription;
import rx.subscriptions.Subscriptions;
import android.os.Handler;

/** A {@link Scheduler} backed by a {@link Handler}. */
public final class HandlerScheduler extends Scheduler {
    /** Create a {@link Scheduler} which uses {@code handler} to execute actions. */
    public static HandlerScheduler from(Handler handler) {
        if (handler == null) throw new NullPointerException("handler == null");
        return new HandlerScheduler(handler);
    }

    private final Handler handler;

    HandlerScheduler(Handler handler) {
        this.handler = handler;
    }

    @Override
    public Worker createWorker() {
        return new HandlerWorker(handler);
    }

    static class HandlerWorker extends Worker {

        final Handler handler;

        private final CompositeSubscription compositeSubscription = new CompositeSubscription();

        HandlerWorker(Handler handler) {
            this.handler = handler;
        }

        @Override
        public void unsubscribe() {
            compositeSubscription.unsubscribe();
        }

        @Override
        public boolean isUnsubscribed() {
            return compositeSubscription.isUnsubscribed();
        }

        @Override
        public Subscription schedule(Action0 action, long delayTime, TimeUnit unit) {
            if (compositeSubscription.isUnsubscribed()) {
                return Subscriptions.unsubscribed();
            }

            action = RxAndroidPlugins.getInstance().getSchedulersHook().onSchedule(action);

            final ScheduledAction scheduledAction = new ScheduledAction(action);
            scheduledAction.addParent(compositeSubscription);
            compositeSubscription.add(scheduledAction);

            handler.postDelayed(scheduledAction, unit.toMillis(delayTime));

            scheduledAction.add(Subscriptions.create(new Action0() {
                @Override
                public void call() {
                    handler.removeCallbacks(scheduledAction);
                }
            }));

            return scheduledAction;
        }

        @Override
        public Subscription schedule(final Action0 action) {
            return schedule(action, 0, TimeUnit.MILLISECONDS);
        }
    }
}
