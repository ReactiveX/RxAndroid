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
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ListView;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.observers.TestObserver;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@RunWith(RobolectricTestRunner.class)
public class OperatorAdapterViewOnItemClickTest {

    private static ListView createListView(List<String> values) {
        final Activity activity = Robolectric.buildActivity(Activity.class).create().get();
        final ListView listView = new ListView(activity);
        BaseAdapter adapter = new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, values);
        listView.setAdapter(adapter);
        return listView;
    }

    private static GridView createGridView(List<String> values) {
        final Activity activity = Robolectric.buildActivity(Activity.class).create().get();
        final GridView gridView = new GridView(activity);
        BaseAdapter adapter = new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, values);
        gridView.setAdapter(adapter);
        return gridView;
    }

    private static List<String> createValues(int count) {
        final List<String> values = new ArrayList<String>(count);
        for (int i = 0; i < count; i++) {
            values.add(String.valueOf(i));
        }
        return values;
    }

    @Test
    public void testListViewNeverEmitEventBeforeSubscribed() {
        final ListView listView = createListView(createValues(10));
        performTestAdapterViewNeverEmitEventBeforeSubscribed(listView);
    }

    @Test
    public void testGridViewNeverEmitEventBeforeSubscribed() {
        final GridView gridView = createGridView(createValues(10));
        performTestAdapterViewNeverEmitEventBeforeSubscribed(gridView);
    }

    @Test
    public void testListViewClickAllViewsEmitAllEvents() {
        final ListView listView = createListView(createValues(10));
        performTestAdapterViewClickAllViewsEmitAllEvents(listView);
    }

    @Test
    public void testGridViewClickAllViewsEmitAllEvents() {
        final GridView gridView = createGridView(createValues(10));
        performTestAdapterViewClickAllViewsEmitAllEvents(gridView);
    }

    @Test
    public void testListViewNeverEmitEventAfterUnsubscribed() {
        final ListView listView = createListView(createValues(10));
        performTestAdapterViewNeverEmitEventAfterUnsubscribed(listView);
    }

    @Test
    public void testGridViewNeverEmitEventAfterUnsubscribed() {
        final GridView gridView = createGridView(createValues(10));
        performTestAdapterViewClickAllViewsEmitAllEvents(gridView);
    }

    @Test
    public void testListViewNeverEmitAnyThrowableAfterUnsubscribed() {
        final ListView listView = createListView(createValues(10));
        performTestAdapterViewNeverEmitAnyThrowableAfterUnsubscribed(listView);
    }

    @Test
    public void testGridViewNeverEmitAnyThrowableAfterUnsubscribed() {
        final GridView gridView = createGridView(createValues(10));
        performTestAdapterViewNeverEmitAnyThrowableAfterUnsubscribed(gridView);
    }

    @Test
    public void testListViewNeverEmitOnCompletedAfterUnsubscribed() {
        final ListView listView = createListView(createValues(10));
        performTestAdapterViewNeverEmitOnCompletedAfterUnsubscribed(listView);
    }

    @Test
    public void testGridViewNeverEmitOnCompletedAfterUnsubscribed() {
        final GridView gridView = createGridView(createValues(10));
        performTestAdapterViewNeverEmitOnCompletedAfterUnsubscribed(gridView);
    }

    @Test
    public void testMultipleSubscriptionsListViewNeverEmitEventBeforeSubscribed() {
        final ListView listView = createListView(createValues(10));
        performTestAdapterViewMultipleSubscriptionsNeverEmitEventBeforeSubscribed(listView);
    }

    @Test
    public void testMultipleSubscriptionsGridViewNeverEmitEventBeforeSubscribed() {
        final GridView gridView = createGridView(createValues(10));
        performTestAdapterViewMultipleSubscriptionsNeverEmitEventBeforeSubscribed(gridView);
    }

    @Test
    public void testMultipleSubscriptionsListViewClickAllViewsEmitAllEvents() {
        final ListView listView = createListView(createValues(10));
        performTestAdapterViewMultipleSubscriptionsClickAllViewsEmitAllEvents(listView);
    }

    @Test
    public void testMultipleSubscriptionsGridViewClickAllViewsEmitAllEvents() {
        final GridView gridView = createGridView(createValues(10));
        performTestAdapterViewMultipleSubscriptionsClickAllViewsEmitAllEvents(gridView);
    }

    @Test
    public void testMultipleSubscriptionsListViewClickAllViewsEmitAllEventsForOneSubscriber() {
        final ListView listView = createListView(createValues(10));
        performTestAdapterViewMultipleSubscriptionsClickAllViewsEmitAllEventsForOneSubscriber(listView);
    }

    @Test
    public void testMultipleSubscriptionsGridViewClickAllViewsEmitAllEventsForOneSubscriber() {
        final GridView gridView = createGridView(createValues(10));
        performTestAdapterViewMultipleSubscriptionsClickAllViewsEmitAllEventsForOneSubscriber(gridView);
    }

    @Test
    public void testMultipleSubscriptionsListViewNeverEmitEventAfterUnsubscribed() {
        final ListView listView = createListView(createValues(10));
        performTestAdapterViewMultipleSubscriptionsNeverEmitEventAfterUnsubscribed(listView);
    }

    @Test
    public void testMultipleSubscriptionsGridViewNeverEmitEventAfrerUnsubscribed() {
        final GridView gridView = createGridView(createValues(10));
        performTestAdapterViewMultipleSubscriptionsNeverEmitEventAfterUnsubscribed(gridView);
    }

    @Test
    public void testMultipleSubscriptionsListViewNeverEmitAnyThrowableAfterUnsubscribed() {
        final ListView listView = createListView(createValues(10));
        performTestAdapterViewMultipleSubscriptionsNeverEmitAnyThrowableAfterUnsubscribed(listView);
    }

    @Test
    public void testMultipleSubscriptionsGridViewNeverEmitAnyThrowableAfterUnsubscribed() {
        final GridView gridView = createGridView(createValues(10));
        performTestAdapterViewMultipleSubscriptionsNeverEmitAnyThrowableAfterUnsubscribed(gridView);
    }

    @Test
    public void testMultipleSubscriptionsListViewNeverEmitOnCompletedAfterUnsubscribed() {
        final ListView listView = createListView(createValues(10));
        performTestAdapterViewMultipleSubscriptionsNeverEmitOnCompletedAfterUnsubscribed(listView);
    }

    @Test
    public void testMultipleSubscriptionsGridViewNeverEmitOnCompletedAfterUnsubscribed() {
        final GridView gridView = createGridView(createValues(10));
        performTestAdapterViewMultipleSubscriptionsNeverEmitOnCompletedAfterUnsubscribed(gridView);
    }

    @SuppressWarnings("unchecked")
    private void performTestAdapterViewNeverEmitEventBeforeSubscribed(AdapterView<? extends Adapter> adapterView) {
        Adapter adapter = adapterView.getAdapter();
        Assert.assertNotNull(adapter);
        final Observable<OnItemClickEvent> observable = WidgetObservable.itemClicks(adapterView);
        final Observer<OnItemClickEvent> observer = mock(Observer.class);
        final Subscription subscription = observable.subscribe(new TestObserver<OnItemClickEvent>(observer));

        final InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, never()).onNext(any(OnItemClickEvent.class));

        subscription.unsubscribe();
    }

    @SuppressWarnings("unchecked")
    private void performTestAdapterViewClickAllViewsEmitAllEvents(AdapterView<? extends Adapter> adapterView) {
        Adapter adapter = adapterView.getAdapter();
        Assert.assertNotNull(adapter);
        final Observable<OnItemClickEvent> observable = WidgetObservable.itemClicks(adapterView);
        final Observer<OnItemClickEvent> observer = mock(Observer.class);
        final Subscription subscription = observable.subscribe(new TestObserver<OnItemClickEvent>(observer));

        final InOrder inOrder = inOrder(observer);

        for (int i = 0; i < adapter.getCount(); i++) {
            View fakeItem = new View(adapterView.getContext());
            adapterView.performItemClick(fakeItem, i, i);
            inOrder.verify(observer, times(1)).onNext(OnItemClickEvent.create(adapterView, fakeItem, i, i));
        }

        subscription.unsubscribe();
    }

    @SuppressWarnings("unchecked")
    private void performTestAdapterViewNeverEmitEventAfterUnsubscribed(AdapterView<? extends Adapter> adapterView) {
        Adapter adapter = adapterView.getAdapter();
        Assert.assertNotNull(adapter);
        final Observable<OnItemClickEvent> observable = WidgetObservable.itemClicks(adapterView);
        final Observer<OnItemClickEvent> observer = mock(Observer.class);
        final Subscription subscription = observable.subscribe(new TestObserver<OnItemClickEvent>(observer));

        final InOrder inOrder = inOrder(observer);

        subscription.unsubscribe();

        inOrder.verify(observer, never()).onNext(any(OnItemClickEvent.class));
    }

    @SuppressWarnings("unchecked")
    private void performTestAdapterViewNeverEmitAnyThrowableAfterUnsubscribed(AdapterView<? extends Adapter> adapterView) {
        Adapter adapter = adapterView.getAdapter();
        Assert.assertNotNull(adapter);
        final Observable<OnItemClickEvent> observable = WidgetObservable.itemClicks(adapterView);
        final Observer<OnItemClickEvent> observer = mock(Observer.class);
        final Subscription subscription = observable.subscribe(new TestObserver<OnItemClickEvent>(observer));

        final InOrder inOrder = inOrder(observer);
        subscription.unsubscribe();

        inOrder.verify(observer, never()).onError(any(Throwable.class));
    }

    @SuppressWarnings("unchecked")
    private void performTestAdapterViewNeverEmitOnCompletedAfterUnsubscribed(AdapterView<? extends Adapter> adapterView) {
        Adapter adapter = adapterView.getAdapter();
        Assert.assertNotNull(adapter);
        final Observable<OnItemClickEvent> observable = WidgetObservable.itemClicks(adapterView);
        final Observer<OnItemClickEvent> observer = mock(Observer.class);
        final Subscription subscription = observable.subscribe(new TestObserver<OnItemClickEvent>(observer));

        final InOrder inOrder = inOrder(observer);
        subscription.unsubscribe();

        inOrder.verify(observer, never()).onCompleted();
    }

    @SuppressWarnings("unchecked")
    private void performTestAdapterViewMultipleSubscriptionsNeverEmitEventBeforeSubscribed(AdapterView<? extends Adapter> adapterView) {
        Adapter adapter = adapterView.getAdapter();
        Assert.assertNotNull(adapter);
        final Observable<OnItemClickEvent> observable = WidgetObservable.itemClicks(adapterView);
        final Observer<OnItemClickEvent> observer1 = mock(Observer.class);
        final Observer<OnItemClickEvent> observer2 = mock(Observer.class);
        final Subscription subscription1 = observable.subscribe(new TestObserver<OnItemClickEvent>(observer1));
        final Subscription subscription2 = observable.subscribe(new TestObserver<OnItemClickEvent>(observer2));

        final InOrder inOrder1 = inOrder(observer1);
        final InOrder inOrder2 = inOrder(observer2);

        inOrder1.verify(observer1, never()).onNext(any(OnItemClickEvent.class));
        inOrder2.verify(observer2, never()).onNext(any(OnItemClickEvent.class));

        subscription1.unsubscribe();
        subscription2.unsubscribe();
    }

    @SuppressWarnings("unchecked")
    private void performTestAdapterViewMultipleSubscriptionsClickAllViewsEmitAllEvents(AdapterView<? extends Adapter> adapterView) {
        Adapter adapter = adapterView.getAdapter();
        Assert.assertNotNull(adapter);
        final Observable<OnItemClickEvent> observable = WidgetObservable.itemClicks(adapterView);
        final Observer<OnItemClickEvent> observer1 = mock(Observer.class);
        final Observer<OnItemClickEvent> observer2 = mock(Observer.class);
        final Subscription subscription1 = observable.subscribe(new TestObserver<OnItemClickEvent>(observer1));
        final Subscription subscription2 = observable.subscribe(new TestObserver<OnItemClickEvent>(observer2));

        final InOrder inOrder1 = inOrder(observer1);
        final InOrder inOrder2 = inOrder(observer2);

        final int count = adapter.getCount();
        for (int i = 0; i < count; i++) {
            View fakeItem = new View(adapterView.getContext());
            adapterView.performItemClick(fakeItem, i, i);
            inOrder1.verify(observer1, times(1)).onNext(OnItemClickEvent.create(adapterView, fakeItem, i, i));
            inOrder2.verify(observer2, times(1)).onNext(OnItemClickEvent.create(adapterView, fakeItem, i, i));
        }

        subscription1.unsubscribe();
        subscription2.unsubscribe();
    }

    @SuppressWarnings("unchecked")
    private void performTestAdapterViewMultipleSubscriptionsClickAllViewsEmitAllEventsForOneSubscriber(AdapterView<? extends Adapter> adapterView) {
        Adapter adapter = adapterView.getAdapter();
        Assert.assertNotNull(adapter);
        final Observable<OnItemClickEvent> observable = WidgetObservable.itemClicks(adapterView);
        final Observer<OnItemClickEvent> observer1 = mock(Observer.class);
        final Observer<OnItemClickEvent> observer2 = mock(Observer.class);
        final Subscription subscription1 = observable.subscribe(new TestObserver<OnItemClickEvent>(observer1));
        final Subscription subscription2 = observable.subscribe(new TestObserver<OnItemClickEvent>(observer2));

        final InOrder inOrder1 = inOrder(observer1);
        final InOrder inOrder2 = inOrder(observer2);

        inOrder1.verify(observer1, never()).onNext(any(OnItemClickEvent.class));
        inOrder2.verify(observer2, never()).onNext(any(OnItemClickEvent.class));

        subscription1.unsubscribe();

        final int count = adapter.getCount();
        for (int i = 0; i < count; i++) {
            View fakeItem = new View(adapterView.getContext());
            adapterView.performItemClick(fakeItem, i, i);
            inOrder1.verify(observer1, never()).onNext(any(OnItemClickEvent.class));
            inOrder2.verify(observer2, times(1)).onNext(OnItemClickEvent.create(adapterView, fakeItem, i, i));
        }
        subscription2.unsubscribe();
    }

    @SuppressWarnings("unchecked")
    private void performTestAdapterViewMultipleSubscriptionsNeverEmitEventAfterUnsubscribed(AdapterView<? extends Adapter> adapterView) {
        Adapter adapter = adapterView.getAdapter();
        Assert.assertNotNull(adapter);
        final Observable<OnItemClickEvent> observable = WidgetObservable.itemClicks(adapterView);
        final Observer<OnItemClickEvent> observer1 = mock(Observer.class);
        final Observer<OnItemClickEvent> observer2 = mock(Observer.class);
        final Subscription subscription1 = observable.subscribe(new TestObserver<OnItemClickEvent>(observer1));
        final Subscription subscription2 = observable.subscribe(new TestObserver<OnItemClickEvent>(observer2));

        final InOrder inOrder1 = inOrder(observer1);
        final InOrder inOrder2 = inOrder(observer2);

        subscription1.unsubscribe();
        subscription2.unsubscribe();

        inOrder1.verify(observer1, never()).onNext(any(OnItemClickEvent.class));
        inOrder2.verify(observer2, never()).onNext(any(OnItemClickEvent.class));
    }

    @SuppressWarnings("unchecked")
    private void performTestAdapterViewMultipleSubscriptionsNeverEmitAnyThrowableAfterUnsubscribed(AdapterView<? extends Adapter> adapterView) {
        Adapter adapter = adapterView.getAdapter();
        Assert.assertNotNull(adapter);
        final Observable<OnItemClickEvent> observable = WidgetObservable.itemClicks(adapterView);
        final Observer<OnItemClickEvent> observer1 = mock(Observer.class);
        final Observer<OnItemClickEvent> observer2 = mock(Observer.class);
        final Subscription subscription1 = observable.subscribe(new TestObserver<OnItemClickEvent>(observer1));
        final Subscription subscription2 = observable.subscribe(new TestObserver<OnItemClickEvent>(observer2));

        final InOrder inOrder1 = inOrder(observer1);
        final InOrder inOrder2 = inOrder(observer2);

        subscription1.unsubscribe();
        subscription2.unsubscribe();

        inOrder1.verify(observer1, never()).onError(any(Throwable.class));
        inOrder2.verify(observer2, never()).onError(any(Throwable.class));
    }

    @SuppressWarnings("unchecked")
    private void performTestAdapterViewMultipleSubscriptionsNeverEmitOnCompletedAfterUnsubscribed(AdapterView<? extends Adapter> adapterView) {
        Adapter adapter = adapterView.getAdapter();
        Assert.assertNotNull(adapter);
        final Observable<OnItemClickEvent> observable = WidgetObservable.itemClicks(adapterView);
        final Observer<OnItemClickEvent> observer1 = mock(Observer.class);
        final Observer<OnItemClickEvent> observer2 = mock(Observer.class);
        final Subscription subscription1 = observable.subscribe(new TestObserver<OnItemClickEvent>(observer1));
        final Subscription subscription2 = observable.subscribe(new TestObserver<OnItemClickEvent>(observer2));

        final InOrder inOrder1 = inOrder(observer1);
        final InOrder inOrder2 = inOrder(observer2);

        subscription1.unsubscribe();
        subscription2.unsubscribe();

        inOrder1.verify(observer1, never()).onCompleted();
        inOrder2.verify(observer2, never()).onCompleted();
    }
}
