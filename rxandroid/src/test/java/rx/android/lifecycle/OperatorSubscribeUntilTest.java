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

package rx.android.lifecycle;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.subjects.BehaviorSubject;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class OperatorSubscribeUntilTest {

    private Observable<Object> observable;
    private Subscriber<Object> subscriber;

    // RxJava tends to swallow fail(); instead just use a subscriber to tell if onComplete was called
    boolean onCompleteCalled;

    @Before
    public void setup() {
        observable = Observable.never();
        subscriber = new Subscriber<Object>() {
            @Override
            public void onCompleted() {
                onCompleteCalled = true;
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(Object o) {

            }
        };
        onCompleteCalled = false;
    }

    @Test
    public void testDoesNotComplete() {
        observable.lift(new OperatorSubscribeUntil<Object, String>(Observable.just("Single Item")))
                .subscribe(subscriber);
        assertFalse(onCompleteCalled);
    }

}
