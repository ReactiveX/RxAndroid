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
package rx.android.subscribers;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.ref.WeakReference;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import rx.Observable;
import rx.Producer;
import rx.Subscriber;
import rx.Subscription;
import rx.android.internal.subscribers.ReferenceSubscriber;
import rx.android.internal.subscribers.StringSubscriber;
import rx.functions.Action0;
import rx.functions.Action1;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@SuppressWarnings("unchecked")
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class NullingSubscriberTest {

    private final AtomicBoolean onCompleted = new AtomicBoolean(false);
    private final AtomicReference<String> onNext = new AtomicReference<String>(null);
    private final AtomicReference<Throwable> onError = new AtomicReference<Throwable>(null);
    private ExecutorService threadpoolExecutor;

    @Before
    public void setup() {
        threadpoolExecutor = Executors.newCachedThreadPool();
        onCompleted.set(false);
        onNext.set(null);
        onError.set(null);
    }

    @After
    public void teardown() {
        threadpoolExecutor.shutdown();
    }

    @Test
    public void testNormalSubscriberShouldLeakSubscriberInLeakyOnSubcribe() throws Exception {
        doLeakyTest(false);
    }

    @Test
    public void testShouldNotLeakSubsriberInLeakyOnSubscribe() throws Exception {
        doLeakyTest(true);
    }

    @Test
    public void testShouldReceiveDataOnlyWhenSubscribed() throws Exception {
        final String value = "snowcones";
        Subscriber<String> subscriber = spy(new StringSubscriber());
        NullingSubscriber<String> nullingSubscriber = NullingSubscriber.create(subscriber);

        // test onNext called
        nullingSubscriber.onNext(value);
        ArgumentCaptor<String> onNext = ArgumentCaptor.forClass(String.class);
        verify(subscriber).onNext(onNext.capture());
        Assert.assertEquals(value, onNext.getValue());

        // test onCompleted called
        nullingSubscriber.onCompleted();
        verify(subscriber).onCompleted();

        // test onError called
        nullingSubscriber.onError(null);
        ArgumentCaptor<Throwable> onError = ArgumentCaptor.forClass(Throwable.class);
        verify(subscriber).onError(onError.capture());
        Assert.assertEquals(null, onError.getValue());

        // test onStart called
        nullingSubscriber.onStart();
        verify(subscriber).onStart();

        // test setProducer was called
        nullingSubscriber.setProducer(mock(Producer.class));
        verify(subscriber, times(1)).setProducer((Producer) any());

        // test unsubscribe called
        nullingSubscriber.unsubscribe();
        // We can't verify unsubscribe is called because it's final
//        verify(subscriber).unsubscribe();
        Assert.assertEquals(true, subscriber.isUnsubscribed());

        // call methods now that unsubscribed
        nullingSubscriber.onNext(value);
        nullingSubscriber.onCompleted();
        nullingSubscriber.setProducer(mock(Producer.class));
        nullingSubscriber.onNext("hello");
        nullingSubscriber.onError(mock(Throwable.class));
        nullingSubscriber.onStart();

        // make sure subscriber had no more method calls
        verifyNoMoreInteractions(subscriber);
    }

    @Test
    public void testShouldReceiveOnNextOnlyWhenSubscribed() {
        String value = "test";
        Action1<String> onNext = (Action1<String>) mock(Action1.class);
        NullingSubscriber<String> nullingSubscriber = NullingSubscriber.create(onNext);

        // verify onNext is called twice
        nullingSubscriber.onNext(value);
        nullingSubscriber.onNext("test2");
        verify(onNext, times(2)).call(ArgumentCaptor.forClass(String.class).capture());

        // verify no more calls to onNext after unsubscribe
        nullingSubscriber.unsubscribe();
        nullingSubscriber.onNext(value);
        nullingSubscriber.onNext(null);
        nullingSubscriber.onNext("say");
        verifyNoMoreInteractions(onNext);
    }

    @Test
    public void testShouldReceiveOnNextAndOnErrorOnlyWhenSubscribed() {
        String value = "test";
        Action1<String> onNext = (Action1<String>) mock(Action1.class);
        Action1<Throwable> onError = (Action1<Throwable>) mock(Action1.class);
        NullingSubscriber<String> nullingSubscriber = NullingSubscriber.create(onNext, onError);

        // verify onNext is called twice
        nullingSubscriber.onNext(value);
        nullingSubscriber.onNext("test2");
        verify(onNext, times(2)).call(ArgumentCaptor.forClass(String.class).capture());

        // verify onError is called
        nullingSubscriber.onError(mock(Throwable.class));
        verify(onError).call(ArgumentCaptor.forClass(Throwable.class).capture());

        nullingSubscriber.unsubscribe();

        // verify no more calls to onNext after unsubscribe
        nullingSubscriber.onNext(value);
        nullingSubscriber.onNext(null);
        nullingSubscriber.onNext("say");
        verifyNoMoreInteractions(onNext);

        // verify no more calls to onError after unsubscribe
        nullingSubscriber.onError(null);
        nullingSubscriber.onError(mock(Throwable.class));
        verifyNoMoreInteractions(onError);
    }

    @Test
    public void testShouldReceiveAllActionsOnlyWhenSubscribed() {
        String value = "test";
        Action1<String> onNext = (Action1<String>) mock(Action1.class);
        Action1<Throwable> onError = (Action1<Throwable>) mock(Action1.class);
        Action0 onCompleted = mock(Action0.class);
        NullingSubscriber<String> nullingSubscriber = NullingSubscriber.create(onNext, onError,
                onCompleted);

        // verify onNext is called twice
        nullingSubscriber.onNext(value);
        nullingSubscriber.onNext("test2");
        verify(onNext, times(2)).call(ArgumentCaptor.forClass(String.class).capture());

        // verify onError is called
        nullingSubscriber.onError(mock(Throwable.class));
        verify(onError).call(ArgumentCaptor.forClass(Throwable.class).capture());

        // verify onCompleted is called
        nullingSubscriber.onCompleted();
        verify(onCompleted).call();

        nullingSubscriber.unsubscribe();

        // verify no more calls to onNext after unsubscribe
        nullingSubscriber.onNext(value);
        nullingSubscriber.onNext(null);
        nullingSubscriber.onNext("say");
        verifyNoMoreInteractions(onNext);

        // verify no more calls to onError after unsubscribe
        nullingSubscriber.onError(null);
        nullingSubscriber.onError(mock(Throwable.class));
        verifyNoMoreInteractions(onError);

        // verify no more calls to onCompleted after unsubscribe
        nullingSubscriber.onCompleted();
        nullingSubscriber.onCompleted();
        nullingSubscriber.onCompleted();
        verifyNoMoreInteractions(onCompleted);
    }

    private void doLeakyTest(boolean isNulling) throws Exception {
        final String tag = isNulling ? "nulling" : "not nulling";
        final String value = "snowcones";
        final long timeoutMillis = TimeUnit.SECONDS.toMillis(20);
        Observable<String> observable = Observable.create(
                new LeakyOnSubscribe<String>(threadpoolExecutor, timeoutMillis, value));

        // GC early, before test
        Runtime.getRuntime().gc();
        Subscriber<String> subscriber = new ReferenceSubscriber<String>(onNext, onError,
                onCompleted);

        // Create weak reference
        WeakReference<Subscriber<String>> subscriberRef
                = new WeakReference<Subscriber<String>>(subscriber);

        long time = System.nanoTime();

        // startSubscribing to observable
        Subscription subscription;
        if (isNulling) {
            subscription = observable.subscribe(NullingSubscriber.create(subscriber));
        } else {
            subscription = observable.subscribe(subscriber);
        }

        subscription.unsubscribe();
        subscription = null;
        subscriber = null;

        // GC after unsubscribed to clear references
        Runtime.getRuntime().gc();

        // assert reference not completed
        Assert.assertFalse(tag, onCompleted.get());
        Assert.assertNull(tag, onNext.get());
        // assert normal subscriber causes a memory leak and nulling subscriber doesn't
        if (isNulling) {
            Assert.assertNull(tag, subscriberRef.get());
        } else {
            Assert.assertNotNull(tag, subscriberRef.get());
        }

        // sleep a few microts
        long elapsed = System.nanoTime() - time;
        Thread.sleep(timeoutMillis / 2 - TimeUnit.NANOSECONDS.toMillis(elapsed));

        // GC after unsubscribed again
        Runtime.getRuntime().gc();

        // assert reference not completed
        Assert.assertFalse(tag, onCompleted.get());
        Assert.assertNull(tag, onNext.get());
        // assert normal subscriber causes a memory leak and nulling subscriber doesn't
        if (isNulling) {
            Assert.assertNull(tag, subscriberRef.get());
        } else {
            Assert.assertNotNull(tag, subscriberRef.get());
        }

        // sleep way more
        elapsed = System.nanoTime() - time;
        Thread.sleep(timeoutMillis + 1000 - TimeUnit.NANOSECONDS.toMillis(elapsed));

        // GC one last time
        Runtime.getRuntime().gc();

        if (isNulling) {
            // assert nulling subscriber not called
            Assert.assertFalse(tag, onCompleted.get());
            Assert.assertNull(tag, onNext.get());
        } else {
            // assert normal subscriber called
            Assert.assertTrue(tag, onCompleted.get());
            Assert.assertEquals(tag, value, onNext.get());
        }
        // assert both subscriber references are gone by now
        Assert.assertNull(tag, subscriberRef.get());
    }

    private static final class LeakyOnSubscribe<T> implements Observable.OnSubscribe<T> {
        private final Executor executor;
        private final long timeoutMillis;
        private final T data;

        private LeakyOnSubscribe(Executor executor, long timeoutMillis, T data) {
            this.executor = executor;
            this.timeoutMillis = timeoutMillis;
            this.data = data;
        }

        @Override public void call(final Subscriber<? super T> subscriber) {
            Runnable runnable = new Runnable() {
                @Override public void run() {
                    try {
                        Thread.sleep(timeoutMillis);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    subscriber.onNext(data);
                    subscriber.onCompleted();
                }
            };
            executor.execute(runnable);
        }
    }
}
