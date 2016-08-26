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
package rx.android.plugins;

import rx.Scheduler;
import rx.annotations.Experimental;
import rx.functions.Func1;

/**
 * Utility class that holds hooks for Scheduler.
 * <p>
 * The class features a lockdown state, see {@link #lockdown()} and {@link #isLockdown()}
 * to prevent further changes to the hooks.
 */
@Experimental
public final class RxAndroidHooks {
    /**
     * Prevents changing the hook callbacks when set to true.
     */
    static volatile boolean lockdown;

    static volatile Func1<Scheduler, Scheduler> onMainScheduler;

    private RxAndroidHooks() {
        throw new IllegalStateException("No instances!");
    }

    /**
     * Returns the current main scheduler hook function or null if it is
     * set to the default pass-through.
     * <p>
     * This operation is threadsafe.
     * @return the current hook function
     */
    public static Func1<Scheduler, Scheduler> getOnMainScheduler() {
        return onMainScheduler;
    }

    /**
     * Sets the hook function for returning a scheduler when
     * the AndroidSchedulers.mainThread() is called unless a lockdown is in effect.
     * <p>
     * This operation is threadsafe.
     * <p>
     * Calling with a {@code null} parameter restores the default behavior:
     * the hook returns the same object.
     * @param onMainScheduler the function that receives the original
     * Android main scheduler and should return a scheduler
     */
    public static void setOnMainScheduler(Func1<Scheduler, Scheduler> onMainScheduler) {
        if (lockdown) {
            return;
        }
        RxAndroidHooks.onMainScheduler = onMainScheduler;
    }

    /**
     * Hook to call when the AndroidSchedulers.mainThread() is called.
     * @param scheduler the default Android main scheduler
     * @return the default of alternative scheduler
     */
    public static Scheduler onMainScheduler(Scheduler scheduler) {
        Func1<Scheduler, Scheduler> f = RxAndroidHooks.onMainScheduler;
        if (f != null) {
            return f.call(scheduler);
        }
        return scheduler;
    }

    /**
     * Reset all hook callbacks to those of the current RxAndroidPlugins handlers.
     */
    public static void reset() {
        if (lockdown) {
            return;
        }
        onMainScheduler = null;
    }

    /**
     * Prevents changing the hooks.
     */
    public static void lockdown() {
        lockdown = true;
    }

    /**
     * Returns true if the hooks can no longer be changed.
     */
    public static boolean isLockdown() {
        return lockdown;
    }
}
