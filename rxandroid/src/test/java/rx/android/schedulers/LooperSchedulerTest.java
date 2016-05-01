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

import android.os.Looper;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import rx.Scheduler;
import rx.Scheduler.Worker;
import rx.Subscription;
import rx.android.plugins.RxAndroidPlugins;
import rx.android.plugins.RxAndroidSchedulersHook;
import rx.exceptions.OnErrorNotImplementedException;
import rx.functions.Action0;

import static java.util.concurrent.TimeUnit.MINUTES;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.robolectric.shadows.ShadowLooper.idleMainLooper;
import static org.robolectric.shadows.ShadowLooper.pauseMainLooper;
import static org.robolectric.shadows.ShadowLooper.runUiThreadTasks;
import static org.robolectric.shadows.ShadowLooper.runUiThreadTasksIncludingDelayedTasks;
import static org.robolectric.shadows.ShadowLooper.unPauseMainLooper;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class LooperSchedulerTest {

    @Before
    public void setUp() {
        RxAndroidPlugins.getInstance().reset();
        pauseMainLooper(); // Take manual control of looper task queue.
    }

    @After
    public void tearDown() {
        RxAndroidPlugins.getInstance().reset();
        unPauseMainLooper();
    }

    private Scheduler scheduler = AndroidSchedulers.from(Looper.getMainLooper());

    @Test
    public void schedulePostsActionImmediately() {
        Worker worker = scheduler.createWorker();

        Action0 action = mock(Action0.class);
        worker.schedule(action);

        runUiThreadTasks();
        verify(action).call();
    }

    @Test
    public void scheduleWithDelayPostsActionWithDelay() {
        Worker worker = scheduler.createWorker();

        Action0 action = mock(Action0.class);
        worker.schedule(action, 1, MINUTES);

        runUiThreadTasks();
        verify(action, never()).call();

        idleMainLooper(MINUTES.toMillis(1));
        runUiThreadTasks();
        verify(action).call();
    }

    @Test
    public void unsubscribeCancelsScheduledAction() {
        Worker worker = scheduler.createWorker();

        Action0 action = mock(Action0.class);
        Subscription subscription = worker.schedule(action);
        subscription.unsubscribe();

        runUiThreadTasks();
        verify(action, never()).call();
    }

    @Test
    public void unsubscribeCancelsScheduledActionWithDelay() {
        Worker worker = scheduler.createWorker();

        Action0 action = mock(Action0.class);
        Subscription subscription = worker.schedule(action, 1, MINUTES);
        subscription.unsubscribe();

        runUiThreadTasksIncludingDelayedTasks();
        verify(action, never()).call();
    }

    @Test
    public void unsubscribeState() {
        Worker worker = scheduler.createWorker();

        Action0 action = mock(Action0.class);
        Subscription subscription = worker.schedule(action);
        assertFalse(subscription.isUnsubscribed());

        subscription.unsubscribe();
        assertTrue(subscription.isUnsubscribed());
    }

    @Test
    public void schedulerHookIsUsed() {
        final Action0 newAction = mock(Action0.class);
        final AtomicReference<Action0> actionRef = new AtomicReference<>();
        RxAndroidPlugins.getInstance().registerSchedulersHook(new RxAndroidSchedulersHook() {
            @Override public Action0 onSchedule(Action0 action) {
                actionRef.set(action); // Capture the original action.
                return newAction; // Return a different one.
            }
        });

        Worker worker = scheduler.createWorker();

        Action0 action = mock(Action0.class);
        worker.schedule(action);

        // Verify our action was passed to the schedulers hook.
        assertSame(action, actionRef.get());

        // Verify the scheduled action was the one returned from the hook.
        runUiThreadTasks();
        verify(newAction).call();
        verify(action, never()).call();
    }

    @Test
    public void workerUnsubscriptionPreventsScheduling() {
        Worker worker = scheduler.createWorker();
        worker.unsubscribe();

        Action0 action = mock(Action0.class);
        worker.schedule(action);

        runUiThreadTasks();
        verify(action, never()).call();
    }

    @Test
    public void workerUnsubscriptionDuringSchedulingCancelsScheduledAction() {
        final AtomicReference<Scheduler.Worker> workerRef = new AtomicReference<>();
        RxAndroidPlugins.getInstance().registerSchedulersHook(new RxAndroidSchedulersHook() {
            @Override public Action0 onSchedule(Action0 action) {
                // Purposefully unsubscribe in an asinine point after the normal unsubscribed check.
                workerRef.get().unsubscribe();
                return super.onSchedule(action);
            }
        });

        Scheduler.Worker worker = scheduler.createWorker();
        workerRef.set(worker);

        Action0 action = mock(Action0.class);
        worker.schedule(action);

        runUiThreadTasks();
        verify(action, never()).call();
    }

    @Test
    public void workerUnsubscriptionCancelsScheduled() {
        Worker worker = scheduler.createWorker();

        Action0 action = mock(Action0.class);
        worker.schedule(action, 1, MINUTES);

        worker.unsubscribe();

        runUiThreadTasks();
        verify(action, never()).call();
    }

    @Test
    public void workerUnsubscriptionDoesNotAffectOtherWorkers() {
        Scheduler.Worker workerA = scheduler.createWorker();
        Action0 actionA = mock(Action0.class);
        workerA.schedule(actionA, 1, MINUTES);

        Scheduler.Worker workerB = scheduler.createWorker();
        Action0 actionB = mock(Action0.class);
        workerB.schedule(actionB, 1, MINUTES);

        workerA.unsubscribe();

        runUiThreadTasksIncludingDelayedTasks();
        verify(actionA, never()).call();
        verify(actionB).call();
    }

    @Test
    public void workerUnsubscribeState() {
        Worker worker = scheduler.createWorker();
        assertFalse(worker.isUnsubscribed());

        worker.unsubscribe();
        assertTrue(worker.isUnsubscribed());
    }

    @Test public void throwingActionRoutedToHookAndThreadHandler() {
        // TODO Test hook as well. Requires https://github.com/ReactiveX/RxJava/pull/3820.

        Thread thread = Thread.currentThread();
        UncaughtExceptionHandler originalHandler = thread.getUncaughtExceptionHandler();

        final AtomicReference<Throwable> throwableRef = new AtomicReference<>();
        thread.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
            @Override public void uncaughtException(Thread thread, Throwable ex) {
                throwableRef.set(ex);
            }
        });

        Worker worker = scheduler.createWorker();

        final NullPointerException npe = new NullPointerException();
        Action0 action = new Action0() {
            @Override public void call() {
                throw npe;
            }
        };
        worker.schedule(action);

        runUiThreadTasks();
        Throwable throwable = throwableRef.get();
        assertTrue(throwable instanceof IllegalStateException);
        assertEquals("Fatal Exception thrown on Scheduler.Worker thread.", throwable.getMessage());
        assertSame(npe, throwable.getCause());

        // Restore the original uncaught exception handler.
        thread.setUncaughtExceptionHandler(originalHandler);
    }

    @Test public void actionMissingErrorHandlerRoutedToHookAndThreadHandler() {
        // TODO Test hook as well. Requires https://github.com/ReactiveX/RxJava/pull/3820.

        Thread thread = Thread.currentThread();
        UncaughtExceptionHandler originalHandler = thread.getUncaughtExceptionHandler();

        final AtomicReference<Throwable> throwableRef = new AtomicReference<>();
        thread.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {
            @Override public void uncaughtException(Thread thread, Throwable ex) {
                throwableRef.set(ex);
            }
        });

        Worker worker = scheduler.createWorker();

        final OnErrorNotImplementedException oenie =
            new OnErrorNotImplementedException(new NullPointerException());
        Action0 action = new Action0() {
            @Override public void call() {
                throw oenie;
            }
        };
        worker.schedule(action);

        runUiThreadTasks();
        Throwable throwable = throwableRef.get();
        assertTrue(throwable instanceof IllegalStateException);
        assertEquals("Exception thrown on Scheduler.Worker thread. Add `onError` handling.",
            throwable.getMessage());
        assertSame(oenie, throwable.getCause());

        // Restore the original uncaught exception handler.
        thread.setUncaughtExceptionHandler(originalHandler);
    }

    @Test
    public void fromNullThrows() {
        try {
            AndroidSchedulers.from(null);
            fail();
        } catch (NullPointerException e) {
            assertEquals("looper == null", e.getMessage());
        }
    }
}
