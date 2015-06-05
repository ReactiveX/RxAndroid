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
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowContentResolver;

import rx.Observable;
import rx.Subscriber;
import rx.observers.TestSubscriber;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static rx.android.content.ContentObservable.fromQuery;


@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class OnSubscribeQueryTest {

    private static final String AUTHORITY = "rx.android";
    private static final Uri URI = Uri.parse("content://" + AUTHORITY + "/path");

    private ContentResolver contentResolver;
    private TestContentProvider provider;

    @Before
    public void setUp() throws Exception {
        this.contentResolver = new Activity().getContentResolver();
        this.provider = new TestContentProvider();
        ShadowContentResolver.registerProvider(AUTHORITY, provider);
    }

    @After
    public void tearDown() throws Exception {
        ShadowContentResolver.registerProvider(AUTHORITY, null);
    }

    @Test
    public void givenCursorWhenFromQueryInvokedThenObservableCallsOnNextWhileHasNext() {
        final Subscriber<Cursor> subscriber = spy(new TestSubscriber<Cursor>());
        final Cursor cursor = mock(Cursor.class);
        provider.cursor = cursor;

        when(cursor.isAfterLast()).thenReturn(false, false, true);
        when(cursor.moveToNext()).thenReturn(true, true, false);
        when(cursor.getCount()).thenReturn(2);

        Observable<Cursor> observable = fromQuery(contentResolver, URI, null, null, null, null);
        observable.subscribe(subscriber);

        verify(subscriber, times(2)).onNext(cursor);
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber).onCompleted();
    }

    @Test
    public void givenEmptyCursorWhenFromQueryInvokedThenObservableCompletesWithoutCallingOnNext() {
        final Subscriber<Cursor> subscriber = spy(new TestSubscriber<Cursor>());
        final Cursor cursor = mock(Cursor.class);
        provider.cursor = cursor;

        Observable<Cursor> observable = fromQuery(contentResolver, URI, null, null, null, null);
        observable.subscribe(subscriber);

        verify(subscriber, never()).onNext(cursor);
        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber).onCompleted();
    }

    @Test
    public void givenCursorWhenFromQueryCalledThenEmitsAndClosesCursorAfterCompletion() {
        final Subscriber<Cursor> subscriber = spy(new TestSubscriber<Cursor>());
        final Cursor cursor = mock(Cursor.class);
        provider.cursor = cursor;

        when(cursor.isAfterLast()).thenReturn(false, true);
        when(cursor.moveToNext()).thenReturn(true, false);
        when(cursor.getCount()).thenReturn(1);

        Observable<Cursor> observable = fromQuery(contentResolver, URI, null, null, null, null);
        observable.subscribe(subscriber);

        verify(subscriber, never()).onError(any(Throwable.class));
        verify(subscriber).onNext(cursor);
        verify(cursor).close();
        verify(subscriber).onCompleted();
    }

    @Test
    public void givenCursorWhenFromQueryCalledThenEmitsAndClosesCursorAfterSubscriberError() {
        final Subscriber<Cursor> subscriber = spy(new TestSubscriber<Cursor>());
        final Cursor cursor = mock(Cursor.class);
        provider.cursor = cursor;
        final RuntimeException throwable = mock(RuntimeException.class);
        doThrow(throwable).when(subscriber).onNext(cursor);

        when(cursor.isAfterLast()).thenReturn(false, true);
        when(cursor.moveToNext()).thenReturn(true, false);
        when(cursor.getCount()).thenReturn(1);

        Observable<Cursor> observable = fromQuery(contentResolver, URI, null, null, null, null);
        observable.subscribe(subscriber);

        verify(subscriber, never()).onCompleted();
        verify(subscriber).onNext(cursor);
        verify(subscriber).onError(throwable);
        verify(cursor).close();
    }

    @Test
    public void givenCursorWhenFromQueryCalledThenEmitsAndClosesCursorAfterObservableError() {
        final Subscriber<Cursor> subscriber = spy(new TestSubscriber<Cursor>());
        final Cursor cursor = mock(Cursor.class);
        provider.cursor = cursor;
        final RuntimeException throwable = mock(RuntimeException.class);

        when(cursor.isAfterLast()).thenReturn(false, false, true);
        when(cursor.moveToNext()).thenReturn(true).thenThrow(throwable);
        when(cursor.getCount()).thenReturn(2);

        Observable<Cursor> observable = fromQuery(contentResolver, URI, null, null, null, null);
        observable.subscribe(subscriber);

        verify(subscriber, never()).onCompleted();
        verify(subscriber).onNext(cursor);
        verify(subscriber).onError(throwable);
        verify(cursor).close();
    }

    static class TestContentProvider extends ContentProvider {
        private Cursor cursor;

        @Override public boolean onCreate() {
            return false;
        }

        @Override
        public Cursor query(Uri uri, String[] projection, String selection,
            String[] selectionArgs, String sortOrder) {
            return cursor;
        }

        @Override public String getType(Uri uri) {
            return null;
        }

        @Override public Uri insert(Uri uri, ContentValues values) {
            return null;
        }

        @Override public int delete(Uri uri, String selection, String[] selectionArgs) {
            return 0;
        }

        @Override
        public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
            return 0;
        }
    }

}
