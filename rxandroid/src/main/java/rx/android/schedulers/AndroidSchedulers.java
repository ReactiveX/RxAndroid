/**
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

/**
 * Schedulers that have Android-specific functionality
 */
public final class AndroidSchedulers {
    private AndroidSchedulers() {
        throw new AssertionError("No instances");
    }

    private static final Scheduler MAIN_THREAD_SCHEDULER =
            new HandlerThreadScheduler(new Handler(Looper.getMainLooper()));

    /**
     * {@link Scheduler} which uses the provided {@link Handler} to execute actions.
     */
    public static Scheduler handlerThread(final Handler handler) {
        return new HandlerThreadScheduler(handler);
    }

    /**
     * {@link Scheduler} which will execute actions on the Android UI thread.
     */
    public static Scheduler mainThread() {
        return MAIN_THREAD_SCHEDULER;
    }
}
