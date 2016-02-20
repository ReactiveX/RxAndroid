package rx.android.schedulers.background;

import rx.Scheduler;
import rx.internal.schedulers.NewThreadWorker;

/**
 * Schedules each unit of work on a new thread. Threads created by this scheduler will
 * have fixed priority ({@link Thread#NORM_PRIORITY} - 1) which is lower than Android
 * main thread priority.
 */
public class BackgroundThreadScheduler extends Scheduler {

    private static final String THREAD_NAME_PREFIX = "RxAndroidBackgroundThreadScheduler-";
    private static final BackgroundThreadFactory THREAD_FACTORY = new BackgroundThreadFactory(THREAD_NAME_PREFIX);

    public static BackgroundThreadScheduler newInstance() {
        return new BackgroundThreadScheduler();
    }

    private BackgroundThreadScheduler() {
    }

    @Override
    public Worker createWorker() {
        return new NewThreadWorker(THREAD_FACTORY);
    }
}
