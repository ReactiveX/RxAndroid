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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;

import android.app.Activity;
import android.widget.EditText;
import android.widget.TextView;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.functions.Func1;
import rx.observers.TestObserver;

@RunWith(RobolectricTestRunner.class)
public class OperatorTextViewInputTest {

    private static TextView createTextView(final String value) {
        final Activity activity = Robolectric.buildActivity(Activity.class).create().get();
        final TextView text = new TextView(activity);

        if (value != null) {
            text.setText(value);
        }

        return text;
    }

    private static EditText createEditText(final String value) {
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
        final TextView input = createTextView("initial");
        final Observable<TextView> observable = WidgetObservable.forText(input);
        final Observer<TextView> observer = mock(Observer.class);
        final Subscription subscription = observable.subscribe(new TestObserver<TextView>(observer));

        final InOrder inOrder = inOrder(observer);

        inOrder.verify(observer, never()).onNext(any(TextView.class));

        input.setText("1");
        inOrder.verify(observer, times(1)).onNext(input);

        input.setText("2");
        inOrder.verify(observer, times(1)).onNext(input);

        input.setText("3");
        inOrder.verify(observer, times(1)).onNext(input);

        subscription.unsubscribe();
        input.setText("4");
        inOrder.verify(observer, never()).onNext(any(TextView.class));

        inOrder.verify(observer, never()).onError(any(Throwable.class));
        inOrder.verify(observer, never()).onCompleted();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testWithoutInitialValue() {
        final TextView input = createTextView("initial");
        final Observable<TextView> observable = WidgetObservable.forText(input, false);
        final Observer<TextView> observer = mock(Observer.class);
        final Subscription subscription = observable.subscribe(new TestObserver<TextView>(observer));

        final InOrder inOrder = inOrder(observer);

        inOrder.verify(observer, never()).onNext(any(TextView.class));

        input.setText("1");
        inOrder.verify(observer, times(1)).onNext(input);

        input.setText("2");
        inOrder.verify(observer, times(1)).onNext(input);

        input.setText("3");
        inOrder.verify(observer, times(1)).onNext(input);

        subscription.unsubscribe();
        input.setText("4");
        inOrder.verify(observer, never()).onNext(any(TextView.class));

        inOrder.verify(observer, never()).onError(any(Throwable.class));
        inOrder.verify(observer, never()).onCompleted();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testWithInitialValue() {
        final TextView input = createTextView("initial");
        final Observable<TextView> observable = WidgetObservable.forText(input, true);
        final Observer<TextView> observer = mock(Observer.class);
        final Subscription subscription = observable.subscribe(new TestObserver<TextView>(observer));

        final InOrder inOrder = inOrder(observer);

        inOrder.verify(observer, times(1)).onNext(input);

        input.setText("one");
        inOrder.verify(observer, times(1)).onNext(input);

        input.setText("two");
        inOrder.verify(observer, times(1)).onNext(input);

        input.setText("three");
        inOrder.verify(observer, times(1)).onNext(input);

        subscription.unsubscribe();
        input.setText("four");
        inOrder.verify(observer, never()).onNext(any(TextView.class));

        inOrder.verify(observer, never()).onError(any(Throwable.class));
        inOrder.verify(observer, never()).onCompleted();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testMultipleSubscriptions() {
        final TextView input = createTextView("initial");
        final Observable<TextView> observable = WidgetObservable.forText(input, false);

        final Observer<TextView> observer1 = mock(Observer.class);
        final Observer<TextView> observer2 = mock(Observer.class);

        final Subscription subscription1 = observable.subscribe(new TestObserver<TextView>(observer1));
        final Subscription subscription2 = observable.subscribe(new TestObserver<TextView>(observer2));

        final InOrder inOrder1 = inOrder(observer1);
        final InOrder inOrder2 = inOrder(observer2);

        input.setText("1");
        inOrder1.verify(observer1, times(1)).onNext(input);
        inOrder2.verify(observer2, times(1)).onNext(input);

        input.setText("2");
        inOrder1.verify(observer1, times(1)).onNext(input);
        inOrder2.verify(observer2, times(1)).onNext(input);
        subscription1.unsubscribe();

        input.setText("3");
        inOrder1.verify(observer1, never()).onNext(any(TextView.class));
        inOrder2.verify(observer2, times(1)).onNext(input);
        subscription2.unsubscribe();

        input.setText("4");
        inOrder1.verify(observer1, never()).onNext(any(TextView.class));
        inOrder2.verify(observer2, never()).onNext(any(TextView.class));

        inOrder1.verify(observer1, never()).onError(any(Throwable.class));
        inOrder2.verify(observer2, never()).onError(any(Throwable.class));

        inOrder1.verify(observer1, never()).onCompleted();
        inOrder2.verify(observer2, never()).onCompleted();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testTextViewSubclass() {
        final EditText input = createEditText("initial");
        final Observable<EditText> observable = WidgetObservable.forText(input, false);
        final Observer<EditText> observer = mock(Observer.class);
        observable.subscribe(new TestObserver<EditText>(observer));

        final InOrder inOrder = inOrder(observer);

        inOrder.verify(observer, never()).onNext(any(EditText.class));

        input.setText("1");
        inOrder.verify(observer, times(1)).onNext(input);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testLegacyStringObservableCompatibility() {
        final EditText input = createEditText("initial");
        final Observable<String> observable = WidgetObservable.forText(input, false)
            .map(new Func1<EditText, String>() {

                    @Override
                    public String call(EditText view) {
                        return view.getText().toString();
                    }
                });
        final Observer<String> observer = mock(Observer.class);
        observable.subscribe(new TestObserver<String>(observer));

        final InOrder inOrder = inOrder(observer);

        inOrder.verify(observer, never()).onNext(anyString());

        input.setText("1");
        inOrder.verify(observer, times(1)).onNext("1");

        input.setText("2");
        inOrder.verify(observer, times(1)).onNext("2");

        input.setText("3");
        inOrder.verify(observer, times(1)).onNext("3");
    }

}
