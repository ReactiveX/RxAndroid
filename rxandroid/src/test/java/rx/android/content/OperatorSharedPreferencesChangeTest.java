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

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import rx.Observable;
import rx.Observer;
import rx.Subscription;
import rx.observers.TestObserver;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
public class OperatorSharedPreferencesChangeTest {

    @Test
    public void testSharedPreferences() {
        Application application = Robolectric.application;
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application);
        Observable<String> observable = ContentObservable.fromSharedPreferencesChanges(sharedPreferences);
        final Observer<String> observer = mock(Observer.class);
        final Subscription subscription = observable.subscribe(new TestObserver<String>(observer));

        final InOrder inOrder = inOrder(observer);

        inOrder.verify(observer, never()).onNext(any(String.class));

        sharedPreferences.edit().putBoolean("a", true).commit();
        inOrder.verify(observer, times(1)).onNext("a");

        sharedPreferences.edit().putInt("b", 9).commit();
        inOrder.verify(observer, times(1)).onNext("b");

        subscription.unsubscribe();

        sharedPreferences.edit().putInt("c", 42).commit();
        inOrder.verify(observer, never()).onNext(any(String.class));
        inOrder.verify(observer, never()).onError(any(Throwable.class));
        inOrder.verify(observer, never()).onCompleted();
    }

}
