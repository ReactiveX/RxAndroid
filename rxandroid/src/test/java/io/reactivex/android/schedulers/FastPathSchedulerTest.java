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
package io.reactivex.android.schedulers;

import android.os.Handler;
import android.os.Looper;
import com.google.common.util.concurrent.Runnables;
import io.reactivex.Scheduler;
import io.reactivex.android.schedulers.FastPathScheduler.ScheduledRunnable;
import io.reactivex.android.testutil.CountingRunnable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.assertj.core.api.Condition;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowMessageQueue;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class FastPathSchedulerTest {

  private final ThreadCheck threadCheck = new ThreadCheck();
  private final Handler localHandler = new Handler(Looper.myLooper());
  private final ShadowMessageQueue messageQueue = Shadows.shadowOf(Looper.myQueue());
  private final Scheduler scheduler = new FastPathScheduler(localHandler, threadCheck);

  @Test public void schedule_whenOnTargetThreadAndDelayIsZero_shouldExecuteImmediately() {
    threadCheck.set(true);

    final CountingRunnable result = new CountingRunnable();
    scheduler.createWorker().schedule(result);

    assertThat(result.get()).isEqualTo(1);
  }

  @Test public void schedule_whenOnTargetThreadAndDelayIsNonZero_shouldSchedule() {
    threadCheck.set(true);

    final Runnable action = new Runnable() {
      @Override public void run() {
      }
    };
    scheduler.createWorker().schedule(action, 1, TimeUnit.MILLISECONDS);

    assertThat(messageQueue.getHead().getCallback()).isNotNull()
        .isInstanceOf(ScheduledRunnable.class)
        .is(new Condition<Runnable>() {
          @Override public boolean matches(Runnable value) {
            return ((ScheduledRunnable) value).getDelegate() == action;
          }
        });
  }

  @Test public void schedule_whenOnDifferentThreadAndDelayIsZero_shouldSchedule() {
    threadCheck.set(false);

    scheduler.createWorker().schedule(Runnables.doNothing());

    assertThat(messageQueue.getHead()).isNull();
  }

  @Test public void scheduleDirect_whenOnTargetThreadAndDelayIsZero_shouldExecuteImmediately() {
    threadCheck.set(true);

    final CountingRunnable result = new CountingRunnable();
    scheduler.scheduleDirect(result);

    assertThat(result.get()).isEqualTo(1);
  }

  @Test public void scheduleDirect_whenOnTargetThreadAndDelayIsNonZero_shouldSchedule() {
    threadCheck.set(true);

    final Runnable action = new Runnable() {
      @Override public void run() {
      }
    };
    scheduler.scheduleDirect(action, 1, TimeUnit.MILLISECONDS);

    assertThat(messageQueue.getHead().getCallback()).isNotNull()
        .isInstanceOf(ScheduledRunnable.class)
        .is(new Condition<Runnable>() {
          @Override public boolean matches(Runnable value) {
            return ((ScheduledRunnable) value).getDelegate() == action;
          }
        });
  }

  @Test public void scheduleDirect_whenOnDifferentThreadAndDelayIsZero_shouldSchedule() {
    threadCheck.set(false);
    scheduler.scheduleDirect(Runnables.doNothing());
    assertThat(messageQueue.getHead()).isNull();
  }

  private static class ThreadCheck extends AtomicBoolean
      implements FastPathScheduler.ThreadChecker {

    @Override public boolean isCurrentThread(Thread thread) {
      return get();
    }
  }
}
