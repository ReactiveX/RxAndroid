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
package io.reactivex.android.plugins;

import io.reactivex.Scheduler;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.functions.Function;

/**
 * Utility class to inject handlers to certain standard RxAndroid operations.
 */
public final class RxAndroidPlugins {
    private static volatile Function<Scheduler, Scheduler> onInitMainThreadHandler;
    private static volatile Function<Scheduler, Scheduler> onMainThreadHandler;

    public static void setInitMainThreadSchedulerHandler(Function<Scheduler, Scheduler> handler) {
        onInitMainThreadHandler = handler;
    }

    public static Scheduler initMainThreadScheduler(Scheduler scheduler) {
        Function<Scheduler, Scheduler> f = onInitMainThreadHandler;
        if (f == null) {
            return scheduler;
        }
        try {
            return f.apply(scheduler);
        } catch (Exception e) {
            throw Exceptions.propagate(e);
        }
    }

    public static void setMainThreadSchedulerHandler(Function<Scheduler, Scheduler> handler) {
        onMainThreadHandler = handler;
    }

    public static Scheduler onMainThreadScheduler(Scheduler scheduler) {
        Function<Scheduler, Scheduler> f = onMainThreadHandler;
        if (f == null) {
            return scheduler;
        }
        try {
            return f.apply(scheduler);
        } catch (Exception e) {
            throw Exceptions.propagate(e);
        }
    }

    /**
     * Removes all handlers and resets the default behavior.
     */
    public static void reset() {
        setInitMainThreadSchedulerHandler(null);
        setMainThreadSchedulerHandler(null);
    }

    private RxAndroidPlugins() {
        throw new AssertionError("No instances.");
    }
}
