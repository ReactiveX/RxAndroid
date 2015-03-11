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

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;

import rx.Observable;
import rx.Subscriber;

/**
 * A variation of {@link rx.android.content.OnSubscribeCursor} that makes a query in addition to
 * emitting a {@link android.database.Cursor} for every available position.
 */
final class OnSubscribeQuery implements Observable.OnSubscribe<Cursor> {

    private final ContentResolver contentResolver;
    private final Uri uri;
    private final String[] projection;
    private final String selection;
    private final String[] selectionArgs;
    private final String orderBy;

    OnSubscribeQuery(ContentResolver contentResolver, Uri uri, String[] projection,
        String selection, String[] selectionArgs, String orderBy) {
        this.contentResolver = contentResolver;
        this.uri = uri;
        this.projection = projection;
        this.selection = selection;
        this.selectionArgs = selectionArgs;
        this.orderBy = orderBy;
    }

    @Override
    public void call(final Subscriber<? super Cursor> subscriber) {
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(uri, projection, selection, selectionArgs, orderBy);
            while (!subscriber.isUnsubscribed() && cursor.moveToNext()) {
                subscriber.onNext(cursor);
            }
            if (!subscriber.isUnsubscribed()) {
                subscriber.onCompleted();
            }
        } catch (Throwable e) {
            if (!subscriber.isUnsubscribed()) {
                subscriber.onError(e);
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
            }
        }
    }

}
