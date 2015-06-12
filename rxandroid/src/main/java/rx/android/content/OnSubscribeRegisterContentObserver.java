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
import android.database.ContentObserver;
import android.net.Uri;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

/**
 * Created by henrytao on 6/9/15.
 */
public class OnSubscribeRegisterContentObserver implements Observable.OnSubscribe<Boolean> {

  private final Context mContext;

  private final Uri mUri;

  private final boolean mNotifyForDescendents;

  public OnSubscribeRegisterContentObserver(Context context, Uri uri, boolean notifyForDescendents) {
    mContext = context;
    mUri = uri;
    mNotifyForDescendents = notifyForDescendents;
  }

  @Override
  public void call(final Subscriber<? super Boolean> subscriber) {
    final ContentObserver observer = new ContentObserver(null) {

      @Override
      public void onChange(boolean selfChange) {
        subscriber.onNext(selfChange);
      }
    };

    final Subscription subscription = Subscriptions.create(new Action0() {
      @Override
      public void call() {
        if (observer != null) {
          mContext.getContentResolver().unregisterContentObserver(observer);
        }
      }
    });
    subscriber.add(subscription);
    mContext.getContentResolver().registerContentObserver(mUri, mNotifyForDescendents, observer);
  }

}
