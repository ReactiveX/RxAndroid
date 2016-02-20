package rx.android.schedulers.background;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Thread factory which creates threads with lowered priority ({@link Thread#NORM_PRIORITY} - 1)
 */
public final class BackgroundThreadFactory extends AtomicLong implements ThreadFactory {
    final String prefix;
    final static int BACKGROUND_THREAD_PRIORITY = Thread.NORM_PRIORITY - 1;

    public BackgroundThreadFactory(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(r, prefix + incrementAndGet());
        if (t.getPriority() != BACKGROUND_THREAD_PRIORITY) {
            t.setPriority(BACKGROUND_THREAD_PRIORITY);
        }
        t.setDaemon(true);
        return t;
    }
}
