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

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

/**
 * Created by henrytao on 6/9/15.
 */
public class OnSubscribeLoadCursor implements Observable.OnSubscribe<Cursor> {

  private final Context mContext;

  private final Uri mUri;

  private String[] mProjection;

  private String mSelection;

  private String[] mSelectionArgs;

  private String mSortOrder;

  public OnSubscribeLoadCursor(Context context, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
    mContext = context;
    mUri = uri;
    mProjection = projection;
    mSelection = selection;
    mSelectionArgs = selectionArgs;
    mSortOrder = sortOrder;
  }

  @Override
  public void call(Subscriber<? super Cursor> subscriber) {
    final Cursor cursor = mContext.getContentResolver().query(mUri, mProjection, mSelection, mSelectionArgs, mSortOrder);
    final Subscription subscription = Subscriptions.create(new Action0() {
      @Override
      public void call() {
        if (cursor != null) {
          cursor.close();
        }
      }
    });
    subscriber.add(subscription);
    subscriber.onNext(cursor);
  }

}
