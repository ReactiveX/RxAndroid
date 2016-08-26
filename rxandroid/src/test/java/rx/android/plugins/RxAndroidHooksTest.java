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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.testutil.EmptyScheduler;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

import static junit.framework.Assert.assertNotSame;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertSame;

public final class RxAndroidHooksTest {

    @Before @After
    public void setUpAndTearDown() {
        RxAndroidHooks.reset();
        AndroidSchedulers.reset();
        RxAndroidPlugins.getInstance().reset();
    }

    @Test
    public void overrideMainScheduler() {
        final Scheduler emptyScheduler = new EmptyScheduler();
        RxAndroidPlugins.getInstance().registerSchedulersHook(new RxAndroidSchedulersHook() {
            @Override
            public Scheduler getMainThreadScheduler() {
                return emptyScheduler;
            }
        });

        try {
            RxAndroidHooks.setOnMainScheduler(new Func1<Scheduler, Scheduler>() {
                @Override
                public Scheduler call(Scheduler scheduler) {
                    return Schedulers.immediate();
                }
            });
            assertSame(Schedulers.immediate(), AndroidSchedulers.mainThread());
        } finally {
            RxAndroidHooks.reset();
        }

        // make sure the reset worked
        assertNotSame(Schedulers.immediate(), AndroidSchedulers.mainThread());
    }

    @Test
    public void lockdown() {
        RxAndroidHooks.lockdown();

        assertTrue(RxAndroidHooks.isLockdown());

        final Scheduler emptyScheduler = new EmptyScheduler();
        RxAndroidPlugins.getInstance().registerSchedulersHook(new RxAndroidSchedulersHook() {
            @Override
            public Scheduler getMainThreadScheduler() {
                return emptyScheduler;
            }
        });

        try {
            RxAndroidHooks.setOnMainScheduler(new Func1<Scheduler, Scheduler>() {
                @Override
                public Scheduler call(Scheduler scheduler) {
                    return Schedulers.immediate();
                }
            });
            assertSame(emptyScheduler, AndroidSchedulers.mainThread());
        } finally {
            RxAndroidHooks.reset();
        }
    }
}
