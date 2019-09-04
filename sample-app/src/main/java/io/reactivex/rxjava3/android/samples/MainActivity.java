/*
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
package io.reactivex.rxjava3.android.samples;

import android.app.Activity;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.functions.Supplier;
import io.reactivex.rxjava3.observers.DisposableObserver;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class MainActivity extends Activity {
    private static final String TAG = "RxAndroidSamples";

    private final CompositeDisposable disposables = new CompositeDisposable();

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        findViewById(R.id.button_run_scheduler).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                onRunSchedulerExampleButtonClicked();
            }
        });
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        disposables.clear();
    }

    void onRunSchedulerExampleButtonClicked() {
        disposables.add(sampleObservable()
            // Run on a background thread
            .subscribeOn(Schedulers.io())
            // Be notified on the main thread
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(new DisposableObserver<String>() {
                @Override public void onComplete() {
                    Log.d(TAG, "onComplete()");
                }

                @Override public void onError(Throwable e) {
                    Log.e(TAG, "onError()", e);
                }

                @Override public void onNext(String string) {
                    Log.d(TAG, "onNext(" + string + ")");
                }
            }));
    }

    static Observable<String> sampleObservable() {
        return Observable.defer(new Supplier<ObservableSource<? extends String>>() {
            @Override public ObservableSource<? extends String> get() throws Throwable {
                // Do some long running operation
                SystemClock.sleep(5000);
                return Observable.just("one", "two", "three", "four", "five");
            }
        });
    }
}
