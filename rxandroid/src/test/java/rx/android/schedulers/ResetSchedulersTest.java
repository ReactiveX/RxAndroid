package rx.android.schedulers;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import rx.Scheduler;
import rx.android.plugins.RxAndroidPlugins;
import rx.android.plugins.RxAndroidSchedulersHook;
import rx.schedulers.TestScheduler;

import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public final class ResetSchedulersTest {

    @Test
    public void reset() {
        RxAndroidPlugins.getInstance().reset();

        final TestScheduler testScheduler = new TestScheduler();
        RxAndroidPlugins.getInstance().registerSchedulersHook(new RxAndroidSchedulersHook() {
            @Override
            public Scheduler getMainThreadScheduler() {
                return testScheduler;
            }
        });
        AndroidSchedulers.reset();

        assertTrue(AndroidSchedulers.mainThread().equals(testScheduler));

        RxAndroidPlugins.getInstance().reset();
        RxAndroidPlugins.getInstance().registerSchedulersHook(RxAndroidSchedulersHook.getDefaultInstance());
        AndroidSchedulers.reset();

        assertTrue(AndroidSchedulers.mainThread() instanceof LooperScheduler);
    }

}
