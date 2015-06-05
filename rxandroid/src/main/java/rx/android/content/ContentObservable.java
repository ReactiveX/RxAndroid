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
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;

import rx.Observable;

public final class ContentObservable {
    private ContentObservable() {
        throw new AssertionError("No instances");
    }

    /**
     * Create Observable that wraps BroadcastReceiver and emits received intents.
     *
     * @param filter Selects the Intent broadcasts to be received.
     */
    public static Observable<Intent> fromBroadcast(Context context, IntentFilter filter){
        return Observable.create(new OnSubscribeBroadcastRegister(context, filter, null, null));
    }

    /**
     * Create Observable that wraps BroadcastReceiver and emits received intents.
     *
     * @param filter Selects the Intent broadcasts to be received.
     * @param broadcastPermission String naming a permissions that a
     *      broadcaster must hold in order to send an Intent to you.  If null,
     *      no permission is required.
     * @param schedulerHandler Handler identifying the thread that will receive
     *      the Intent.  If null, the main thread of the process will be used.
     */
    public static Observable<Intent> fromBroadcast(Context context, IntentFilter filter, String broadcastPermission, Handler schedulerHandler){
        return Observable.create(new OnSubscribeBroadcastRegister(context, filter, broadcastPermission, schedulerHandler));
    }

    /**
     * Create Observable that wraps BroadcastReceiver and connects to LocalBroadcastManager
     * to emit received intents.
     *
     * @param filter Selects the Intent broadcasts to be received.
     */
    public static Observable<Intent> fromLocalBroadcast(Context context, IntentFilter filter){
        return Observable.create(new OnSubscribeLocalBroadcastRegister(context, filter));
    }

    /**
     * Create Observable that emits String keys whenever it changes in provided SharedPreferences
     *
     * Items will be observed on the main Android UI thread
     */
    public static Observable<String> fromSharedPreferencesChanges(SharedPreferences sharedPreferences){
        return Observable.create(new OnSubscribeSharedPreferenceChange(sharedPreferences));
    }

    /**
     * Create Observable that emits the specified {@link android.database.Cursor} for each available position
     * of the cursor moving to the next position before each call and closing the cursor whether the
     * Observable completes or an error occurs.
     */
    public static Observable<Cursor> fromCursor(final Cursor cursor) {
        return Observable.create(new OnSubscribeCursor(cursor));
    }

    /**
     * Create Observable that makes a {@link android.content.ContentResolver} query and emits the
     * resulting {@link android.database.Cursor} for each available position.
     *
     * @param contentResolver
     * @param uri             The URI, using the content:// scheme, for the content to retrieve.
     * @param projection      A list of which columns to return.  Passing null will return all
     *                        columns, which is discouraged to prevent reading data from storage
     *                        that
     *                        isn't going to be used.
     * @param selection       A filter declaring which rows to return,
     *                        formatted as an SQL WHERE clause
     *                        (excluding the WHERE itself).  Passing null will return all rows
     *                        for the
     *                        given URI.
     * @param selectionArgs   You may include ?s in selection, which will be replaced by the values
     *                        from selectionArgs, in the order that they appear in the selection.
     *                        The values will be bound as Strings.
     * @param orderBy         How to order the rows, formatted as an SQL ORDER BY clause
     *                        (excluding the
     *                        ORDER BY itself).  Passing null will use the default sort order,
     *                        which may be unordered.
     * @return
     */
    public static Observable<Cursor> fromQuery(ContentResolver contentResolver, Uri uri,
        String[] projection, String selection, String[] selectionArgs, String orderBy) {
        return Observable.create(new OnSubscribeQuery(contentResolver, uri, projection,
            selection, selectionArgs, orderBy));
    }

}
