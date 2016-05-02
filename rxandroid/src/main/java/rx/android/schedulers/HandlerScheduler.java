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

/**
 * A {@link Scheduler} backed by a {@link Handler}.
 *
 * @deprecated Use {@link AndroidSchedulers#from(Looper)}.
 */
@Deprecated
public final class HandlerScheduler extends LooperScheduler {
    /**
     * Create a {@link Scheduler} which uses {@code handler} to execute actions.
     *
     * @deprecated Use {@link AndroidSchedulers#from(Looper)}.
     */
    @Deprecated
    public static HandlerScheduler from(Handler handler) {
        if (handler == null) throw new NullPointerException("handler == null");
        return new HandlerScheduler(handler);
    }

    private HandlerScheduler(Handler handler) {
        super(handler);
    }
}
