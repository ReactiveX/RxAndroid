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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import rx.Scheduler;
import rx.android.plugins.RxAndroidPlugins;
import rx.android.plugins.RxAndroidSchedulersHook;

import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

public class AndroidSchedulersTest {

    @Before @After
    public void setUpAndTearDown() {
        RxAndroidPlugins.getInstance().reset();
    }

    @Test
    public void mainThreadCallsThroughToHook() {
        final Scheduler scheduler = mock(Scheduler.class);
        RxAndroidPlugins.getInstance().registerSchedulersHook(new RxAndroidSchedulersHook() {
            @Override public Scheduler getMainThreadScheduler() {
                return scheduler;
            }
        });

        Scheduler mainThread = AndroidSchedulers.mainThread();
        assertSame(scheduler, mainThread);
    }
}
