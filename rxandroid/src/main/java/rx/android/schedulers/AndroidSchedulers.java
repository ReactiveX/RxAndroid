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

import java.util.concurrent.atomic.AtomicReference;

import rx.Scheduler;
import rx.android.plugins.RxAndroidPlugins;
import rx.android.plugins.RxAndroidSchedulersHook;
import rx.annotations.Experimental;

/** Android-specific Schedulers. */
public final class AndroidSchedulers {
    private static final AtomicReference<AndroidSchedulers> INSTANCE = new AtomicReference<>();

    private final Scheduler mainThreadScheduler;

    private static AndroidSchedulers getInstance() {
        for (;;) {
            AndroidSchedulers current = INSTANCE.get();
            if (current != null) {
                return current;
            }
            current = new AndroidSchedulers();
            if (INSTANCE.compareAndSet(null, current)) {
                return current;
            }
        }
    }

    private AndroidSchedulers() {
        RxAndroidSchedulersHook hook = RxAndroidPlugins.getInstance().getSchedulersHook();

        Scheduler main = hook.getMainThreadScheduler();
        if (main != null) {
            mainThreadScheduler = main;
        } else {
            mainThreadScheduler = new LooperScheduler(Looper.getMainLooper());
        }
    }

    /** A {@link Scheduler} which executes actions on the Android UI thread. */
    public static Scheduler mainThread() {
        return getInstance().mainThreadScheduler;
    }

    /** A {@link Scheduler} which executes actions on {@code looper}. */
    public static Scheduler from(Looper looper) {
        if (looper == null) throw new NullPointerException("looper == null");
        return new LooperScheduler(looper);
    }

    /**
     * Resets the current {@link AndroidSchedulers} instance.
     * This will re-init the cached schedulers on the next usage,
     * which can be useful in testing.
     */
    @Experimental
    public static void reset() {
        INSTANCE.set(null);
    }
}
