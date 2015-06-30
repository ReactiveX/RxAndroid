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

import java.util.concurrent.atomic.AtomicReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Scheduler;
import rx.Scheduler.Worker;
import rx.Subscriber;
import rx.Subscription;
import rx.android.plugins.RxAndroidPlugins;
import rx.android.plugins.RxAndroidPluginsTest;
import rx.android.plugins.RxAndroidSchedulersHook;
import rx.functions.Action0;
import rx.schedulers.Schedulers;

import static org.junit.Assert.assertSame;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class AndroidSchedulersTest {

    @Before @After
    public void setUpAndTearDown() {
        RxAndroidPluginsTest.resetPlugins();
    }

    @Test
    public void shouldScheduleImmediateActionOnHandlerThread() {
        final Handler handler = mock(Handler.class);
        @SuppressWarnings("unchecked")
        final Action0 action = mock(Action0.class);

        Scheduler scheduler = AndroidSchedulers.handlerThread(handler);
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
        final Handler handler = mock(Handler.class);
        @SuppressWarnings("unchecked")
        final Action0 action = mock(Action0.class);

        Scheduler scheduler = AndroidSchedulers.handlerThread(handler);
        Worker inner = scheduler.createWorker();
        inner.schedule(action, 1L, TimeUnit.SECONDS);

        // verify that we post to the given Handler
        ArgumentCaptor<Runnable> runnable = ArgumentCaptor.forClass(Runnable.class);
        verify(handler).postDelayed(runnable.capture(), eq(1000L));

        // verify that the given handler delegates to our action
        runnable.getValue().run();
        verify(action).call();
    }

    @Test
    public void shouldRemoveCallbacksFromHandlerWhenUnsubscribedSubscription() {
        final Handler handler = spy(new Handler());
        final Observable.OnSubscribe<Integer> onSubscribe = mock(Observable.OnSubscribe.class);
        final Subscription subscription = Observable.create(onSubscribe)
                .subscribeOn(AndroidSchedulers.handlerThread(handler))
                .subscribe();

        verify(onSubscribe).call(Matchers.any(Subscriber.class));

        subscription.unsubscribe();

        verify(handler).removeCallbacks(Matchers.any(Runnable.class));
    }

    @Test
    public void shouldNotCallOnSubscribeWhenSubscriptionUnsubscribedBeforeDelay() {
        final Observable.OnSubscribe<Integer> onSubscribe = mock(Observable.OnSubscribe.class);
        final Handler handler = spy(new Handler());

        final Scheduler scheduler = AndroidSchedulers.handlerThread(handler);
        final Worker worker = spy(scheduler.createWorker());

        final Scheduler spyScheduler = spy(scheduler);
        when(spyScheduler.createWorker()).thenReturn(worker);

        final Subscription subscription = Observable.create(onSubscribe)
                .delaySubscription(1, TimeUnit.MINUTES, spyScheduler)
                .subscribe();

        verify(worker).schedule(Matchers.any(Action0.class),
                Matchers.eq(1L), Matchers.eq(TimeUnit.MINUTES));
        verify(handler).postDelayed(Matchers.any(Runnable.class),
                Matchers.eq(TimeUnit.MINUTES.toMillis(1L)));

        subscription.unsubscribe();

        Robolectric.runUiThreadTasksIncludingDelayedTasks();

        verify(onSubscribe, never()).call(Matchers.any(Subscriber.class));
        verify(handler).removeCallbacks(Matchers.any(Runnable.class));
    }

    @Test public void mainThreadCallsThroughToHook() {
        final Scheduler scheduler = Schedulers.immediate();
        RxAndroidSchedulersHook hook = new RxAndroidSchedulersHook() {
            @Override public Scheduler getMainThreadScheduler() {
                return scheduler;
            }
        };
        RxAndroidPlugins.getInstance().registerSchedulersHook(hook);

        Scheduler mainThread = AndroidSchedulers.mainThread();
        assertSame(Schedulers.immediate(), mainThread);
    }

    @Test public void handlerSchedulerCallsThroughToHook() {
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

        Scheduler scheduler = AndroidSchedulers.handlerThread(handler);
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
}
