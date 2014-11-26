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
import android.widget.CheckBox;
import android.widget.CompoundButton;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.android.view.OnCheckedChangeEvent;
import rx.observers.TestObserver;

import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
public class OperatorCompoundButtonInputTest {
    private static OnCheckedChangeEvent mkMockedEvent(final CompoundButton button, final boolean value) {
        return refEq(OnCheckedChangeEvent.create(button, value));
    }

    private static CompoundButton mkCompoundButton(final boolean value) {
        final Activity activity = Robolectric.buildActivity(Activity.class).create().get();
        final CheckBox checkbox = new CheckBox(activity);

        checkbox.setChecked(value);
        return checkbox;
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testWithoutInitialValue() {
        final CompoundButton button = mkCompoundButton(true);
        final Observable<OnCheckedChangeEvent> observable = WidgetObservable.input(button, false);
        final Observer<OnCheckedChangeEvent> observer = mock(Observer.class);
        final Subscription subscription = observable.subscribe(new TestObserver<OnCheckedChangeEvent>(observer));

        final InOrder inOrder = inOrder(observer);

        inOrder.verify(observer, never()).onNext(any(OnCheckedChangeEvent.class));

        button.setChecked(true);
        inOrder.verify(observer, never()).onNext(any(OnCheckedChangeEvent.class));

        button.setChecked(false);
        inOrder.verify(observer, times(1)).onNext(mkMockedEvent(button, false));

        button.setChecked(true);
        inOrder.verify(observer, times(1)).onNext(mkMockedEvent(button, true));

        button.setChecked(false);
        inOrder.verify(observer, times(1)).onNext(mkMockedEvent(button, false));
        subscription.unsubscribe();

        button.setChecked(true);
        inOrder.verify(observer, never()).onNext(any(OnCheckedChangeEvent.class));

        inOrder.verify(observer, never()).onError(any(Throwable.class));
        inOrder.verify(observer, never()).onCompleted();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testWithInitialValue() {
        final CompoundButton button = mkCompoundButton(true);
        final Observable<OnCheckedChangeEvent> observable = WidgetObservable.input(button, true);
        final Observer<OnCheckedChangeEvent> observer = mock(Observer.class);
        final Subscription subscription = observable.subscribe(new TestObserver<OnCheckedChangeEvent>(observer));

        final InOrder inOrder = inOrder(observer);

        inOrder.verify(observer, times(1)).onNext(mkMockedEvent(button, true));

        button.setChecked(false);
        inOrder.verify(observer, times(1)).onNext(mkMockedEvent(button, false));

        button.setChecked(true);
        inOrder.verify(observer, times(1)).onNext(mkMockedEvent(button, true));

        button.setChecked(true);
        inOrder.verify(observer, never()).onNext(any(OnCheckedChangeEvent.class));

        button.setChecked(false);
        inOrder.verify(observer, times(1)).onNext(mkMockedEvent(button, false));
        subscription.unsubscribe();

        button.setChecked(true);
        inOrder.verify(observer, never()).onNext(any(OnCheckedChangeEvent.class));

        inOrder.verify(observer, never()).onError(any(Throwable.class));
        inOrder.verify(observer, never()).onCompleted();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testMultipleSubscriptions() {
        final CompoundButton button = mkCompoundButton(false);
        final Observable<OnCheckedChangeEvent> observable = WidgetObservable.input(button, false);

        final Observer<OnCheckedChangeEvent> observer1 = mock(Observer.class);
        final Observer<OnCheckedChangeEvent> observer2 = mock(Observer.class);

        final Subscription subscription1 = observable.subscribe(new TestObserver<OnCheckedChangeEvent>(observer1));
        final Subscription subscription2 = observable.subscribe(new TestObserver<OnCheckedChangeEvent>(observer2));

        final InOrder inOrder1 = inOrder(observer1);
        final InOrder inOrder2 = inOrder(observer2);

        button.setChecked(true);
        inOrder1.verify(observer1, times(1)).onNext(mkMockedEvent(button, true));
        inOrder2.verify(observer2, times(1)).onNext(mkMockedEvent(button, true));

        button.setChecked(false);
        inOrder1.verify(observer1, times(1)).onNext(mkMockedEvent(button, false));
        inOrder2.verify(observer2, times(1)).onNext(mkMockedEvent(button, false));
        subscription1.unsubscribe();

        button.setChecked(true);
        inOrder1.verify(observer1, never()).onNext(any(OnCheckedChangeEvent.class));
        inOrder2.verify(observer2, times(1)).onNext(mkMockedEvent(button, true));
        subscription2.unsubscribe();

        button.setChecked(false);
        inOrder1.verify(observer1, never()).onNext(any(OnCheckedChangeEvent.class));
        inOrder2.verify(observer2, never()).onNext(any(OnCheckedChangeEvent.class));

        inOrder1.verify(observer1, never()).onError(any(Throwable.class));
        inOrder1.verify(observer1, never()).onCompleted();

        inOrder2.verify(observer2, never()).onError(any(Throwable.class));
        inOrder2.verify(observer2, never()).onCompleted();
    }
}
