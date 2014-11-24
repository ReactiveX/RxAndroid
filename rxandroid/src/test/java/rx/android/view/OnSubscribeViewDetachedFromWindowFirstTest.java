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
package rx.android.view;

import android.view.View;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.robolectric.RobolectricTestRunner;

import rx.Observable;
import rx.Subscriber;
import rx.observers.TestSubscriber;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class OnSubscribeViewDetachedFromWindowFirstTest {

    @Test
    public void testGivenSubscriptionWhenViewDetachedThenUnsubscribesAndRemovesListener() {
        final Subscriber<View> subscriber = spy(new TestSubscriber<View>());
        final View view = mock(View.class);
        final Observable<View> observable = Observable.create(new OnSubscribeViewDetachedFromWindowFirst(view));
        observable.subscribe(subscriber);

        verify(subscriber, never()).onNext(view);
        verify(subscriber, never()).onError(Matchers.any(Throwable.class));
        verify(subscriber, never()).onCompleted();

        final ArgumentCaptor<View.OnAttachStateChangeListener> captor =
            ArgumentCaptor.forClass(View.OnAttachStateChangeListener.class);
        verify(view).addOnAttachStateChangeListener(captor.capture());

        final View.OnAttachStateChangeListener listener = captor.getValue();
        Assert.assertNotNull("Should have added listener on subscription.", listener);

        listener.onViewDetachedFromWindow(view);

        verify(subscriber, never()).onError(Matchers.any(Throwable.class));
        verify(subscriber).onNext(view);
        verify(subscriber).onCompleted();

        verify(view).removeOnAttachStateChangeListener(listener);
    }

    @Test
    public void testGivenSubscriptionWhenUnsubscribedThenStateListenerRemoved() {
        final Subscriber<View> subscriber = spy(new TestSubscriber<View>());
        final View view = mock(View.class);
        final Observable<View> observable = Observable.create(new OnSubscribeViewDetachedFromWindowFirst(view));
        observable.subscribe(subscriber).unsubscribe();

        verify(subscriber, never()).onNext(view);
        verify(subscriber, never()).onError(Matchers.any(Throwable.class));
        verify(subscriber, never()).onCompleted();

        final ArgumentCaptor<View.OnAttachStateChangeListener> captor =
            ArgumentCaptor.forClass(View.OnAttachStateChangeListener.class);
        verify(view).addOnAttachStateChangeListener(captor.capture());

        final View.OnAttachStateChangeListener listener = captor.getValue();
        Assert.assertNotNull("Should have added listener on subscription.", listener);

        verify(view).removeOnAttachStateChangeListener(listener);
    }
}
