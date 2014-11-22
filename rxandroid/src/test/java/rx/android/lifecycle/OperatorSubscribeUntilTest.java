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
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.observers.TestSubscriber;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class OperatorSubscribeUntilTest {

    @Spy
    private Subscriber<Object> subscriber = new TestSubscriber<Object>();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testSourceUnsubscribesOnNext() {
        Subscription subscription = Observable.never()
                .lift(new OperatorSubscribeUntil<Object, Object>(Observable.just(new Object())))
                .subscribe(subscriber);

        verify(subscriber, never()).onNext(any());
        assertTrue(subscription.isUnsubscribed());
    }

    @Test
    public void testSourceUnsubscribesOnComplete() {
        Subscription subscription = Observable.never()
                .lift(new OperatorSubscribeUntil<Object, Object>(Observable.empty()))
                .subscribe(subscriber);

        verify(subscriber, never()).onCompleted();
        assertTrue(subscription.isUnsubscribed());
    }

    @Test
    public void testSourceReceivesExceptions() {
        Exception exception = new RuntimeException();
        Subscription subscription = Observable.never()
                .lift(new OperatorSubscribeUntil<Object, String>(Observable.<String>error(exception)))
                .subscribe(subscriber);

        verify(subscriber, atLeastOnce()).onError(exception);
        assertTrue(subscription.isUnsubscribed());
    }

}
