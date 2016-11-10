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
package io.reactivex.android.schedulers;

import android.os.Handler;
import android.os.Looper;
import io.reactivex.Scheduler;
import io.reactivex.Scheduler.Worker;
import io.reactivex.android.testutil.CountingRunnable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.plugins.RxJavaPlugins;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.robolectric.shadows.ShadowLooper.pauseMainLooper;
import static org.robolectric.shadows.ShadowLooper.runUiThreadTasks;
import static org.robolectric.shadows.ShadowLooper.runUiThreadTasksIncludingDelayedTasks;
import static org.robolectric.shadows.ShadowLooper.unPauseMainLooper;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public final class HandlerSchedulerTest {
    @Before
    public void setUp() {
        RxJavaPlugins.reset();
        pauseMainLooper(); // Take manual control of looper task queue.
    }

    @After
    public void tearDown() {
        RxJavaPlugins.reset();
        unPauseMainLooper();
    }

    private Scheduler scheduler = new HandlerScheduler(new Handler(Looper.getMainLooper()));

    @Test
    public void directScheduleOncePostsImmediately() {
        CountingRunnable counter = new CountingRunnable();
        scheduler.scheduleDirect(counter);

        runUiThreadTasks();
        assertEquals(1, counter.get());
    }

    @Test
    public void directScheduleOnceWithNegativeDelayPostsImmediately() {
        CountingRunnable counter = new CountingRunnable();
        scheduler.scheduleDirect(counter, -1, TimeUnit.MINUTES);

        runUiThreadTasks();
        assertEquals(1, counter.get());
    }

    @Test
    public void directScheduleOnceUsesHook() {
        final CountingRunnable newCounter = new CountingRunnable();
        final AtomicReference<Runnable> runnableRef = new AtomicReference<>();
        RxJavaPlugins.setScheduleHandler(new Function<Runnable, Runnable>() {
            @Override public Runnable apply(Runnable runnable) {
                runnableRef.set(runnable);
                return newCounter;
            }
        });

        CountingRunnable counter = new CountingRunnable();
        scheduler.scheduleDirect(counter);

        // Verify our runnable was passed to the schedulers hook.
        assertSame(counter, runnableRef.get());

        runUiThreadTasks();
        // Verify the scheduled runnable was the one returned from the hook.
        assertEquals(1, newCounter.get());
        assertEquals(0, counter.get());
    }

    @Test
    public void directScheduleOnceDisposedDoesNotRun() {
        CountingRunnable counter = new CountingRunnable();
        Disposable disposable = scheduler.scheduleDirect(counter);
        disposable.dispose();

        runUiThreadTasks();
        assertEquals(0, counter.get());
    }

    @Test
    public void directScheduleOnceWithDelayPostsWithDelay() {
        CountingRunnable counter = new CountingRunnable();
        scheduler.scheduleDirect(counter, 1, MINUTES);

        runUiThreadTasks();
        assertEquals(0, counter.get());

        idleMainLooper(1, MINUTES);
        runUiThreadTasks();
        assertEquals(1, counter.get());
    }

    @Test
    public void directScheduleOnceWithDelayUsesHook() {
        final CountingRunnable newCounter = new CountingRunnable();
        final AtomicReference<Runnable> runnableRef = new AtomicReference<>();
        RxJavaPlugins.setScheduleHandler(new Function<Runnable, Runnable>() {
            @Override public Runnable apply(Runnable runnable) {
                runnableRef.set(runnable);
                return newCounter;
            }
        });

        CountingRunnable counter = new CountingRunnable();
        scheduler.scheduleDirect(counter, 1, MINUTES);

        // Verify our runnable was passed to the schedulers hook.
        assertSame(counter, runnableRef.get());

        idleMainLooper(1, MINUTES);
        runUiThreadTasks();
        // Verify the scheduled runnable was the one returned from the hook.
        assertEquals(1, newCounter.get());
        assertEquals(0, counter.get());
    }

    @Test
    public void directScheduleOnceWithDelayDisposedDoesNotRun() {
        CountingRunnable counter = new CountingRunnable();
        Disposable disposable = scheduler.scheduleDirect(counter, 1, MINUTES);

        idleMainLooper(30, SECONDS);
        disposable.dispose();

        idleMainLooper(30, SECONDS);
        runUiThreadTasks();
        assertEquals(0, counter.get());
    }

    @Test @Ignore("Implementation delegated to default RxJava implementation")
    public void directSchedulePeriodicallyReschedulesItself() {
        CountingRunnable counter = new CountingRunnable();
        scheduler.schedulePeriodicallyDirect(counter, 1, 1, MINUTES);

        runUiThreadTasks();
        assertEquals(0, counter.get());

        idleMainLooper(1, MINUTES);
        runUiThreadTasks();
        assertEquals(1, counter.get());

        idleMainLooper(1, MINUTES);
        runUiThreadTasks();
        assertEquals(2, counter.get());

        idleMainLooper(1, MINUTES);
        runUiThreadTasks();
        assertEquals(3, counter.get());
    }

    @Test @Ignore("Implementation delegated to default RxJava implementation")
    public void directSchedulePeriodicallyUsesHookOnce() {
        final CountingRunnable newCounter = new CountingRunnable();
        final AtomicReference<Runnable> runnableRef = new AtomicReference<>();
        RxJavaPlugins.setScheduleHandler(new Function<Runnable, Runnable>() {
            @Override public Runnable apply(Runnable runnable) {
                runnableRef.set(runnable);
                return newCounter;
            }
        });

        CountingRunnable counter = new CountingRunnable();
        scheduler.schedulePeriodicallyDirect(counter, 1, 1, MINUTES);

        // Verify our action was passed to the schedulers hook.
        assertSame(counter, runnableRef.get());
        runnableRef.set(null);

        idleMainLooper(1, MINUTES);
        runUiThreadTasks();
        // Verify the scheduled action was the one returned from the hook.
        assertEquals(1, newCounter.get());
        assertEquals(0, counter.get());

        // Ensure the hook was not called again when the runnable re-scheduled itself.
        assertNull(runnableRef.get());
    }

    @Test @Ignore("Implementation delegated to default RxJava implementation")
    public void directSchedulePeriodicallyDisposedDoesNotRun() {
        CountingRunnable counter = new CountingRunnable();
        Disposable disposable = scheduler.schedulePeriodicallyDirect(counter, 1, 1, MINUTES);

        runUiThreadTasks();
        assertEquals(0, counter.get());

        idleMainLooper(1, MINUTES);
        runUiThreadTasks();
        assertEquals(1, counter.get());

        idleMainLooper(1, MINUTES);
        runUiThreadTasks();
        assertEquals(2, counter.get());

        disposable.dispose();

        idleMainLooper(1, MINUTES);
        runUiThreadTasks();
        assertEquals(2, counter.get());
    }

    @Test @Ignore("Implementation delegated to default RxJava implementation")
    public void directSchedulePeriodicallyDisposedDuringRunDoesNotReschedule() {
        final AtomicReference<Disposable> disposableRef = new AtomicReference<>();
        CountingRunnable counter = new CountingRunnable() {
            @Override public void run() {
                super.run();
                if (get() == 2) {
                    disposableRef.get().dispose();
                }
            }
        };
        Disposable disposable = scheduler.schedulePeriodicallyDirect(counter, 1, 1, MINUTES);
        disposableRef.set(disposable);

        runUiThreadTasks();
        assertEquals(0, counter.get());

        idleMainLooper(1, MINUTES);
        runUiThreadTasks();
        assertEquals(1, counter.get());

        idleMainLooper(1, MINUTES);
        runUiThreadTasks();
        assertEquals(2, counter.get());

        // Dispose will have happened here during the last run() execution.

        idleMainLooper(1, MINUTES);
        runUiThreadTasks();
        assertEquals(2, counter.get());
    }

    @Test @Ignore("Implementation delegated to default RxJava implementation")
    public void directSchedulePeriodicallyThrowingDoesNotReschedule() {
        CountingRunnable counter = new CountingRunnable() {
            @Override public void run() {
                super.run();
                if (get() == 2) {
                    throw new RuntimeException("Broken!");
                }
            }
        };
        scheduler.schedulePeriodicallyDirect(counter, 1, 1, MINUTES);

        runUiThreadTasks();
        assertEquals(0, counter.get());

        idleMainLooper(1, MINUTES);
        runUiThreadTasks();
        assertEquals(1, counter.get());

        idleMainLooper(1, MINUTES);
        runUiThreadTasks();
        assertEquals(2, counter.get());

        // Exception will have happened here during the last run() execution.

        idleMainLooper(1, MINUTES);
        runUiThreadTasks();
        assertEquals(2, counter.get());
    }

    @Test
    public void workerScheduleOncePostsImmediately() {
        Worker worker = scheduler.createWorker();

        CountingRunnable counter = new CountingRunnable();
        worker.schedule(counter);

        runUiThreadTasks();
        assertEquals(1, counter.get());
    }

    @Test
    public void workerScheduleOnceWithNegativeDelayPostsImmediately() {
        Worker worker = scheduler.createWorker();

        CountingRunnable counter = new CountingRunnable();
        worker.schedule(counter, -1, TimeUnit.MINUTES);

        runUiThreadTasks();
        assertEquals(1, counter.get());
    }

    @Test
    public void workerScheduleOnceUsesHook() {
        final CountingRunnable newCounter = new CountingRunnable();
        final AtomicReference<Runnable> runnableRef = new AtomicReference<>();
        RxJavaPlugins.setScheduleHandler(new Function<Runnable, Runnable>() {
            @Override public Runnable apply(Runnable runnable) {
                runnableRef.set(runnable);
                return newCounter;
            }
        });

        Worker worker = scheduler.createWorker();

        CountingRunnable counter = new CountingRunnable();
        worker.schedule(counter);

        // Verify our runnable was passed to the schedulers hook.
        assertSame(counter, runnableRef.get());

        runUiThreadTasks();
        // Verify the scheduled runnable was the one returned from the hook.
        assertEquals(1, newCounter.get());
        assertEquals(0, counter.get());
    }

    @Test
    public void workerScheduleOnceDisposedDoesNotRun() {
        Worker worker = scheduler.createWorker();

        CountingRunnable counter = new CountingRunnable();
        Disposable disposable = worker.schedule(counter);
        disposable.dispose();

        runUiThreadTasks();
        assertEquals(0, counter.get());
    }

    @Test
    public void workerScheduleOnceWithDelayPostsWithDelay() {
        Worker worker = scheduler.createWorker();

        CountingRunnable counter = new CountingRunnable();
        worker.schedule(counter, 1, MINUTES);

        runUiThreadTasks();
        assertEquals(0, counter.get());

        idleMainLooper(1, MINUTES);
        runUiThreadTasks();
        assertEquals(1, counter.get());
    }

    @Test
    public void workerScheduleOnceWithDelayUsesHook() {
        final CountingRunnable newCounter = new CountingRunnable();
        final AtomicReference<Runnable> runnableRef = new AtomicReference<>();
        RxJavaPlugins.setScheduleHandler(new Function<Runnable, Runnable>() {
            @Override public Runnable apply(Runnable runnable) {
                runnableRef.set(runnable);
                return newCounter;
            }
        });

        Worker worker = scheduler.createWorker();

        CountingRunnable counter = new CountingRunnable();
        worker.schedule(counter, 1, MINUTES);

        // Verify our runnable was passed to the schedulers hook.
        assertSame(counter, runnableRef.get());

        idleMainLooper(1, MINUTES);
        runUiThreadTasks();
        // Verify the scheduled runnable was the one returned from the hook.
        assertEquals(1, newCounter.get());
        assertEquals(0, counter.get());
    }

    @Test
    public void workerScheduleOnceWithDelayDisposedDoesNotRun() {
        Worker worker = scheduler.createWorker();

        CountingRunnable counter = new CountingRunnable();
        Disposable disposable = worker.schedule(counter, 1, MINUTES);

        idleMainLooper(30, SECONDS);
        disposable.dispose();

        idleMainLooper(30, SECONDS);
        runUiThreadTasks();
        assertEquals(0, counter.get());
    }

    @Test @Ignore("Implementation delegated to default RxJava implementation")
    public void workerSchedulePeriodicallyReschedulesItself() {
        Worker worker = scheduler.createWorker();

        CountingRunnable counter = new CountingRunnable();
        worker.schedulePeriodically(counter, 1, 1, MINUTES);

        runUiThreadTasks();
        assertEquals(0, counter.get());

        idleMainLooper(1, MINUTES);
        runUiThreadTasks();
        assertEquals(1, counter.get());

        idleMainLooper(1, MINUTES);
        runUiThreadTasks();
        assertEquals(2, counter.get());

        idleMainLooper(1, MINUTES);
        runUiThreadTasks();
        assertEquals(3, counter.get());
    }

    @Test @Ignore("Implementation delegated to default RxJava implementation")
    public void workerSchedulePeriodicallyUsesHookOnce() {
        Worker worker = scheduler.createWorker();

        final CountingRunnable newCounter = new CountingRunnable();
        final AtomicReference<Runnable> runnableRef = new AtomicReference<>();
        RxJavaPlugins.setScheduleHandler(new Function<Runnable, Runnable>() {
            @Override public Runnable apply(Runnable runnable) {
                runnableRef.set(runnable);
                return newCounter;
            }
        });

        CountingRunnable counter = new CountingRunnable();
        worker.schedulePeriodically(counter, 1, 1, MINUTES);

        // Verify our action was passed to the schedulers hook.
        assertSame(counter, runnableRef.get());
        runnableRef.set(null);

        idleMainLooper(1, MINUTES);
        runUiThreadTasks();
        // Verify the scheduled action was the one returned from the hook.
        assertEquals(1, newCounter.get());
        assertEquals(0, counter.get());

        // Ensure the hook was not called again when the runnable re-scheduled itself.
        assertNull(runnableRef.get());
    }

    @Test @Ignore("Implementation delegated to default RxJava implementation")
    public void workerSchedulePeriodicallyDisposedDoesNotRun() {
        Worker worker = scheduler.createWorker();

        CountingRunnable counter = new CountingRunnable();
        Disposable disposable = worker.schedulePeriodically(counter, 1, 1, MINUTES);

        runUiThreadTasks();
        assertEquals(0, counter.get());

        idleMainLooper(1, MINUTES);
        runUiThreadTasks();
        assertEquals(1, counter.get());

        idleMainLooper(1, MINUTES);
        runUiThreadTasks();
        assertEquals(2, counter.get());

        disposable.dispose();

        idleMainLooper(1, MINUTES);
        runUiThreadTasks();
        assertEquals(2, counter.get());
    }

    @Test @Ignore("Implementation delegated to default RxJava implementation")
    public void workerSchedulePeriodicallyDisposedDuringRunDoesNotReschedule() {
        Worker worker = scheduler.createWorker();

        final AtomicReference<Disposable> disposableRef = new AtomicReference<>();
        CountingRunnable counter = new CountingRunnable() {
            @Override public void run() {
                super.run();
                if (get() == 2) {
                    disposableRef.get().dispose();
                }
            }
        };
        Disposable disposable = worker.schedulePeriodically(counter, 1, 1, MINUTES);
        disposableRef.set(disposable);

        runUiThreadTasks();
        assertEquals(0, counter.get());

        idleMainLooper(1, MINUTES);
        runUiThreadTasks();
        assertEquals(1, counter.get());

        idleMainLooper(1, MINUTES);
        runUiThreadTasks();
        assertEquals(2, counter.get());

        // Dispose will have happened here during the last run() execution.

        idleMainLooper(1, MINUTES);
        runUiThreadTasks();
        assertEquals(2, counter.get());
    }

    @Test @Ignore("Implementation delegated to default RxJava implementation")
    public void workerSchedulePeriodicallyThrowingDoesNotReschedule() {
        Worker worker = scheduler.createWorker();

        CountingRunnable counter = new CountingRunnable() {
            @Override public void run() {
                super.run();
                if (get() == 2) {
                    throw new RuntimeException("Broken!");
                }
            }
        };
        worker.schedulePeriodically(counter, 1, 1, MINUTES);

        runUiThreadTasks();
        assertEquals(0, counter.get());

        idleMainLooper(1, MINUTES);
        runUiThreadTasks();
        assertEquals(1, counter.get());

        idleMainLooper(1, MINUTES);
        runUiThreadTasks();
        assertEquals(2, counter.get());

        // Exception will have happened here during the last run() execution.

        idleMainLooper(1, MINUTES);
        runUiThreadTasks();
        assertEquals(2, counter.get());
    }

    @Test
    public void workerDisposableTracksDisposedState() {
        Worker worker = scheduler.createWorker();

        CountingRunnable counter = new CountingRunnable();
        Disposable disposable = worker.schedule(counter);
        assertFalse(disposable.isDisposed());

        disposable.dispose();
        assertTrue(disposable.isDisposed());
    }

    @Test
    public void workerUnsubscriptionDuringSchedulingCancelsScheduledAction() {
        final AtomicReference<Worker> workerRef = new AtomicReference<>();
        RxJavaPlugins.setScheduleHandler(new Function<Runnable, Runnable>() {
            @Override public Runnable apply(Runnable runnable) {
                // Purposefully unsubscribe in an asinine point after the normal unsubscribed check.
                workerRef.get().dispose();
                return runnable;
            }
        });

        Worker worker = scheduler.createWorker();
        workerRef.set(worker);

        CountingRunnable counter = new CountingRunnable();
        worker.schedule(counter);

        runUiThreadTasks();
        assertEquals(0, counter.get());
    }

    @Test
    public void workerDisposeCancelsScheduled() {
        Worker worker = scheduler.createWorker();

        CountingRunnable counter = new CountingRunnable();
        worker.schedule(counter, 1, MINUTES);

        worker.dispose();

        runUiThreadTasks();
        assertEquals(0, counter.get());
    }

    @Test
    public void workerUnsubscriptionDoesNotAffectOtherWorkers() {
        Worker workerA = scheduler.createWorker();
        CountingRunnable counterA = new CountingRunnable();
        workerA.schedule(counterA, 1, MINUTES);

        Worker workerB = scheduler.createWorker();
        CountingRunnable counterB = new CountingRunnable();
        workerB.schedule(counterB, 1, MINUTES);

        workerA.dispose();

        runUiThreadTasksIncludingDelayedTasks();
        assertEquals(0, counterA.get());
        assertEquals(1, counterB.get());
    }

    @Test
    public void workerTracksDisposedState() {
        Worker worker = scheduler.createWorker();
        assertFalse(worker.isDisposed());

        worker.dispose();
        assertTrue(worker.isDisposed());
    }

    @Test
    public void disposedWorkerReturnsDisposedDisposables() {
        Worker worker = scheduler.createWorker();
        worker.dispose();

        Disposable disposable = worker.schedule(new CountingRunnable());
        assertTrue(disposable.isDisposed());
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
        Runnable action = new Runnable() {
            @Override public void run() {
                throw npe;
            }
        };
        worker.schedule(action);

        runUiThreadTasks();
        Throwable throwable = throwableRef.get();
        assertTrue(throwable instanceof IllegalStateException);
        assertEquals("Fatal Exception thrown on Scheduler.", throwable.getMessage());
        assertSame(npe, throwable.getCause());

        // Restore the original uncaught exception handler.
        thread.setUncaughtExceptionHandler(originalHandler);
    }

    @Test
    public void directScheduleOnceInputValidation() {
        try {
            scheduler.scheduleDirect(null);
            fail();
        } catch (NullPointerException e) {
            assertEquals("run == null", e.getMessage());
        }
        try {
            scheduler.scheduleDirect(null, 1, MINUTES);
            fail();
        } catch (NullPointerException e) {
            assertEquals("run == null", e.getMessage());
        }
        try {
            scheduler.scheduleDirect(new CountingRunnable(), 1, null);
            fail();
        } catch (NullPointerException e) {
            assertEquals("unit == null", e.getMessage());
        }
    }

    @Test @Ignore("Implementation delegated to default RxJava implementation")
    public void directSchedulePeriodicallyInputValidation() {
        try {
            scheduler.schedulePeriodicallyDirect(null, 1, 1, MINUTES);
            fail();
        } catch (NullPointerException e) {
            assertEquals("run == null", e.getMessage());
        }
        try {
            scheduler.schedulePeriodicallyDirect(new CountingRunnable(), 1, -1, MINUTES);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("period < 0: -1", e.getMessage());
        }
        try {
            scheduler.schedulePeriodicallyDirect(new CountingRunnable(), 1, 1, null);
            fail();
        } catch (NullPointerException e) {
            assertEquals("unit == null", e.getMessage());
        }
    }

    @Test
    public void workerScheduleOnceInputValidation() {
        Worker worker = scheduler.createWorker();
        try {
            worker.schedule(null);
            fail();
        } catch (NullPointerException e) {
            assertEquals("run == null", e.getMessage());
        }
        try {
            worker.schedule(null, 1, MINUTES);
            fail();
        } catch (NullPointerException e) {
            assertEquals("run == null", e.getMessage());
        }
        try {
            worker.schedule(new CountingRunnable(), 1, null);
            fail();
        } catch (NullPointerException e) {
            assertEquals("unit == null", e.getMessage());
        }
    }

    @Test @Ignore("Implementation delegated to default RxJava implementation")
    public void workerSchedulePeriodicallyInputValidation() {
        Worker worker = scheduler.createWorker();
        try {
            worker.schedulePeriodically(null, 1, 1, MINUTES);
            fail();
        } catch (NullPointerException e) {
            assertEquals("run == null", e.getMessage());
        }
        try {
            worker.schedulePeriodically(new CountingRunnable(), 1, -1, MINUTES);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("period < 0: -1", e.getMessage());
        }
        try {
            worker.schedulePeriodically(new CountingRunnable(), 1, 1, null);
            fail();
        } catch (NullPointerException e) {
            assertEquals("unit == null", e.getMessage());
        }
    }

    private static void idleMainLooper(long amount, TimeUnit unit) {
        // TODO delete this when https://github.com/robolectric/robolectric/pull/2592 is released.
        ShadowLooper.idleMainLooper(unit.toMillis(amount));
    }
}
