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
package rx.android.content;

import android.app.Activity;
import android.app.Fragment;
import android.database.Cursor;
import android.support.v4.app.FragmentActivity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.android.TestUtil;
import rx.observers.TestObserver;
import rx.observers.TestSubscriber;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ContentObservableTest {

    // support library fragments
    private FragmentActivity fragmentActivity;
    private android.support.v4.app.Fragment supportFragment;

    // native fragments
    private Activity activity;
    private Fragment fragment;

    @Mock
    private Observer<String> observer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        supportFragment = new android.support.v4.app.Fragment();
        fragmentActivity = Robolectric.buildActivity(FragmentActivity.class).create().get();
        fragmentActivity.getSupportFragmentManager().beginTransaction().add(supportFragment, null).commit();

        fragment = new Fragment();
        activity = Robolectric.buildActivity(Activity.class).create().get();
        activity.getFragmentManager().beginTransaction().add(fragment, null).commit();
    }

    @Test
    public void itSupportsFragmentsFromTheSupportV4Library() {
        ContentObservable.bindFragment(supportFragment, Observable.just("success")).subscribe(new TestObserver<String>(observer));
        verify(observer).onNext("success");
        verify(observer).onCompleted();
    }

    @Test
    public void itSupportsNativeFragments() {
        ContentObservable.bindFragment(fragment, Observable.just("success")).subscribe(new TestObserver<String>(observer));
        verify(observer).onNext("success");
        verify(observer).onCompleted();
    }

    @Test(expected = IllegalArgumentException.class)
    public void itThrowsIfObjectPassedIsNotAFragment() {
        ContentObservable.bindFragment("not a fragment", Observable.never());
    }

    @Test(expected = IllegalStateException.class)
    public void itThrowsIfObserverCallsFromFragmentFromBackgroundThread() throws Throwable {
        final Future<Object> future = Executors.newSingleThreadExecutor().submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                ContentObservable.bindFragment(fragment, Observable.empty());
                return null;
            }
        });
        try {
            future.get(1, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            throw e.getCause();
        }
    }

    @Test
    public void bindFragmentToSourceFromDifferentThread() throws InterruptedException {
        CountDownLatch done = new CountDownLatch(1);
        ContentObservable.bindFragment(fragment, TestUtil.atBackgroundThread(done)).subscribe(new TestObserver<String>(observer));
        done.await();

        Robolectric.runUiThreadTasksIncludingDelayedTasks();

        verify(observer).onNext(TestUtil.STRING_EXPECTATION);
        verify(observer).onCompleted();
    }

    @Test
    public void bindSupportFragmentToSourceFromDifferentThread() throws InterruptedException {
        CountDownLatch done = new CountDownLatch(1);
        ContentObservable.bindFragment(supportFragment, TestUtil.atBackgroundThread(done)).subscribe(new TestObserver<String>(observer));
        done.await();

        Robolectric.runUiThreadTasksIncludingDelayedTasks();

        verify(observer).onNext(TestUtil.STRING_EXPECTATION);
        verify(observer).onCompleted();
    }

    @Test(expected = IllegalStateException.class)
    public void itThrowsIfObserverCallsFromActivityFromBackgroundThread() throws Throwable {
        final Future<Object> future = Executors.newSingleThreadExecutor().submit(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                ContentObservable.bindActivity(activity, Observable.empty());
                return null;
            }
        });
        try {
            future.get(1, TimeUnit.SECONDS);
        } catch (ExecutionException e) {
            throw e.getCause();
        }
    }

    @Test
    public void bindActivityToSourceFromDifferentThread() throws InterruptedException {
        CountDownLatch done = new CountDownLatch(1);
        ContentObservable.bindActivity(activity, TestUtil.atBackgroundThread(done)).subscribe(new TestObserver<String>(observer));
        done.await();

        Robolectric.runUiThreadTasksIncludingDelayedTasks();

        verify(observer).onNext(TestUtil.STRING_EXPECTATION);
        verify(observer).onCompleted();
    }


    public void givenCursorWhenFromCursorInvokedThenObservableCallsOnNextWhileHasNext() {
        final Subscriber<Cursor> subscriber = spy(new TestSubscriber<Cursor>());
        final Cursor cursor = mock(Cursor.class);

        when(cursor.isAfterLast()).thenReturn(false, false, true);
        when(cursor.moveToNext()).thenReturn(true, true, false);
        when(cursor.getCount()).thenReturn(2);

        Observable<Cursor> observable = ContentObservable.fromCursor(cursor);
        observable.subscribe(subscriber);

        verify(subscriber, times(2)).onNext(cursor);
        verify(subscriber, never()).onError(Matchers.any(Throwable.class));
        verify(subscriber).onCompleted();
    }

    @Test
    public void givenEmptyCursorWhenFromCursorInvokedThenObservableCompletesWithoutCallingOnNext() {
        final Subscriber<Cursor> subscriber = spy(new TestSubscriber<Cursor>());
        final Cursor cursor = mock(Cursor.class);

        Observable<Cursor> observable = ContentObservable.fromCursor(cursor);
        observable.subscribe(subscriber);

        verify(subscriber, never()).onNext(cursor);
        verify(subscriber, never()).onError(Matchers.any(Throwable.class));
        verify(subscriber).onCompleted();
    }

    @Test
    public void givenCursorWhenFromCursorCalledThenEmitsAndClosesCursorAfterCompletion() {
        final Subscriber<Cursor> subscriber = spy(new TestSubscriber<Cursor>());
        final Cursor cursor = mock(Cursor.class);

        when(cursor.isAfterLast()).thenReturn(false, true);
        when(cursor.moveToNext()).thenReturn(true, false);
        when(cursor.getCount()).thenReturn(1);

        Observable<Cursor> observable = ContentObservable.fromCursor(cursor);
        observable.subscribe(subscriber);

        verify(subscriber, never()).onError(Matchers.any(Throwable.class));
        verify(subscriber).onNext(cursor);
        verify(cursor).close();
        verify(subscriber).onCompleted();
    }

    @Test
    public void givenCursorWhenFromCursorCalledThenEmitsAndClosesCursorAfterError() {
        final Subscriber<Cursor> subscriber = spy(new TestSubscriber<Cursor>());
        final Cursor cursor = mock(Cursor.class);
        final RuntimeException throwable = mock(RuntimeException.class);
        doThrow(throwable).when(subscriber).onNext(cursor);

        when(cursor.isAfterLast()).thenReturn(false, true);
        when(cursor.moveToNext()).thenReturn(true, false);
        when(cursor.getCount()).thenReturn(1);

        Observable<Cursor> observable = ContentObservable.fromCursor(cursor);
        observable.subscribe(subscriber);

        verify(subscriber, never()).onCompleted();
        verify(subscriber).onNext(cursor);
        verify(subscriber).onError(throwable);
        verify(cursor).close();
    }
}
