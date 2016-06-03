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

import rx.Scheduler;
import rx.android.plugins.RxAndroidPlugins;
import rx.android.plugins.RxAndroidSchedulersHook;
import rx.annotations.Experimental;
import rx.schedulers.Schedulers;

/** Android-specific Schedulers. */
public final class AndroidSchedulers {
    private static AndroidSchedulers INSTANCE = new AndroidSchedulers();

    private final Scheduler mainThreadScheduler;

    private static synchronized AndroidSchedulers getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new AndroidSchedulers();
        }
        return INSTANCE;
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
     * Resets the current {@link Schedulers} instance.
     * <p>
     * This API is experimental. Resetting the schedulers is dangerous
     * during application runtime and also bad code could invoke it in
     * the middle of an application life-cycle and really break applications
     * if not used cautiously.
     */
    @Experimental
    public static void reset() {
        INSTANCE = null;
    }
}
