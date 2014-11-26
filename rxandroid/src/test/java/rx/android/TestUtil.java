/**
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

import android.view.View;

import org.robolectric.Robolectric;
import org.robolectric.util.Scheduler;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import rx.Observable;
import rx.Subscriber;

public class TestUtil {
    private TestUtil() {
        throw new AssertionError("No instances");
    }

    static public final String STRING_EXPECTATION = "Hello";

    public static View createView() {
        return new View(Robolectric.application);
    }

    public static Observable<String> atBackgroundThread(final CountDownLatch done) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(final Subscriber<? super String> subscriber) {
                Executors.newSingleThreadExecutor().submit(new Runnable() {
                    @Override
                    public void run() {
                        subscriber.onNext(STRING_EXPECTATION);
                        subscriber.onCompleted();
                        done.countDown();
                    }
                });
            }
        });
    }

}
