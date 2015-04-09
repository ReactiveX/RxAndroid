package rx.android.plugins;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public final class RxAndroidPluginsTest {
    /** Reset plugins. Used by other tests which need to register hooks. */
    public static void resetPlugins() {
        RxAndroidPlugins.getInstance().reset();
    }

    @Before @After
    public void setUpAndTearDown() {
        resetPlugins();
    }

    @Test
    public void registeredSchedulersHookIsUsed() {
        RxAndroidPlugins plugins = new RxAndroidPlugins();
        RxAndroidSchedulersHook hook = new RxAndroidSchedulersHook();
        plugins.registerSchedulersHook(hook);
        assertSame(hook, plugins.getSchedulersHook());
    }

    @Test
    public void registerSchedulersHookViaSystemProperty() {
        System.setProperty("rxandroid.plugin.RxAndroidSchedulersHook.implementation",
                "rx.android.plugins.RxAndroidPluginsTest$CustomHook");
        assertEquals(CustomHook.class,
                RxAndroidPlugins.getInstance().getSchedulersHook().getClass());
    }

    static class CustomHook extends RxAndroidSchedulersHook {
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
