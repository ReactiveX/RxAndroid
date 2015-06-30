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

import android.database.Cursor;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func0;

/**
 * Emits a {@link android.database.Cursor} for every available position.
 */
final class OnSubscribeCursor implements Observable.OnSubscribe<Cursor> {

    private Func0<Cursor> cursorQuery;

    OnSubscribeCursor(Func0<Cursor> cursorQuery) {
        this.cursorQuery = cursorQuery;
    }

    @Override
    public void call(final Subscriber<? super Cursor> subscriber) {
        try {
            Cursor cursor = cursorQuery.call();
            try {
                while (!subscriber.isUnsubscribed() && cursor.moveToNext()) {
                    subscriber.onNext(cursor);
                }
                if (!subscriber.isUnsubscribed()) {
                    subscriber.onCompleted();
                }
            } finally {
                if (!cursor.isClosed()) {
                    cursor.close();
                }
            }
        } catch (Throwable e) {
            if (!subscriber.isUnsubscribed()) {
                subscriber.onError(e);
            }
        }
    }
}
