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

/**
 * Registry for plugin implementations that allows global override and handles the retrieval of
 * correct implementation based on order of precedence:
 * <ol>
 * <li>plugin registered globally via {@code register} methods in this class</li>
 * <li>plugin registered and retrieved using {@link System#getProperty(String)} (see get methods
 * for property names)</li>
 * <li>default implementation</li>
 * </ol>
 */
public final class RxAndroidPlugins {
    private static final RxAndroidPlugins INSTANCE = new RxAndroidPlugins();

    public static RxAndroidPlugins getInstance() {
        return INSTANCE;
    }

    private final AtomicReference<RxAndroidSchedulersHook> schedulersHook =
            new AtomicReference<RxAndroidSchedulersHook>();

    RxAndroidPlugins() {
    }

    void reset() {
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
            // Check for an implementation from System.getProperty first.
            RxAndroidSchedulersHook impl =
                    getPluginImplementationViaProperty(RxAndroidSchedulersHook.class);
            if (impl == null) {
                // Nothing set via properties so initialize with default.
                schedulersHook.compareAndSet(null, RxAndroidSchedulersHook.getDefaultInstance());
                // We don't return from here but call get() again in case of thread-race so the winner will
                // always get returned.
            } else {
                // We received an implementation from the system property so use it.
                schedulersHook.compareAndSet(null, impl);
            }
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

    @SuppressWarnings("unchecked") // Burden of correctness is on the property setter.
    private static <T> T getPluginImplementationViaProperty(Class<T> pluginClass) {
        String classSimpleName = pluginClass.getSimpleName();
        // Check system properties for plugin class. This will only happen during system startup thus
        // it's okay to use the synchronized System.getProperties as it will never get called in
        // normal operations.
        String implementingClass =
                System.getProperty("rxandroid.plugin." + classSimpleName + ".implementation");
        if (implementingClass != null) {
            try {
                Class<?> cls = Class.forName(implementingClass);
                // narrow the scope (cast) to the type we're expecting
                cls = cls.asSubclass(pluginClass);
                return (T) cls.newInstance();
            } catch (ClassCastException e) {
                throw new RuntimeException(classSimpleName
                        + " implementation is not an instance of "
                        + classSimpleName
                        + ": "
                        + implementingClass);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(classSimpleName
                        + " implementation class not found: "
                        + implementingClass, e);
            } catch (InstantiationException e) {
              throw new RuntimeException(classSimpleName
                      + " implementation not able to be instantiated: "
                      + implementingClass, e);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(classSimpleName
                        + " implementation not able to be accessed: "
                        + implementingClass, e);
            }
        } else {
            return null;
        }
    }
}
