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

import android.os.Looper;
import java.util.concurrent.atomic.AtomicBoolean;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;

/**
 * A {@linkplain Subscription subscription} which ensures its {@linkplain #onUnsubscribe()
 * unsubscribe action} is executed on the main thread. When unsubscription occurs on a different
 * thread than the main thread, the action is posted to run on the main thread as soon as possible.
 * <p>
 * Instances of this class are useful in creating observables which interact with APIs that can
 * only be used on the main thread, such as UI objects.
 * <p>
 * A {@link #verifyMainThread() convenience method} is also provided for validating whether code
 * is being called on the main thread. Calls to this method along with instances of this class are
 * commonly used when creating custom observables using the following pattern:
 * <pre>{@code
 * &#064;Override public void call(Subscriber<? extends T> subscriber) {
 *   MainThreadSubscription.verifyMainThread();
 *
 *   // TODO set up behavior
 *
 *   subscriber.add(new MainThreadSubscription() {
 *     &#064;Override public void onUnsubscribe() {
 *       // TODO undo behavior
 *     }
 *   });
 * }
 * }</pre>
 */
public abstract class MainThreadSubscription implements Subscription {
  /**
   * Verify that the calling thread is the Android main thread.
   * <p>
   * Calls to this method are usually preconditions for subscription behavior which instances of
   * this class later undo. See the class documentation for an example.
   *
   * @throws IllegalStateException when called from any other thread.
   */
  public static void verifyMainThread() {
    if (Looper.myLooper() != Looper.getMainLooper()) {
      throw new IllegalStateException(
          "Expected to be called on the main thread but was " + Thread.currentThread().getName());
    }
  }

  private final AtomicBoolean unsubscribed = new AtomicBoolean();

  @Override public final boolean isUnsubscribed() {
    return unsubscribed.get();
  }

  @Override public final void unsubscribe() {
    if (unsubscribed.compareAndSet(false, true)) {
      if (Looper.myLooper() == Looper.getMainLooper()) {
        onUnsubscribe();
      } else {
        AndroidSchedulers.mainThread().createWorker().schedule(new Action0() {
          @Override public void call() {
            onUnsubscribe();
          }
        });
      }
    }
  }

  protected abstract void onUnsubscribe();
}
