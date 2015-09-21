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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

import rx.Observable;
import rx.Scheduler;
import rx.Scheduler.Worker;
import rx.Subscriber;
import rx.Subscription;
import rx.android.plugins.RxAndroidPlugins;
import rx.android.plugins.RxAndroidSchedulersHook;
import rx.functions.Action0;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class HandlerSchedulerTest {

    @Before @After
    public void setUpAndTearDown() {
        RxAndroidPlugins.getInstance().reset();
    }

    @Test
    public void fromNullThrows() {
        try {
            HandlerScheduler.from(null);
            fail();
        } catch (NullPointerException e) {
            assertEquals("handler == null", e.getMessage());
        }
    }

    @Test
    public void shouldScheduleImmediateActionOnHandlerThread() {
        Handler handler = mock(Handler.class);
        @SuppressWarnings("unchecked")
        Action0 action = mock(Action0.class);

        Scheduler scheduler = HandlerScheduler.from(handler);
        Worker inner = scheduler.createWorker();
        inner.schedule(action);

        // verify that we post to the given Handler
        ArgumentCaptor<Runnable> runnable = ArgumentCaptor.forClass(Runnable.class);
        verify(handler).postDelayed(runnable.capture(), eq(0L));

        // verify that the given handler delegates to our action
        runnable.getValue().run();
        verify(action).call();
    }

    @Test
    public void shouldScheduleDelayedActionOnHandlerThread() {
        Handler handler = mock(Handler.class);
        @SuppressWarnings("unchecked")
        Action0 action = mock(Action0.class);

        Scheduler scheduler = HandlerScheduler.from(handler);
        Worker inner = scheduler.createWorker();
        inner.schedule(action, 1, SECONDS);

        // verify that we post to the given Handler
        ArgumentCaptor<Runnable> runnable = ArgumentCaptor.forClass(Runnable.class);
        verify(handler).postDelayed(runnable.capture(), eq(1000L));

        // verify that the given handler delegates to our action
        runnable.getValue().run();
        verify(action).call();
    }

    @Test
    public void shouldRemoveCallbacksFromHandlerWhenUnsubscribedSubscription() {
        Handler handler = spy(new Handler());
        Observable.OnSubscribe<Integer> onSubscribe = mock(Observable.OnSubscribe.class);
        Subscription subscription = Observable.create(onSubscribe)
                .subscribeOn(HandlerScheduler.from(handler))
                .subscribe();

        verify(onSubscribe).call(any(Subscriber.class));

        subscription.unsubscribe();

        verify(handler).removeCallbacks(any(Runnable.class));
    }

    @Test
    public void shouldNotCallOnSubscribeWhenSubscriptionUnsubscribedBeforeDelay() {
        Observable.OnSubscribe<Integer> onSubscribe = mock(Observable.OnSubscribe.class);
        Handler handler = spy(new Handler());

        final Worker worker = spy(new HandlerScheduler.HandlerWorker(handler));
        Scheduler scheduler = new Scheduler() {
            @Override public Worker createWorker() {
                return worker;
            }
        };

        Subscription subscription = Observable.create(onSubscribe)
                .delaySubscription(1, MINUTES, scheduler)
                .subscribe();

        verify(worker).schedule(any(Action0.class), eq(1L), eq(MINUTES));
        verify(handler).postDelayed(any(Runnable.class), eq(MINUTES.toMillis(1)));

        subscription.unsubscribe();

        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        verify(onSubscribe, never()).call(any(Subscriber.class));
        verify(handler).removeCallbacks(any(Runnable.class));
    }

    @Test
    public void handlerSchedulerCallsThroughToHook() {
        final AtomicReference<Action0> actionRef = new AtomicReference<Action0>();
        RxAndroidPlugins.getInstance().registerSchedulersHook(new RxAndroidSchedulersHook() {
            @Override public Action0 onSchedule(Action0 action) {
                actionRef.set(action);
                return super.onSchedule(action);
            }
        });

        Handler handler = mock(Handler.class);
        @SuppressWarnings("unchecked")
        Action0 action = mock(Action0.class);

        Scheduler scheduler = HandlerScheduler.from(handler);
        Worker inner = scheduler.createWorker();
        inner.schedule(action);

        // Verify the action was passed through the schedulers hook.
        assertSame(action, actionRef.get());

        // Verify that we post to the given Handler.
        ArgumentCaptor<Runnable> runnable = ArgumentCaptor.forClass(Runnable.class);
        verify(handler).postDelayed(runnable.capture(), eq(0L));

        // Verify that the given handler delegates to our action.
        runnable.getValue().run();
        verify(action).call();
    }

    @Test
    public void shouldNotScheduleAfterUnsubscribe() {
        Scheduler scheduler = HandlerScheduler.from(new Handler());
        Worker inner = scheduler.createWorker();
        inner.unsubscribe();

        // Assert that work scheduled after unsubscribe() is never called
        final AtomicBoolean neverCalled = new AtomicBoolean(true);
        inner.schedule(new Action0() {
            @Override
            public void call() {
                neverCalled.set(false);
            }
        });
        assertTrue(neverCalled.get());
    }

    @Test
    public void shouldNotScheduleAfterUnsubscribeRaceCondition() {
        Scheduler scheduler = HandlerScheduler.from(new Handler());
        final Scheduler.Worker inner = scheduler.createWorker();

        RxAndroidPlugins.getInstance().registerSchedulersHook(new RxAndroidSchedulersHook() {
            @Override public Action0 onSchedule(Action0 action) {
                // Purposefully unsubscribe in an asinine point,
                // after the normal isUnsubscribed() check
                inner.unsubscribe();
                return super.onSchedule(action);
            }
        });

        final AtomicBoolean neverCalled = new AtomicBoolean(true);
        inner.schedule(new Action0() {
            @Override
            public void call() {
                neverCalled.set(false);
            }
        }, 1, TimeUnit.MILLISECONDS);

        ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

        assertTrue(neverCalled.get());
    }
}
