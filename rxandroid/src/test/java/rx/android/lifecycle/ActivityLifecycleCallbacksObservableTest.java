package rx.android.lifecycle;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.util.ActivityController;

import rx.android.lifecycle.scenarios.BaseLifecycleActivity;
import rx.android.lifecycle.scenarios.BindActivityRule;
import rx.android.lifecycle.scenarios.BindOnCreateEventActivity;
import rx.android.lifecycle.scenarios.BindOnPauseEventActivity;
import rx.android.lifecycle.scenarios.BindOnResumeEventActivity;
import rx.android.lifecycle.scenarios.BindOnStartEventActivity;
import rx.android.lifecycle.scenarios.BindOnStopEventActivity;
import rx.android.lifecycle.scenarios.UseActivity;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ActivityLifecycleCallbacksObservableTest {
    @Rule
    public BindActivityRule bindActivityRule = new BindActivityRule();

    @UseActivity(BindOnCreateEventActivity.class)
    @Test
    public void testBindOnCreate() {
        assertNotNull(bindActivityRule.activity.getSubscription());
        assertFalse(bindActivityRule.activity.getSubscription().isUnsubscribed());

        bindActivityRule.controller.start();
        assertFalse(bindActivityRule.activity.getSubscription().isUnsubscribed());

        bindActivityRule.controller.resume();
        assertFalse(bindActivityRule.activity.getSubscription().isUnsubscribed());

        bindActivityRule.controller.pause();
        assertFalse(bindActivityRule.activity.getSubscription().isUnsubscribed());

        bindActivityRule.controller.stop();
        assertFalse(bindActivityRule.activity.getSubscription().isUnsubscribed());

        // We will unsubscribe onDestroy, but the current version of Robolectric doesn't emit this event
        // correctly  see https://github.com/robolectric/robolectric/issues/1429 (has been fixed on Snapshot BTW)
        // FIXME update this test once Robolectric bug https://github.com/robolectric/robolectric/issues/1429
        // once the fix (currently in robolectric:3.0-SNAPSHOT) will be released
        //bindActivityRule.controller.destroy();
        //assertTrue(bindActivityRule.activity.getSubscription().isUnsubscribed());
    }


    @UseActivity(BindOnStartEventActivity.class)
    @Test
    public void testBindOnStart() {
        assertNull(bindActivityRule.activity.getSubscription());

        bindActivityRule.controller.start();
        assertNotNull(bindActivityRule.activity.getSubscription());
        assertFalse(bindActivityRule.activity.getSubscription().isUnsubscribed());

        bindActivityRule.controller.resume();
        assertFalse(bindActivityRule.activity.getSubscription().isUnsubscribed());

        bindActivityRule.controller.pause();
        assertFalse(bindActivityRule.activity.getSubscription().isUnsubscribed());

        bindActivityRule.controller.stop();
        assertTrue(bindActivityRule.activity.getSubscription().isUnsubscribed());

        bindActivityRule.controller.destroy();
        assertTrue(bindActivityRule.activity.getSubscription().isUnsubscribed());
    }

    @UseActivity(BindOnResumeEventActivity.class)
    @Test
    public void testBindOnResume() {
        assertNull(bindActivityRule.activity.getSubscription());

        bindActivityRule.controller.start();
        assertNull(bindActivityRule.activity.getSubscription());

        bindActivityRule.controller.resume();
        assertNotNull(bindActivityRule.activity.getSubscription());
        assertFalse(bindActivityRule.activity.getSubscription().isUnsubscribed());

        bindActivityRule.controller.pause();
        assertTrue(bindActivityRule.activity.getSubscription().isUnsubscribed());

        bindActivityRule.controller.stop();
        assertTrue(bindActivityRule.activity.getSubscription().isUnsubscribed());

        bindActivityRule.controller.destroy();
        assertTrue(bindActivityRule.activity.getSubscription().isUnsubscribed());
    }

    @UseActivity(BindOnPauseEventActivity.class)
    @Test
    public void testBindOnPause() {
        assertNull(bindActivityRule.activity.getSubscription());

        bindActivityRule.controller.start();
        assertNull(bindActivityRule.activity.getSubscription());

        bindActivityRule.controller.resume();
        assertNull(bindActivityRule.activity.getSubscription());

        bindActivityRule.controller.pause();
        assertFalse(bindActivityRule.activity.getSubscription().isUnsubscribed());

        bindActivityRule.controller.stop();
        assertTrue(bindActivityRule.activity.getSubscription().isUnsubscribed());

        bindActivityRule.controller.destroy();
        assertTrue(bindActivityRule.activity.getSubscription().isUnsubscribed());
    }

    @UseActivity(BindOnStopEventActivity.class)
    @Test
    public void testBindOnStop() {
        assertNull(bindActivityRule.activity.getSubscription());

        bindActivityRule.controller.start();
        assertNull(bindActivityRule.activity.getSubscription());

        bindActivityRule.controller.resume();
        assertNull(bindActivityRule.activity.getSubscription());

        bindActivityRule.controller.pause();
        assertNull(bindActivityRule.activity.getSubscription());

        bindActivityRule.controller.stop();
        assertNotNull(bindActivityRule.activity.getSubscription());
        assertFalse(bindActivityRule.activity.getSubscription().isUnsubscribed());

        // We will unsubscribe onDestroy, but the current version of Robolectric doesn't emit this event
        // correctly  see https://github.com/robolectric/robolectric/issues/1429 (has been fixed on Snapshot BTW)
        // FIXME update this test once Robolectric bug https://github.com/robolectric/robolectric/issues/1429
        // once the fix (currently in robolectric:3.0-SNAPSHOT) will be released
        //bindActivityRule.controller.destroy();
        //assertTrue(bindActivityRule.activity.getSubscription().isUnsubscribed());
    }

    @Test
    public void testMultipleInstances() {
        ActivityController<BindOnStartEventActivity> ctrlBndStart = Robolectric.buildActivity(BindOnStartEventActivity.class);
        BaseLifecycleActivity actBndStart = ctrlBndStart.create().get();

        ActivityController<BindOnResumeEventActivity> ctrlBndResume = Robolectric.buildActivity(BindOnResumeEventActivity.class);
        BaseLifecycleActivity actBndResume = ctrlBndResume.create().get();

        ActivityController<BindOnPauseEventActivity> ctrlBndPause = Robolectric.buildActivity(BindOnPauseEventActivity.class);
        BaseLifecycleActivity actBndPause = ctrlBndPause.create().get();

        //Other instances lifecycle change shouldn't interfere with each other

        ctrlBndStart.start();
        assertNotNull(actBndStart.getSubscription());
        assertFalse(actBndStart.getSubscription().isUnsubscribed());

        ctrlBndStart.resume();
        assertFalse(actBndStart.getSubscription().isUnsubscribed());
        assertNull(actBndResume.getSubscription());

        ctrlBndStart.pause();
        assertNull(actBndResume.getSubscription());
        assertNull(actBndPause.getSubscription());

        ctrlBndStart.stop();
        assertNull(actBndResume.getSubscription());
        assertNull(actBndPause.getSubscription());
        assertTrue(actBndStart.getSubscription().isUnsubscribed());

        ctrlBndResume.start();
        assertTrue(actBndStart.getSubscription().isUnsubscribed());
        assertNull(actBndResume.getSubscription());
        assertNull(actBndPause.getSubscription());

        ctrlBndResume.resume();
        assertNotNull(actBndResume.getSubscription());
        assertFalse(actBndResume.getSubscription().isUnsubscribed());

        ctrlBndResume.pause();
        assertNull(actBndPause.getSubscription());
        assertTrue(actBndResume.getSubscription().isUnsubscribed());

        ctrlBndResume.stop();
        assertNull(actBndPause.getSubscription());
        assertTrue(actBndStart.getSubscription().isUnsubscribed());
        assertTrue(actBndResume.getSubscription().isUnsubscribed());

        ctrlBndPause.start();
        assertNull(actBndPause.getSubscription());
        assertTrue(actBndStart.getSubscription().isUnsubscribed());
        assertTrue(actBndResume.getSubscription().isUnsubscribed());

        ctrlBndPause.resume();
        assertNull(actBndPause.getSubscription());
        assertTrue(actBndStart.getSubscription().isUnsubscribed());
        assertTrue(actBndResume.getSubscription().isUnsubscribed());

        ctrlBndPause.pause();
        assertNotNull(actBndPause.getSubscription());
        assertFalse(actBndPause.getSubscription().isUnsubscribed());
        assertTrue(actBndStart.getSubscription().isUnsubscribed());
        assertTrue(actBndResume.getSubscription().isUnsubscribed());

        ctrlBndPause.stop();
        assertTrue(actBndPause.getSubscription().isUnsubscribed());
        assertTrue(actBndStart.getSubscription().isUnsubscribed());
        assertTrue(actBndResume.getSubscription().isUnsubscribed());

        ctrlBndStart.destroy();
        ctrlBndResume.destroy();
        ctrlBndPause.destroy();
    }
}
