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
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

class OnSubscribeContentObserverRegister implements Observable.OnSubscribe<Uri> {

    private final ContentResolver contentResolver;
    private final Uri uri;
    private final Handler schedulerHandler;

    public OnSubscribeContentObserverRegister(ContentResolver contentResolver, Uri uri,
        Handler schedulerHandler) {
        this.contentResolver = contentResolver;
        this.uri = uri;
        this.schedulerHandler = schedulerHandler;
    }

    @Override
    public void call(final Subscriber<? super Uri> subscriber) {
        final ContentObserver contentObserver = new ContentObserver(schedulerHandler) {
            @Override public void onChange(boolean selfChange) {
                subscriber.onNext(uri);
            }

            /*@Override*/
            public void onChange(boolean selfChange, Uri uri) {
                subscriber.onNext(uri);
            }
        };

        final Subscription subscription = Subscriptions.create(new Action0() {
            @Override
            public void call() {
                contentResolver.unregisterContentObserver(contentObserver);
            }
        });

        subscriber.add(subscription);
        contentResolver.registerContentObserver(uri, true, contentObserver);
    }

}