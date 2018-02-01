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

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import io.reactivex.Scheduler;
import io.reactivex.android.plugins.RxAndroidPlugins;
import java.util.concurrent.Callable;

/** Android-specific Schedulers. */
public final class AndroidSchedulers {

    private static final class MainHolder {
        static final Scheduler DEFAULT
            = new HandlerScheduler(new Handler(Looper.getMainLooper()), false);
    }

    private static final Scheduler MAIN_THREAD = RxAndroidPlugins.initMainThreadScheduler(
            new Callable<Scheduler>() {
                @Override public Scheduler call() throws Exception {
                    return MainHolder.DEFAULT;
                }
            });

    /** A {@link Scheduler} which executes actions on the Android main thread. */
    public static Scheduler mainThread() {
        return RxAndroidPlugins.onMainThreadScheduler(MAIN_THREAD);
    }

    /** A {@link Scheduler} which executes actions on {@code looper}. */
    public static Scheduler from(Looper looper) {
        return from(looper, false);
    }

    /**
     * A {@link Scheduler} which executes actions on {@code looper}.
     *
     * @param async if true, the scheduler will use async messaging on API >= 16 to avoid VSYNC
     *              locking. On API < 16 this value is ignored.
     * @see Message#setAsynchronous(boolean)
     */
    @SuppressLint("NewApi") // Checking for an @hide API.
    public static Scheduler from(Looper looper, boolean async) {
        if (looper == null) throw new NullPointerException("looper == null");
        if (Build.VERSION.SDK_INT < 16) {
            async = false;
        } else if (async && Build.VERSION.SDK_INT < 22) {
            // Confirm that the method is available on this API level despite being @hide.
            Message message = Message.obtain();
            try {
                message.setAsynchronous(true);
            } catch (NoSuchMethodError e) {
                async = false;
            }
            message.recycle();
        }
        return new HandlerScheduler(new Handler(looper), async);
    }

    private AndroidSchedulers() {
        throw new AssertionError("No instances.");
    }
}
