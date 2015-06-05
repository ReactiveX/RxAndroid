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

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

import java.util.Arrays;

import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import rx.*;
import rx.observers.TestSubscriber;
import rx.subjects.PublishSubject;

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

    @Test
    public void testUntilFires() {
        PublishSubject<Integer> source = PublishSubject.create();
        PublishSubject<Integer> until = PublishSubject.create();
        
        TestSubscriber<Integer> ts = new TestSubscriber<Integer>();
        
        source.lift(new OperatorSubscribeUntil<Integer, Integer>(until)).unsafeSubscribe(ts);

        assertTrue(source.hasObservers());
        assertTrue(until.hasObservers());

        source.onNext(1);
        
        ts.assertReceivedOnNext(Arrays.asList(1));
        until.onNext(1);
        
        ts.assertReceivedOnNext(Arrays.asList(1));
        ts.assertNoErrors();
        
        assertFalse("Source still has observers", source.hasObservers());
        assertFalse("Until still has observers", until.hasObservers());
        // FIXME: operator pitfalls: don't unsubscribe the downstream
        assertTrue("TestSubscriber is unsubscribed", ts.isUnsubscribed());
    }
    @Test
    public void testMainCompletes() {
        PublishSubject<Integer> source = PublishSubject.create();
        PublishSubject<Integer> until = PublishSubject.create();
        
        TestSubscriber<Integer> ts = new TestSubscriber<Integer>();
        
        source.lift(new OperatorSubscribeUntil<Integer, Integer>(until)).unsafeSubscribe(ts);

        assertTrue(source.hasObservers());
        assertTrue(until.hasObservers());

        source.onNext(1);
        source.onCompleted();
        
        ts.assertReceivedOnNext(Arrays.asList(1));
        ts.assertNoErrors();
        ts.assertTerminalEvent();
        
        assertFalse("Source still has observers", source.hasObservers());
        assertFalse("Until still has observers", until.hasObservers());
        
        // FIXME: operator pitfalls: don't unsubscribe the downstream
        assertTrue("TestSubscriber is unsubscribed", ts.isUnsubscribed());
    }
    @Test
    public void testDownstreamUnsubscribes() {
        PublishSubject<Integer> source = PublishSubject.create();
        PublishSubject<Integer> until = PublishSubject.create();
        
        TestSubscriber<Integer> ts = new TestSubscriber<Integer>();
        
        source.lift(new OperatorSubscribeUntil<Integer, Integer>(until)).take(1).unsafeSubscribe(ts);

        assertTrue(source.hasObservers());
        assertTrue(until.hasObservers());

        source.onNext(1);
        
        ts.assertReceivedOnNext(Arrays.asList(1));
        ts.assertNoErrors();
        ts.assertTerminalEvent();
        
        assertFalse("Source still has observers", source.hasObservers());
        assertFalse("Until still has observers", until.hasObservers());
        assertFalse("TestSubscriber is unsubscribed", ts.isUnsubscribed());
    }
    public void testBackpressure() {
        PublishSubject<Integer> until = PublishSubject.create();
        
        TestSubscriber<Integer> ts = new TestSubscriber<Integer>() {
            @Override
            public void onStart() {
                requestMore(0);
            }
        };
        
        Observable.range(1, 10).lift(new OperatorSubscribeUntil<Integer, Integer>(until))
        .unsafeSubscribe(ts);

        assertTrue(until.hasObservers());

        ts.requestMore(1);
        
        ts.assertReceivedOnNext(Arrays.asList(1));
        ts.assertNoErrors();
        assertTrue("TestSubscriber completed", ts.getOnCompletedEvents().isEmpty());
        
        assertFalse("Until still has observers", until.hasObservers());
        assertFalse("TestSubscriber is unsubscribed", ts.isUnsubscribed());
    }
}
