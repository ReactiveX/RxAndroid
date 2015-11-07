package rx.android.samples;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.schedulers.HandlerScheduler;
import rx.exceptions.OnErrorThrowable;
import rx.functions.Action0;
import rx.functions.Func0;
import rx.schedulers.Schedulers;

import static android.os.Process.THREAD_PRIORITY_BACKGROUND;

public class MainActivity extends Activity {
    private static final String TAG = "RxAndroidSamples";

    private Handler backgroundHandler;
    private int counter;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity);

        BackgroundThread backgroundThread = new BackgroundThread();
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());

        findViewById(R.id.scheduler_example).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                onRunSchedulerExampleButtonClicked();
            }
        });
        findViewById(R.id.fast_path_example).setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                int count = counter++;
                onRunFastPathExample(count, false);
                onRunFastPathExample(count, true);
            }
        });
    }

    void onRunSchedulerExampleButtonClicked() {
        sampleObservable()
                // Run on a background thread
                .subscribeOn(HandlerScheduler.from(backgroundHandler))
                // Be notified on the main thread
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override public void onCompleted() {
                        Log.d(TAG, "onCompleted()");
                    }

                    @Override public void onError(Throwable e) {
                        Log.e(TAG, "onError()", e);
                    }

                    @Override public void onNext(String string) {
                        Log.d(TAG, "onNext(" + string + ")");
                    }
                });
    }

    void onRunFastPathExample(final int count, final boolean fastPathEnabled) {
        final String name = count + "-" + (fastPathEnabled ? "Fast path" : "Non-fast path");
        final Scheduler observeOn = fastPathEnabled
                ? AndroidSchedulers.mainThreadFastPath()
                : AndroidSchedulers.mainThread();
        fastPathSampleObservable()
                // Run on a background thread
                .subscribeOn(HandlerScheduler.from(backgroundHandler))
                // Be notified on the main thread
                .observeOn(observeOn)
                .subscribe(new Subscriber<String>() {
                    @Override public void onCompleted() {
                        Log.d(TAG, String.format("onCompleted(%s)", name));
                    }

                    @Override public void onError(Throwable e) {
                        Log.e(TAG, "onError()", e);
                    }

                    @Override public void onNext(String string) {
                        string = String.format("%s posted at: %s, now is: %d", name, string,
                                System.currentTimeMillis());
                        Log.d(TAG, "onNext(" + string + ")");
                    }
                });
    }

    static Observable<String> sampleObservable() {
        return Observable.defer(new Func0<Observable<String>>() {
            @Override public Observable<String> call() {
                try {
                    // Do some long running operation
                    Thread.sleep(TimeUnit.SECONDS.toMillis(5));
                } catch (InterruptedException e) {
                    throw OnErrorThrowable.from(e);
                }
                return Observable.just("one", "two", "three", "four", "five");
            }
        });
    }

    static Observable<String> fastPathSampleObservable() {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override public void call(final Subscriber<? super String> subscriber) {
                new SomeThirdPartyNetworkCall(new Callback<String>() {
                    @Override public void onResult(String s) {
                        // Calling the subscriber from the main thread
                        subscriber.onNext(s);
                        subscriber.onCompleted();
                    }
                }).execute();
            }
        });
    }

    static class BackgroundThread extends HandlerThread {
        BackgroundThread() {
            super("SchedulerSample-BackgroundThread", THREAD_PRIORITY_BACKGROUND);
        }
    }

    interface Callback<T> {
        void onResult(T t);
    }

    static class SomeThirdPartyNetworkCall {
        private final Handler handler = new Handler(Looper.getMainLooper());
        private final Callback<String> callback;

        private SomeThirdPartyNetworkCall(Callback<String> callback) {
            this.callback = callback;
        }

        void execute() {
            Schedulers.io().createWorker().schedule(new Action0() {
                @Override public void call() {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    handler.post(new Runnable() {
                        @Override public void run() {
                            // Simulate taking a bit of time on handler thread
                            try {
                                Thread.sleep(5);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            callback.onResult(Long.toString(System.currentTimeMillis()));
                        }
                    });
                }
            });
        }
    }
}
