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

import android.app.Activity;
import android.view.View;

import android.widget.LinearLayout;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.observers.TestObserver;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.refEq;
import static org.mockito.Mockito.times;

@RunWith(RobolectricTestRunner.class)
public class OperatorViewFocusChangeTest {
    private static OnFocusChangeEvent mkMockedEvent(final View view, final boolean hasFocus) {
        return refEq(OnFocusChangeEvent.create(view, hasFocus));
    }

    private static View mkFocusableViewWithParent() {
        final Activity context = Robolectric.buildActivity(Activity.class).create().get();
        final View view = new View(context);
        final LinearLayout parent = new LinearLayout(context);
        view.setFocusable(true);
        parent.setFocusable(true);
        parent.addView(view);
        return view;
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testWithoutInitialValue() {
        final View view = mkFocusableViewWithParent();
        final Observable<OnFocusChangeEvent> observable = ViewObservable.focus(view, false);
        final Observer<OnFocusChangeEvent> observer = mock(Observer.class);
        final Subscription subscription = observable.subscribe(new TestObserver<OnFocusChangeEvent>(observer));

        final InOrder inOrder = inOrder(observer);

        inOrder.verify(observer, never()).onNext(any(OnFocusChangeEvent.class));

        view.requestFocus();
        inOrder.verify(observer, times(1)).onNext(mkMockedEvent(view, true));

        view.clearFocus();
        inOrder.verify(observer, times(1)).onNext(mkMockedEvent(view, false));

        subscription.unsubscribe();
        inOrder.verify(observer, never()).onNext(any(OnFocusChangeEvent.class));

        inOrder.verify(observer, never()).onError(any(Throwable.class));
        inOrder.verify(observer, never()).onCompleted();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testWithInitialValue() {
        final View view = mkFocusableViewWithParent();
        final Observable<OnFocusChangeEvent> observable = ViewObservable.focus(view, true);
        final Observer<OnFocusChangeEvent> observer = mock(Observer.class);
        final Subscription subscription = observable.subscribe(new TestObserver<OnFocusChangeEvent>(observer));

        final InOrder inOrder = inOrder(observer);

        inOrder.verify(observer, times(1)).onNext(mkMockedEvent(view, false));

        view.requestFocus();
        inOrder.verify(observer, times(1)).onNext(mkMockedEvent(view, true));

        view.clearFocus();
        inOrder.verify(observer, times(1)).onNext(mkMockedEvent(view, false));

        subscription.unsubscribe();
        inOrder.verify(observer, never()).onNext(any(OnFocusChangeEvent.class));

        inOrder.verify(observer, never()).onError(any(Throwable.class));
        inOrder.verify(observer, never()).onCompleted();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testMultipleSubscriptions() {
        final View view = mkFocusableViewWithParent();
        final Observable<OnFocusChangeEvent> observable = ViewObservable.focus(view, false);

        final Observer<OnFocusChangeEvent> observer1 = mock(Observer.class);
        final Observer<OnFocusChangeEvent> observer2 = mock(Observer.class);

        final Subscription subscription1 = observable.subscribe(new TestObserver<OnFocusChangeEvent>(observer1));
        final Subscription subscription2 = observable.subscribe(new TestObserver<OnFocusChangeEvent>(observer2));

        final InOrder inOrder1 = inOrder(observer1);
        final InOrder inOrder2 = inOrder(observer2);

        view.requestFocus();
        inOrder1.verify(observer1, times(1)).onNext(mkMockedEvent(view, true));
        inOrder2.verify(observer2, times(1)).onNext(mkMockedEvent(view, true));

        view.clearFocus();
        inOrder1.verify(observer1, times(1)).onNext(mkMockedEvent(view, false));
        inOrder2.verify(observer2, times(1)).onNext(mkMockedEvent(view, false));
        subscription1.unsubscribe();

        view.requestFocus();
        inOrder1.verify(observer1, never()).onNext(any(OnFocusChangeEvent.class));
        inOrder2.verify(observer2, times(1)).onNext(mkMockedEvent(view, true));
        subscription2.unsubscribe();

        view.clearFocus();
        inOrder1.verify(observer1, never()).onNext(any(OnFocusChangeEvent.class));
        inOrder2.verify(observer2, never()).onNext(any(OnFocusChangeEvent.class));

        inOrder1.verify(observer1, never()).onError(any(Throwable.class));
        inOrder2.verify(observer2, never()).onError(any(Throwable.class));

        inOrder1.verify(observer1, never()).onCompleted();
        inOrder2.verify(observer2, never()).onCompleted();
    }
}
