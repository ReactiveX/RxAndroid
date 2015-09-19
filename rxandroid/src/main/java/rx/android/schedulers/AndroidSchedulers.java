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

import rx.Scheduler;
import android.os.Handler;
import android.os.Looper;
import rx.android.plugins.RxAndroidPlugins;

/** Android-specific Schedulers. */
public final class AndroidSchedulers {
    private AndroidSchedulers() {
        throw new AssertionError("No instances");
    }

    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());
    private static final Scheduler MAIN_THREAD_SCHEDULER =
            new HandlerScheduler(MAIN_HANDLER);
    private static final Scheduler MAIN_THREAD_FAST_PATH_SCHEDULER =
            new FastPathHandlerScheduler(MAIN_HANDLER);

    /** A {@link Scheduler} which executes actions on the Android UI thread. */
    public static Scheduler mainThread() {
        Scheduler scheduler =
                RxAndroidPlugins.getInstance().getSchedulersHook().getMainThreadScheduler();
        return scheduler != null ? scheduler : MAIN_THREAD_SCHEDULER;
    }

    // TODO: Do we need a plugin hook for this one?
    // Also, should it be created with the scheduler returned from mainThread()?
    /**
     * A {@link rx.Scheduler} which executes actions on the Android UI thread. This scheduler could
     * possibly operate synchronously if the action is immediate and is scheduled from the target
     * thread.
     */
    public static Scheduler mainThreadFastPath() {
        return MAIN_THREAD_FAST_PATH_SCHEDULER;
    }
}
