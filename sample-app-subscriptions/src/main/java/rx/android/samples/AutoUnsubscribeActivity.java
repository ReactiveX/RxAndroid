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
package rx.android.samples;

import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.widget.TextView;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.lifecycle.LifecycleEvent;
import rx.android.samples.subscriptions.R;
import rx.android.schedulers.HandlerScheduler;
import rx.exceptions.OnErrorThrowable;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

public class AutoUnsubscribeActivity extends RxActivity {
    private static final String TAG = AutoUnsubscribeActivity.class.getSimpleName();

    private Handler backgroundHandler;
    private TextView textView;
    private Subscription subscription;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        BackgroundThread backgroundThread = new BackgroundThread();
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
        textView = (TextView) findViewById(R.id.scheduler_example);
    }

    @Override protected void onStart() {
        super.onStart();
        subscription = subscriptions().with(neverEnding())
                .subscribeOn(HandlerScheduler.from(backgroundHandler))
                        // Observe until Stop
                .observeUntil(LifecycleEvent.STOP)
                        // Be notified on the main thread
                .observeOnMainThread()
                .subscribe(new Subscriber<String>() {
                    @Override public void onCompleted() {
                        Log.d(TAG, "onCompleted()");
                    }

                    @Override public void onError(Throwable e) {
                        Log.e(TAG, "onError()", e);
                    }

                    @Override public void onNext(String string) {
                        textView.setText(string);
                    }
                });
    }

    @Override protected void onPause() {
        super.onPause();
        // Should still be subscribed
        Log.d(TAG, "onPause unsubscribed? " + subscription.isUnsubscribed());
    }

    @Override protected void onStop() {
        super.onStop();
        // Should be unsubscribed
        Log.d(TAG, "onStop unsubscribed? " + subscription.isUnsubscribed());
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        backgroundHandler.getLooper().quit();
    }

    static Observable<String> neverEnding() {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override public void call(Subscriber<? super String> subscriber) {
                // Do a bunch of stuff until unsubscribed
                for (int i = 0; ; i++) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        throw OnErrorThrowable.from(e);
                    }
                    subscriber.onNext(Integer.toString(i));
                    if (subscriber.isUnsubscribed()) {
                        break;
                    }
                }
                Log.d(TAG, "unsubscribed. breaking out");
            }
        });
    }

    static class BackgroundThread extends HandlerThread {
        BackgroundThread() {
            super("SchedulerSample-BackgroundThread", THREAD_PRIORITY_BACKGROUND);
        }
    }
}
