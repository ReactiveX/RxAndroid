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
import android.os.Message;
import io.reactivex.Scheduler;
import io.reactivex.disposables.Disposable;
import io.reactivex.disposables.Disposables;
import io.reactivex.plugins.RxJavaPlugins;
import java.util.concurrent.TimeUnit;

/**
 * A {@link Scheduler} backed by a {@link Handler}. This scheduler is optimized to call the action
 * directly if immediate and scheduled from the target thread. Because of this, it could operate
 * synchronously.
 */
public final class FastPathScheduler extends Scheduler {

  private final Handler handler;
  private final ThreadChecker threadChecker;

  public FastPathScheduler(Handler handler) {
    this(handler, new ThreadChecker() {
      @Override public boolean isCurrentThread(Thread thread) {
        Looper looper = Looper.myLooper();
        return looper != null && looper.getThread() == thread;
      }
    });
  }

  /**
   * @param handler the target {@link Handler}.
   * @param threadChecker the {@link ThreadChecker} to be provide a {@link Looper}.
   */
  FastPathScheduler(Handler handler, ThreadChecker threadChecker) {
    this.handler = handler;
    this.threadChecker = threadChecker;
  }

  @Override public Disposable scheduleDirect(Runnable run, long delay, TimeUnit unit) {
    if (run == null) {
      throw new NullPointerException("run == null");
    }
    if (unit == null) {
      throw new NullPointerException("unit == null");
    }

    run = RxJavaPlugins.onSchedule(run);
    ScheduledRunnable scheduled = new ScheduledRunnable(handler, run);
    // Fast path if action is immediate and we are on the target thread.
    if (delay <= 0L && threadChecker.isCurrentThread(handler.getLooper().getThread())) {
      scheduled.run();
      return Disposables.disposed();
    } else {
      handler.postDelayed(scheduled, Math.max(0L, unit.toMillis(delay)));
      return scheduled;
    }
  }

  @Override public Worker createWorker() {
    return new HandlerWorker(handler, threadChecker);
  }

  /** Checker to compare threads, used for testing. */
  interface ThreadChecker {

    /**
     * @param thread the thread to check
     * @return {@code true} if the thread is the same as the current, {@code false} if not.
     */
    boolean isCurrentThread(Thread thread);
  }

  private static final class HandlerWorker extends Worker {

    private final Handler handler;
    private final ThreadChecker threadChecker;

    private volatile boolean disposed;

    HandlerWorker(Handler handler, ThreadChecker threadChecker) {
      this.handler = handler;
      this.threadChecker = threadChecker;
    }

    @Override public Disposable schedule(Runnable run, long delay, TimeUnit unit) {
      if (run == null) {
        throw new NullPointerException("run == null");
      }
      if (unit == null) {
        throw new NullPointerException("unit == null");
      }

      if (disposed) {
        return Disposables.disposed();
      }

      run = RxJavaPlugins.onSchedule(run);

      ScheduledRunnable scheduled = new ScheduledRunnable(handler, run);

      Message message = Message.obtain(handler, scheduled);
      // Used as token for batch disposal of this worker's runnables.
      message.obj = this;

      if (delay <= 0L && threadChecker.isCurrentThread(handler.getLooper().getThread())) {
        scheduled.run();
        return Disposables.disposed();
      }
      handler.sendMessageDelayed(message, Math.max(0L, unit.toMillis(delay)));

      // Re-check disposed state for removing in case we were racing a call to dispose().
      if (disposed) {
        handler.removeCallbacks(scheduled);
        return Disposables.disposed();
      }

      return scheduled;
    }

    @Override public void dispose() {
      disposed = true;
      handler.removeCallbacksAndMessages(this /* token */);
    }

    @Override public boolean isDisposed() {
      return disposed;
    }
  }

  /** Scheduled runnable. */
  static final class ScheduledRunnable implements Runnable, Disposable {

    private final Handler handler;
    private final Runnable delegate;

    private volatile boolean disposed;

    ScheduledRunnable(Handler handler, Runnable delegate) {
      this.handler = handler;
      this.delegate = delegate;
    }

    Runnable getDelegate() {
      return delegate;
    }

    @Override public void run() {
      try {
        delegate.run();
      } catch (Throwable t) {
        IllegalStateException ie =
            new IllegalStateException("Fatal Exception thrown on Scheduler.", t);
        RxJavaPlugins.onError(ie);
        Thread thread = Thread.currentThread();
        thread.getUncaughtExceptionHandler().uncaughtException(thread, ie);
      }
    }

    @Override public void dispose() {
      disposed = true;
      handler.removeCallbacks(this);
    }

    @Override public boolean isDisposed() {
      return disposed;
    }
  }
}
