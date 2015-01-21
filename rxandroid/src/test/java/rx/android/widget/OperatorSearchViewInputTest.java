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
import android.widget.EditText;
import android.widget.SearchView;
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
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
public class OperatorSearchViewInputTest {

    private static SearchView mkSearchView() {
        final Activity activity = Robolectric.buildActivity(Activity.class).create().get();
        final SearchView searchView = new SearchView(activity);

        return searchView;
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testQueryTextSubmit() {
        final SearchView input = mkSearchView();
        final Observable<String> observable = WidgetObservable.queryTextSubmit(input);
        final Observer<String> observer = mock(Observer.class);
        final Subscription subscription = observable.subscribe(new TestObserver<String>(observer));

        final InOrder inOrder = inOrder(observer);

        inOrder.verify(observer, never()).onNext(any(String.class));

        input.setQuery("1",true);
        inOrder.verify(observer, times(1)).onNext("1");


        input.setQuery("2",true);
        inOrder.verify(observer, times(1)).onNext("2");


        input.setQuery("3",true);
        inOrder.verify(observer, times(1)).onNext("3");


        input.setQuery("4",true);
        inOrder.verify(observer, times(1)).onNext("4");

        subscription.unsubscribe();
        input.setQuery("5",true);
        inOrder.verify(observer, never()).onNext(any(String.class));

        inOrder.verify(observer, never()).onError(any(Throwable.class));
        inOrder.verify(observer, never()).onCompleted();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testQueryTextChange() {
        final SearchView input = mkSearchView();
        final Observable<String> observable = WidgetObservable.queryTextChange(input);
        final Observer<String> observer = mock(Observer.class);
        final Subscription subscription = observable.subscribe(new TestObserver<String>(observer));

        final InOrder inOrder = inOrder(observer);

        inOrder.verify(observer, never()).onNext(any(String.class));

        input.setQuery("1",false);
        inOrder.verify(observer, times(1)).onNext("1");


        input.setQuery("2",false);
        inOrder.verify(observer, times(1)).onNext("2");


        input.setQuery("3",false);
        inOrder.verify(observer, times(1)).onNext("3");


        input.setQuery("4",false);
        inOrder.verify(observer, times(1)).onNext("4");

        subscription.unsubscribe();
        input.setQuery("5",false);
        inOrder.verify(observer, never()).onNext(any(String.class));

        inOrder.verify(observer, never()).onError(any(Throwable.class));
        inOrder.verify(observer, never()).onCompleted();
    }



    @Test
    @SuppressWarnings("unchecked")
    public void testMultipleSubscriptions() {
        final SearchView input = mkSearchView();
        final Observable<String> observable = WidgetObservable.queryTextSubmit(input);

        final Observer<String> observer1 = mock(Observer.class);
        final Observer<String> observer2 = mock(Observer.class);

        final Subscription subscription1 = observable.subscribe(new TestObserver<String>(observer1));
        final Subscription subscription2 = observable.subscribe(new TestObserver<String>(observer2));

        final InOrder inOrder1 = inOrder(observer1);
        final InOrder inOrder2 = inOrder(observer2);

        input.setQuery("1", true);
        inOrder1.verify(observer1, times(1)).onNext("1");
        inOrder2.verify(observer2, times(1)).onNext("1");

        input.setQuery("2", true);
        inOrder1.verify(observer1, times(1)).onNext("2");
        inOrder2.verify(observer2, times(1)).onNext("2");
        subscription1.unsubscribe();

        input.setQuery("3", true);
        inOrder1.verify(observer1, never()).onNext(any(String.class));
        inOrder2.verify(observer2, times(1)).onNext("3");
        subscription2.unsubscribe();

        input.setQuery("4", true);
        inOrder1.verify(observer1, never()).onNext(any(String.class));
        inOrder2.verify(observer2, never()).onNext(any(String.class));

        inOrder1.verify(observer1, never()).onError(any(Throwable.class));
        inOrder2.verify(observer2, never()).onError(any(Throwable.class));

        inOrder1.verify(observer1, never()).onCompleted();
        inOrder2.verify(observer2, never()).onCompleted();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testMultipleDifferentSubscriptions() {
        final SearchView input = mkSearchView();
        final Observable<String> observable = WidgetObservable.queryTextChange(input);
        final Observable<String> observable1 = WidgetObservable.queryTextSubmit(input);

        final Observer<String> observer1 = mock(Observer.class);
        final Observer<String> observer2 = mock(Observer.class);
        final Observer<String> observer3 = mock(Observer.class);

        final Subscription subscription1 = observable.subscribe(new TestObserver<String>(observer1));
        final Subscription subscription2 = observable.subscribe(new TestObserver<String>(observer2));
        final Subscription subscription3 = observable1.subscribe(new TestObserver<String>(observer3));

        final InOrder inOrder1 = inOrder(observer1);
        final InOrder inOrder2 = inOrder(observer2);
        final InOrder inOrder3 = inOrder(observer3);

        input.setQuery("1", false);
        inOrder1.verify(observer1, times(1)).onNext("1");
        inOrder2.verify(observer2, times(1)).onNext("1");
        inOrder3.verify(observer3, never()).onNext(any(String.class));

        input.setQuery("2", true);
        inOrder1.verify(observer1, times(1)).onNext("2");
        inOrder2.verify(observer2, times(1)).onNext("2");
        inOrder3.verify(observer3, times(1)).onNext("2");
        subscription1.unsubscribe();
        subscription3.unsubscribe();

        input.setQuery("3", true);
        inOrder1.verify(observer1, never()).onNext(any(String.class));
        inOrder2.verify(observer2, times(1)).onNext("3");
        subscription2.unsubscribe();

        input.setQuery("4", true);
        inOrder1.verify(observer1, never()).onNext(any(String.class));
        inOrder2.verify(observer2, never()).onNext(any(String.class));

        inOrder1.verify(observer1, never()).onError(any(Throwable.class));
        inOrder2.verify(observer2, never()).onError(any(Throwable.class));

        inOrder1.verify(observer1, never()).onCompleted();
        inOrder2.verify(observer2, never()).onCompleted();
    }
}

