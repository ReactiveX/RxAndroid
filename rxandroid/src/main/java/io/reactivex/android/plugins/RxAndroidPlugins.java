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

import java.util.concurrent.Callable;

import io.reactivex.Scheduler;
import io.reactivex.exceptions.Exceptions;
import io.reactivex.functions.Function;

/**
 * Utility class to inject handlers to certain standard RxAndroid operations.
 */
public final class RxAndroidPlugins {

    private static volatile Function<Callable<Scheduler>, Scheduler> onInitMainThreadHandler;
    private static volatile Function<Scheduler, Scheduler> onMainThreadHandler;

    public static void setInitMainThreadSchedulerHandler(Function<Callable<Scheduler>, Scheduler> handler) {
        onInitMainThreadHandler = handler;
    }

    public static Scheduler initMainThreadScheduler(Callable<Scheduler> scheduler) {
        if (scheduler == null) {
            throw new NullPointerException("scheduler == null");
        }
        Function<Callable<Scheduler>, Scheduler> f = onInitMainThreadHandler;
        if (f == null) {
            return callRequireNonNull(scheduler);
        }
        return applyRequireNonNull(f, scheduler);
    }

    public static void setMainThreadSchedulerHandler(Function<Scheduler, Scheduler> handler) {
        onMainThreadHandler = handler;
    }

    public static Scheduler onMainThreadScheduler(Scheduler scheduler) {
        if (scheduler == null) {
            throw new NullPointerException("scheduler == null");
        }
        Function<Scheduler, Scheduler> f = onMainThreadHandler;
        if (f == null) {
            return scheduler;
        }
        return apply(f, scheduler);
    }

    /**
     * Returns the current hook function.
     * @return the hook function, may be null
     */
    public static Function<Callable<Scheduler>, Scheduler> getInitMainThreadSchedulerHandler() {
        return onInitMainThreadHandler;
    }

    /**
     * Returns the current hook function.
     * @return the hook function, may be null
     */
    public static Function<Scheduler, Scheduler> getOnMainThreadSchedulerHandler() {
        return onMainThreadHandler;
    }

    /**
     * Removes all handlers and resets the default behavior.
     */
    public static void reset() {
        setInitMainThreadSchedulerHandler(null);
        setMainThreadSchedulerHandler(null);
    }

    static Scheduler callRequireNonNull(Callable<Scheduler> s) {
        try {
            Scheduler scheduler = s.call();
            if (scheduler == null) {
                throw new NullPointerException("Scheduler Callable returned null");
            }
            return scheduler;
        } catch (Throwable ex) {
            throw Exceptions.propagate(ex);
        }
    }

    static Scheduler applyRequireNonNull(Function<Callable<Scheduler>, Scheduler> f, Callable<Scheduler> s) {
        Scheduler scheduler = apply(f,s);
        if (scheduler == null) {
            throw new NullPointerException("Scheduler Callable returned null");
        }
        return scheduler;
    }

    static <T, R> R apply(Function<T, R> f, T t) {
        try {
            return f.apply(t);
        } catch (Throwable ex) {
            throw Exceptions.propagate(ex);
        }
    }

    private RxAndroidPlugins() {
        throw new AssertionError("No instances.");
    }
}
