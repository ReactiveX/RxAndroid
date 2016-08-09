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

import android.os.Looper;
import io.reactivex.Scheduler;
import io.reactivex.android.plugins.RxAndroidPlugins;
import io.reactivex.android.testutil.EmptyScheduler;
import io.reactivex.functions.Function;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public final class AndroidSchedulersTest {

    @Before @After
    public void setUpAndTearDown() {
        RxAndroidPlugins.reset();
    }

    @Test
    public void mainThreadCallsThroughToHook() {
        final AtomicInteger called = new AtomicInteger();
        final Scheduler newScheduler = new EmptyScheduler();
        RxAndroidPlugins.setMainThreadSchedulerHandler(new Function<Scheduler, Scheduler>() {
            @Override public Scheduler apply(Scheduler scheduler) {
                called.getAndIncrement();
                return newScheduler;
            }
        });

        assertSame(newScheduler, AndroidSchedulers.mainThread());
        assertEquals(1, called.get());

        assertSame(newScheduler, AndroidSchedulers.mainThread());
        assertEquals(2, called.get());
    }

    @Test
    public void fromNullThrows() {
        try {
            AndroidSchedulers.from(null);
            fail();
        } catch (NullPointerException e) {
            assertEquals("looper == null", e.getMessage());
        }
    }

    @Test
    public void fromReturnsUsableScheduler() {
        assertNotNull(AndroidSchedulers.from(Looper.getMainLooper()));
    }
}
