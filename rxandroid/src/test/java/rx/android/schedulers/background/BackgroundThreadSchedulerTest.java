package rx.android.schedulers.background;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import rx.Scheduler.Worker;
import rx.android.plugins.RxAndroidPlugins;
import rx.functions.Action0;
import rx.plugins.RxJavaPlugins;
import rx.plugins.RxJavaSchedulersHook;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class BackgroundThreadSchedulerTest {

    @Before @After
    public void setUpAndTearDown() {
        RxAndroidPlugins.getInstance().reset();
    }

    @Test
    public void newInstanceWorks() {
        BackgroundThreadScheduler scheduler = BackgroundThreadScheduler.newInstance();
        assertNotNull(scheduler);
    }

    @Test
    public void testScheduledThroughHook() throws InterruptedException {
        Action0 action = mock(Action0.class);
        final AtomicReference<Action0> ref = new AtomicReference<>();

        RxJavaPlugins.getInstance().registerSchedulersHook(new RxJavaSchedulersHook() {
            @Override public Action0 onSchedule(Action0 action) {
                ref.set(action);
                return action;
            }
        });

        BackgroundThreadScheduler scheduler = BackgroundThreadScheduler.newInstance();
        Worker worker = scheduler.createWorker();
        worker.schedule(action);

        assertSame(action, ref.get());
    }

    @Test
    public void testActionIsCalled() throws InterruptedException {
        BackgroundThreadScheduler scheduler = BackgroundThreadScheduler.newInstance();
        Worker worker = scheduler.createWorker();
        final CountDownLatch latch = new CountDownLatch(1);
        final AtomicBoolean isCalled = new AtomicBoolean(false);
        Action0 action = new Action0() {
            @Override public void call() {
                isCalled.set(true);
                latch.countDown();
            }
        };

        worker.schedule(action);

        latch.await(1, TimeUnit.SECONDS);
        assertTrue(isCalled.get());
    }

    @Test
    public void testThreadIsDifferent() throws InterruptedException {
        BackgroundThreadScheduler scheduler = BackgroundThreadScheduler.newInstance();
        Worker worker = scheduler.createWorker();

        final AtomicLong backgroundThreadId = new AtomicLong();
        final CountDownLatch latch = new CountDownLatch(1);
        Action0 action = new Action0() {
            @Override public void call() {
                backgroundThreadId.set(Thread.currentThread().getId());
                latch.countDown();
            }
        };
        worker.schedule(action);

        latch.await(1, TimeUnit.SECONDS);
        assertNotEquals(Thread.currentThread().getId(), backgroundThreadId.get());
    }

    @Test
    public void testThreadPriority() throws InterruptedException {
        BackgroundThreadScheduler scheduler = BackgroundThreadScheduler.newInstance();
        Worker worker = scheduler.createWorker();

        final AtomicInteger backgroundThreadPriority = new AtomicInteger();
        final CountDownLatch latch = new CountDownLatch(1);
        Action0 action = new Action0() {
            @Override public void call() {
                backgroundThreadPriority.set(Thread.currentThread().getPriority());
                latch.countDown();
            }
        };
        worker.schedule(action);

        latch.await(1, TimeUnit.SECONDS);
        assertEquals(Thread.NORM_PRIORITY - 1, backgroundThreadPriority.get());
    }
}