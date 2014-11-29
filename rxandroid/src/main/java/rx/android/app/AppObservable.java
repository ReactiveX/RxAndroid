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
package rx.android.app;

import android.app.Activity;
import android.app.Fragment;
import android.os.Build;

import rx.Observable;
import rx.android.internal.Assertions;
import rx.functions.Func1;

import static rx.android.schedulers.AndroidSchedulers.mainThread;

/**
 * @deprecated The functionality of this class is being removed as it was determined to not
 * provide enough value. Despite binding observables to an activity or fragment lifecycle, they
 * only prevented items from emitting after 'pause'. The contents of the subscription were still
 * referenced and had to be managed separately otherwise memory leaks would occur. Since you have
 * manage the subscription anyway, these bindings are redundant.
 */
@Deprecated
public final class AppObservable {
    private AppObservable() {
        throw new AssertionError("No instances");
    }

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
    public static final boolean USES_SUPPORT_FRAGMENTS;

    /**
     * @deprecated The functionality of this class is being removed as it was determined to not
     * provide enough value. Despite binding observables to an activity or fragment lifecycle, they
     * only prevented items from emitting after 'pause'. The contents of the subscription were still
     * referenced and had to be managed separately otherwise memory leaks would occur. Since you have
     * manage the subscription anyway, these bindings are redundant.
     */
    @Deprecated
    public static <T> Observable<T> bindActivity(Activity activity, Observable<T> source) {
        Assertions.assertUiThread();
        return source.observeOn(mainThread()).lift(new OperatorConditionalBinding<T, Activity>(activity, ACTIVITY_VALIDATOR));
    }

    /**
     * @deprecated The functionality of this class is being removed as it was determined to not
     * provide enough value. Despite binding observables to an activity or fragment lifecycle, they
     * only prevented items from emitting after 'pause'. The contents of the subscription were still
     * referenced and had to be managed separately otherwise memory leaks would occur. Since you have
     * manage the subscription anyway, these bindings are redundant.
     */
    @Deprecated
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
}
