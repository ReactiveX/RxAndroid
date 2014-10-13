/**
 * Copyright 2014 Netflix, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package rx.android.observables;

import static rx.android.schedulers.AndroidSchedulers.mainThread;

import rx.Observable;
import rx.functions.Func1;
import rx.operators.OperatorBroadcastRegister;
import rx.operators.OperatorConditionalBinding;
import rx.operators.OperatorLocalBroadcastRegister;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;


public final class AndroidObservable {

    private static final boolean USES_SUPPORT_FRAGMENTS;

    static {
        boolean supportFragmentsAvailable = false;
        try {
            Class.forName("android.support.v4.app.Fragment");
            supportFragmentsAvailable = true;
        } catch (ClassNotFoundException e) {
        }
        USES_SUPPORT_FRAGMENTS = supportFragmentsAvailable;
    }

    private static final Func1<Activity, Boolean> ACTIVITY_VALIDATOR = new Func1<Activity, Boolean>() {
        @Override
        public Boolean call(Activity activity) {
            return !activity.isFinishing();
        }
    };

    private static final Func1<Fragment, Boolean> FRAGMENT_VALIDATOR = new Func1<Fragment, Boolean>() {
        @Override
        public Boolean call(Fragment fragment) {
            return fragment.isAdded() && !fragment.getActivity().isFinishing();
        }
    };

    private static final Func1<android.support.v4.app.Fragment, Boolean> FRAGMENTV4_VALIDATOR =
            new Func1<android.support.v4.app.Fragment, Boolean>() {
                @Override
                public Boolean call(android.support.v4.app.Fragment fragment) {
                    return fragment.isAdded() && !fragment.getActivity().isFinishing();
                }
            };

    private AndroidObservable() {
    }

    /**
     * Binds the given source sequence to an activity.
     * <p/>
     * This helper will schedule the given sequence to be observed on the main UI thread and ensure
     * that no notifications will be forwarded to the activity in case it is scheduled to finish.
     * <p/>
     * You should unsubscribe from the returned Observable in onDestroy at the latest, in order to not
     * leak the activity or an inner subscriber. Conversely, when the source sequence can outlive the activity,
     * make sure to bind to new instances of the activity again, e.g. after going through configuration changes.
     * Refer to the samples project for actual examples.
     *
     * @param activity the activity to bind the source sequence to
     * @param source   the source sequence
     */
    public static <T> Observable<T> bindActivity(Activity activity, Observable<T> source) {
        Assertions.assertUiThread();
        return source.observeOn(mainThread()).lift(new OperatorConditionalBinding<T, Activity>(activity, ACTIVITY_VALIDATOR));
    }

    /**
     * Binds the given source sequence to a fragment (native or support-v4).
     * <p/>
     * This helper will schedule the given sequence to be observed on the main UI thread and ensure
     * that no notifications will be forwarded to the fragment in case it gets detached from its
     * activity or the activity is scheduled to finish.
     * <p/>
     * You should unsubscribe from the returned Observable in onDestroy for normal fragments, or in onDestroyView
     * for retained fragments, in order to not leak any references to the host activity or the fragment.
     * Refer to the samples project for actual examples.
     *
     * @param fragment the fragment to bind the source sequence to
     * @param source   the source sequence
     */
    public static <T> Observable<T> bindFragment(Object fragment, Observable<T> source) {
        Assertions.assertUiThread();
        final Observable<T> o = source.observeOn(mainThread());
        if (USES_SUPPORT_FRAGMENTS && fragment instanceof android.support.v4.app.Fragment) {
            android.support.v4.app.Fragment f = (android.support.v4.app.Fragment) fragment;
            return o.lift(new OperatorConditionalBinding<T, android.support.v4.app.Fragment>(f, FRAGMENTV4_VALIDATOR));
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && fragment instanceof Fragment) {
            Fragment f = (Fragment) fragment;
            return o.lift(new OperatorConditionalBinding<T, Fragment>(f, FRAGMENT_VALIDATOR));
        } else {
            throw new IllegalArgumentException("Target fragment is neither a native nor support library Fragment");
        }
    }

    /**
     * Create Observable that wraps BroadcastReceiver and emmit received intents.
     *
     * @param filter Selects the Intent broadcasts to be received.
     */
    public static Observable<Intent> fromBroadcast(Context context, IntentFilter filter){
        return Observable.create(new OperatorBroadcastRegister(context, filter, null, null));
    }

    /**
     * Create Observable that wraps BroadcastReceiver and emmit received intents.
     *
     * @param filter Selects the Intent broadcasts to be received.
     * @param broadcastPermission String naming a permissions that a
     *      broadcaster must hold in order to send an Intent to you.  If null,
     *      no permission is required.
     * @param schedulerHandler Handler identifying the thread that will receive
     *      the Intent.  If null, the main thread of the process will be used.
     */
    public static Observable<Intent> fromBroadcast(Context context, IntentFilter filter, String broadcastPermission, Handler schedulerHandler){
        return Observable.create(new OperatorBroadcastRegister(context, filter, broadcastPermission, schedulerHandler));
    }

    /**
     * Create Observable that wraps BroadcastReceiver and connects to LocalBroadcastManager
     * to emmit received intents.
     *
     * @param filter Selects the Intent broadcasts to be received.
     */
    public static Observable<Intent> fromLocalBroadcast(Context context, IntentFilter filter){
        return Observable.create(new OperatorLocalBroadcastRegister(context, filter));
    }
}
