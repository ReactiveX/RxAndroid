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

import rx.Scheduler;
import rx.android.plugins.RxAndroidPlugins;
import rx.android.schedulers.background.BackgroundThreadScheduler;
import rx.android.schedulers.handler.HandlerScheduler;

/**
 * Android-specific Schedulers.
 */
public final class AndroidSchedulers {
    private AndroidSchedulers() {
        throw new AssertionError("No instances");
    }

    // See https://github.com/ReactiveX/RxAndroid/issues/238
    // https://en.wikipedia.org/wiki/Initialization-on-demand_holder_idiom
    private static class MainThreadSchedulerHolder {
        static final Scheduler MAIN_THREAD_SCHEDULER =
                HandlerScheduler.from(new Handler(Looper.getMainLooper()));
    }

    private static class BackgroundThreadSchedulerHolder {
        static final Scheduler BACKGROUND_THREAD_SCHEDULER =
                BackgroundThreadScheduler.newInstance();
    }

    /**
     * A {@link Scheduler} which executes actions on the Android UI thread.
     */
    public static Scheduler mainThread() {
        Scheduler scheduler =
                RxAndroidPlugins.getInstance().getSchedulersHook().getMainThreadScheduler();
        return scheduler != null ? scheduler : MainThreadSchedulerHolder.MAIN_THREAD_SCHEDULER;
    }

    /**
     * Creates and returns a {@link Scheduler} that creates a new {@link Thread} for each unit of work.
     * <p>
     * Each thread created by this scheduler will have fixed priority ({@link Thread#NORM_PRIORITY} - 1)
     * which is lower than Android main thread priority.
     * <p>
     * Unhandled errors will be delivered to the scheduler Thread's {@link java.lang.Thread.UncaughtExceptionHandler}.
     *
     * @return a {@link BackgroundThreadScheduler} instance
     */
    public static Scheduler backgroundThread() {
        Scheduler scheduler =
                RxAndroidPlugins.getInstance().getSchedulersHook().getBackgroundThreadScheduler();
        return scheduler != null ? scheduler : BackgroundThreadSchedulerHolder.BACKGROUND_THREAD_SCHEDULER;
    }
}
