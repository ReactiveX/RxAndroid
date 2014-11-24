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

import android.content.SharedPreferences;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.subscriptions.Subscriptions;

class OnSubscribeSharedPreferenceChange implements Observable.OnSubscribe<String>{

    private final SharedPreferences sharedPreferences;

    public OnSubscribeSharedPreferenceChange(SharedPreferences sharedPreferences) {
        this.sharedPreferences = sharedPreferences;
    }

    @Override
    public void call(final Subscriber<? super String> subscriber) {
        final SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                subscriber.onNext(key);
            }
        };

        subscriber.add(Subscriptions.create(new Action0() {
            @Override
            public void call() {
                sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener);
            }
        }));

        sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
    }
}
