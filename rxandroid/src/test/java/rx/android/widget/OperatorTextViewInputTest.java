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
package rx.android.widget;

import android.app.Activity;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InOrder;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.observers.TestObserver;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
public class OperatorTextViewInputTest {
    private static OnTextChangeEvent mkMockedEvent(final TextView view, final CharSequence text) {
        return argThat(new ArgumentMatcher<OnTextChangeEvent>() {
            @Override
            public boolean matches(final Object argument) {
                if (!(argument instanceof OnTextChangeEvent)) {
                    return false;
                }

                final OnTextChangeEvent event = (OnTextChangeEvent) argument;

                if (event.view() != view) {
                    return false;
                }

                return TextUtils.equals(event.text(), text);
            }
        });
    }

    private static TextView mkTextView(final CharSequence value) {
        final Activity activity = Robolectric.buildActivity(Activity.class).create().get();
        final TextView text = new TextView(activity);

        if (value != null) {
            text.setText(value);
        }

        return text;
    }

    private static EditText mkEditText(final CharSequence value) {
        final Activity activity = Robolectric.buildActivity(Activity.class).create().get();
        final EditText text = new EditText(activity);

        if (value != null) {
            text.setText(value);
        }

        return text;
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testOverloadedMethodDefaultsWithoutInitialValue() {
        final TextView input = mkTextView("initial");
        final Observable<OnTextChangeEvent> observable = WidgetObservable.text(input);
        final Observer<OnTextChangeEvent> observer = mock(Observer.class);
        final Subscription subscription = observable.subscribe(new TestObserver<OnTextChangeEvent>(observer));

        final InOrder inOrder = inOrder(observer);

        inOrder.verify(observer, never()).onNext(any(OnTextChangeEvent.class));

        input.setText("1");
        inOrder.verify(observer, times(1)).onNext(mkMockedEvent(input, "1"));

        input.setText("2");
        inOrder.verify(observer, times(1)).onNext(mkMockedEvent(input, "2"));

        input.setText("3");
        inOrder.verify(observer, times(1)).onNext(mkMockedEvent(input, "3"));

        subscription.unsubscribe();
        input.setText("4");
        inOrder.verify(observer, never()).onNext(any(OnTextChangeEvent.class));

        inOrder.verify(observer, never()).onError(any(Throwable.class));
        inOrder.verify(observer, never()).onCompleted();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testWithoutInitialValue() {
        final TextView input = mkTextView("initial");
        final Observable<OnTextChangeEvent> observable = WidgetObservable.text(input, false);
        final Observer<OnTextChangeEvent> observer = mock(Observer.class);
        final Subscription subscription = observable.subscribe(new TestObserver<OnTextChangeEvent>(observer));

        final InOrder inOrder = inOrder(observer);

        inOrder.verify(observer, never()).onNext(any(OnTextChangeEvent.class));

        input.setText("1");
        inOrder.verify(observer, times(1)).onNext(mkMockedEvent(input, "1"));

        input.setText("2");
        inOrder.verify(observer, times(1)).onNext(mkMockedEvent(input, "2"));

        input.setText("3");
        inOrder.verify(observer, times(1)).onNext(mkMockedEvent(input, "3"));

        subscription.unsubscribe();
        input.setText("4");
        inOrder.verify(observer, never()).onNext(any(OnTextChangeEvent.class));

        inOrder.verify(observer, never()).onError(any(Throwable.class));
        inOrder.verify(observer, never()).onCompleted();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testWithInitialValue() {
        final TextView input = mkTextView("initial");
        final Observable<OnTextChangeEvent> observable = WidgetObservable.text(input, true);
        final Observer<OnTextChangeEvent> observer = mock(Observer.class);
        final Subscription subscription = observable.subscribe(new TestObserver<OnTextChangeEvent>(observer));

        final InOrder inOrder = inOrder(observer);

        inOrder.verify(observer, times(1)).onNext(mkMockedEvent(input, "initial"));

        input.setText("1");
        inOrder.verify(observer, times(1)).onNext(mkMockedEvent(input, "1"));

        input.setText("2");
        inOrder.verify(observer, times(1)).onNext(mkMockedEvent(input, "2"));

        input.setText("3");
        inOrder.verify(observer, times(1)).onNext(mkMockedEvent(input, "3"));

        subscription.unsubscribe();
        input.setText("4");
        inOrder.verify(observer, never()).onNext(any(OnTextChangeEvent.class));

        inOrder.verify(observer, never()).onError(any(Throwable.class));
        inOrder.verify(observer, never()).onCompleted();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testMultipleSubscriptions() {
        final TextView input = mkTextView("initial");
        final Observable<OnTextChangeEvent> observable = WidgetObservable.text(input, false);

        final Observer<OnTextChangeEvent> observer1 = mock(Observer.class);
        final Observer<OnTextChangeEvent> observer2 = mock(Observer.class);

        final Subscription subscription1 = observable.subscribe(new TestObserver<OnTextChangeEvent>(observer1));
        final Subscription subscription2 = observable.subscribe(new TestObserver<OnTextChangeEvent>(observer2));

        final InOrder inOrder1 = inOrder(observer1);
        final InOrder inOrder2 = inOrder(observer2);

        input.setText("1");
        inOrder1.verify(observer1, times(1)).onNext(mkMockedEvent(input, "1"));
        inOrder2.verify(observer2, times(1)).onNext(mkMockedEvent(input, "1"));

        input.setText("2");
        inOrder1.verify(observer1, times(1)).onNext(mkMockedEvent(input, "2"));
        inOrder2.verify(observer2, times(1)).onNext(mkMockedEvent(input, "2"));
        subscription1.unsubscribe();

        input.setText("3");
        inOrder1.verify(observer1, never()).onNext(any(OnTextChangeEvent.class));
        inOrder2.verify(observer2, times(1)).onNext(mkMockedEvent(input, "3"));
        subscription2.unsubscribe();

        input.setText("4");
        inOrder1.verify(observer1, never()).onNext(any(OnTextChangeEvent.class));
        inOrder2.verify(observer2, never()).onNext(any(OnTextChangeEvent.class));

        inOrder1.verify(observer1, never()).onError(any(Throwable.class));
        inOrder2.verify(observer2, never()).onError(any(Throwable.class));

        inOrder1.verify(observer1, never()).onCompleted();
        inOrder2.verify(observer2, never()).onCompleted();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testTextViewSubclass() {
        final EditText input = mkEditText("initial");
        final Observable<OnTextChangeEvent> observable = WidgetObservable.text(input, false);
        final Observer<OnTextChangeEvent> observer = mock(Observer.class);
        observable.subscribe(new TestObserver<OnTextChangeEvent>(observer));

        final InOrder inOrder = inOrder(observer);

        inOrder.verify(observer, never()).onNext(any(OnTextChangeEvent.class));

        input.setText("1");
        inOrder.verify(observer, times(1)).onNext(mkMockedEvent(input, "1"));
    }
}

