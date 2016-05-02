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

import java.util.concurrent.atomic.AtomicReference;
import rx.annotations.Experimental;

/**
 * Registry for plugin implementations that allows global override and handles the retrieval of
 * correct implementation based on order of precedence:
 * <ol>
 * <li>plugin registered globally via {@code register} methods in this class</li>
 * <li>default implementation</li>
 * </ol>
 */
public final class RxAndroidPlugins {
    private static final RxAndroidPlugins INSTANCE = new RxAndroidPlugins();

    public static RxAndroidPlugins getInstance() {
        return INSTANCE;
    }

    private final AtomicReference<RxAndroidSchedulersHook> schedulersHook = new AtomicReference<>();

    RxAndroidPlugins() {
    }

    /**
     * Reset any explicit or default-set hooks.
     * <p>
     * Note: This should only be used for testing purposes.
     */
    @Experimental
    public void reset() {
        schedulersHook.set(null);
    }

    /**
     * Retrieves the instance of {@link RxAndroidSchedulersHook} to use based on order of
     * precedence as defined in the {@link RxAndroidPlugins} class header.
     * <p>
     * Override the default by calling {@link #registerSchedulersHook(RxAndroidSchedulersHook)} or by
     * setting the property {@code rxandroid.plugin.RxAndroidSchedulersHook.implementation} with the
     * full classname to load.
     */
    public RxAndroidSchedulersHook getSchedulersHook() {
        if (schedulersHook.get() == null) {
            schedulersHook.compareAndSet(null, RxAndroidSchedulersHook.getDefaultInstance());
            // We don't return from here but call get() again in case of thread-race so the winner will
            // always get returned.
        }
        return schedulersHook.get();
    }

    /**
     * Registers an {@link RxAndroidSchedulersHook} implementation as a global override of any
     * injected or default implementations.
     *
     * @throws IllegalStateException if called more than once or after the default was initialized
     * (if usage occurs before trying to register)
     */
    public void registerSchedulersHook(RxAndroidSchedulersHook impl) {
        if (!schedulersHook.compareAndSet(null, impl)) {
            throw new IllegalStateException(
                    "Another strategy was already registered: " + schedulersHook.get());
        }
    }
}
