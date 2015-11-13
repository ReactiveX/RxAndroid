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
package rx.android;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public final class MainThreadSubscriptionTest {
  @Test public void verifyDoesNotThrowOnMainThread() throws InterruptedException {
    MainThreadSubscription.verifyMainThread();
    // Robolectric tests run on its main thread.
  }

  @Test public void verifyThrowsOffMainThread() throws InterruptedException {
    final CountDownLatch latch = new CountDownLatch(1);
    new Thread(new Runnable() {
      @Override public void run() {
        try {
          MainThreadSubscription.verifyMainThread();
          fail();
        } catch (IllegalStateException e) {
          assertTrue(e.getMessage().startsWith("Expected to be called on the main thread"));
          latch.countDown();
        }
      }
    }).start();

    assertTrue(latch.await(1, SECONDS));
  }

  @Test public void onUnsubscribeRunsSyncOnMainThread() {
    ShadowLooper.pauseMainLooper();

    final AtomicBoolean called = new AtomicBoolean();
    new MainThreadSubscription() {
      @Override protected void onUnsubscribe() {
        called.set(true);
      }
    }.unsubscribe();

    assertTrue(called.get());
  }

  @Test public void unsubscribeTwiceDoesNotRunTwice() {
    final AtomicInteger called = new AtomicInteger(0);

    MainThreadSubscription subscription = new MainThreadSubscription() {
      @Override protected void onUnsubscribe() {
        called.incrementAndGet();
      }
    };

    subscription.unsubscribe();
    subscription.unsubscribe();
    subscription.unsubscribe();

    assertEquals(1, called.get());
  }

  @Test public void onUnsubscribePostsOffMainThread() throws InterruptedException {
    ShadowLooper.pauseMainLooper();

    final CountDownLatch latch = new CountDownLatch(1);
    final AtomicBoolean called = new AtomicBoolean();
    new Thread(new Runnable() {
      @Override public void run() {
        new MainThreadSubscription() {
          @Override protected void onUnsubscribe() {
            called.set(true);
          }
        }.unsubscribe();
        latch.countDown();
      }
    }).start();

    assertTrue(latch.await(1, SECONDS));
    assertFalse(called.get()); // Callback has not yet run.

    ShadowLooper.runMainLooperOneTask();
    assertTrue(called.get());
  }
}
