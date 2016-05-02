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

import org.junit.Test;

import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public final class RxAndroidPluginsTest {
    @Test
    public void registeredSchedulersHookIsUsed() {
        RxAndroidPlugins plugins = new RxAndroidPlugins();
        RxAndroidSchedulersHook hook = new RxAndroidSchedulersHook();
        plugins.registerSchedulersHook(hook);
        assertSame(hook, plugins.getSchedulersHook());
    }

    @Test
    public void registerSchedulersHookTwiceFails() {
        RxAndroidPlugins plugins = new RxAndroidPlugins();
        RxAndroidSchedulersHook hook = new RxAndroidSchedulersHook();
        plugins.registerSchedulersHook(hook);
        try {
            plugins.registerSchedulersHook(hook);
            fail();
        } catch (IllegalStateException e) {
            assertTrue(e.getMessage().startsWith("Another strategy was already registered:"));
        }
    }
}
