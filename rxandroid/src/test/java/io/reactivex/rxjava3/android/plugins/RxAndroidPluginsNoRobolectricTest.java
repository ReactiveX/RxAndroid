package io.reactivex.rxjava3.android.plugins;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.android.testutil.EmptyScheduler;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertSame;

public final class RxAndroidPluginsNoRobolectricTest {
    @Before @After
    public void setUpAndTearDown() {
        RxAndroidPlugins.reset();
    }
    
    @Test public void mainThreadSchedulerCanBeReplaced() {
        EmptyScheduler emptyScheduler = new EmptyScheduler();
        RxAndroidPlugins.setMainThreadSchedulerHandler(scheduler -> emptyScheduler);
        assertSame(emptyScheduler, AndroidSchedulers.mainThread());
    }
}
