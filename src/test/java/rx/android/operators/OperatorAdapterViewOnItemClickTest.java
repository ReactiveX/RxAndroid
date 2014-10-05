/**
 * Copyright 2014 Netflix, Inc.
 *
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
package rx.android.operators;

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
import rx.android.events.OnItemClickEvent;
import rx.android.observables.ViewObservable;
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
    public void testListView() {
        final ListView listView = createListView(createValues(10));
        performTestAdapterView(listView);
    }

    @Test
    public void testGridView() {
        final GridView gridView = createGridView(createValues(10));
        performTestAdapterView(gridView);
    }

    @Test
    public void testMultipleSubscriptionsListView() {
        final ListView listView = createListView(createValues(10));
        performTestAdapterViewMultipleSubscriptions(listView);
    }

    @Test
    public void testMultipleSubscriptionsGridView() {
        final GridView gridView = createGridView(createValues(10));
        performTestAdapterViewMultipleSubscriptions(gridView);
    }

    @SuppressWarnings("unchecked")
    private void performTestAdapterView(AdapterView<? extends Adapter> adapterView) {
        Adapter adapter = adapterView.getAdapter();
        Assert.assertNotNull(adapter);
        final Observable<OnItemClickEvent> observable = ViewObservable.itemClicks(adapterView);
        final Observer<OnItemClickEvent> observer = mock(Observer.class);
        final Subscription subscription = observable.subscribe(new TestObserver<OnItemClickEvent>(observer));

        final InOrder inOrder = inOrder(observer);
        inOrder.verify(observer, never()).onNext(any(OnItemClickEvent.class));

        for (int i = 0; i < adapter.getCount(); i++) {
            adapterView.performItemClick(any(View.class), i, i);
            inOrder.verify(observer, times(1)).onNext(new OnItemClickEvent(adapterView, any(View.class), i, i));
        }

        subscription.unsubscribe();

        inOrder.verify(observer, never()).onNext(any(OnItemClickEvent.class));

        inOrder.verify(observer, never()).onError(any(Throwable.class));
        inOrder.verify(observer, never()).onCompleted();
    }

    @SuppressWarnings("unchecked")
    private void performTestAdapterViewMultipleSubscriptions(AdapterView<? extends Adapter> adapterView) {
        Adapter adapter = adapterView.getAdapter();
        Assert.assertNotNull(adapter);
        final Observable<OnItemClickEvent> observable = ViewObservable.itemClicks(adapterView);
        final Observer<OnItemClickEvent> observer1 = mock(Observer.class);
        final Observer<OnItemClickEvent> observer2 = mock(Observer.class);
        final Subscription subscription1 = observable.subscribe(new TestObserver<OnItemClickEvent>(observer1));
        final Subscription subscription2 = observable.subscribe(new TestObserver<OnItemClickEvent>(observer2));

        final InOrder inOrder1 = inOrder(observer1);
        final InOrder inOrder2 = inOrder(observer2);

        inOrder1.verify(observer1, never()).onNext(any(OnItemClickEvent.class));
        inOrder2.verify(observer2, never()).onNext(any(OnItemClickEvent.class));

        final int count = adapter.getCount();
        for (int i = 0; i < count; i++) {
            adapterView.performItemClick(any(View.class), i, i);
            inOrder1.verify(observer1, times(1)).onNext(new OnItemClickEvent(adapterView, any(View.class), i, i));
            inOrder2.verify(observer2, times(1)).onNext(new OnItemClickEvent(adapterView, any(View.class), i, i));
        }
        subscription1.unsubscribe();

        for (int i = 0; i < count; i++) {
            adapterView.performItemClick(any(View.class), i, i);
            inOrder1.verify(observer1, never()).onNext(any(OnItemClickEvent.class));
            inOrder2.verify(observer2, times(1)).onNext(new OnItemClickEvent(adapterView, any(View.class), i, i));
        }
        subscription2.unsubscribe();

        inOrder1.verify(observer1, never()).onNext(any(OnItemClickEvent.class));
        inOrder2.verify(observer2, never()).onNext(any(OnItemClickEvent.class));

        inOrder1.verify(observer1, never()).onError(any(Throwable.class));
        inOrder2.verify(observer2, never()).onNext(any(OnItemClickEvent.class));
        inOrder1.verify(observer1, never()).onCompleted();
        inOrder2.verify(observer2, never()).onCompleted();
    }
}
