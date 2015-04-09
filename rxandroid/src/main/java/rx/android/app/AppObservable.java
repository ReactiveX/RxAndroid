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
import rx.Observable;
import rx.android.internal.Assertions;
import rx.functions.Func1;

import static rx.android.schedulers.AndroidSchedulers.mainThread;

public final class AppObservable {
    private AppObservable() {
        throw new AssertionError("No instances");
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

    /**
     * Binds the given source sequence to an activity.
     * <p>
     * This helper will schedule the given sequence to be observed on the main UI thread and ensure
     * that no notifications will be forwarded to the activity in case it is scheduled to finish.
     * <p>
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
        return source.observeOn(mainThread())
                .lift(new OperatorConditionalBinding<T, Activity>(activity, ACTIVITY_VALIDATOR));
    }

    /**
     * Binds the given source sequence to a fragment.
     * <p>
     * This helper will schedule the given sequence to be observed on the main UI thread and ensure
     * that no notifications will be forwarded to the fragment in case it gets detached from its
     * activity or the activity is scheduled to finish.
     * <p>
     * You should unsubscribe from the returned Observable in onDestroy for normal fragments, or in onDestroyView
     * for retained fragments, in order to not leak any references to the host activity or the fragment.
     * Refer to the samples project for actual examples.
     *
     * @param fragment the fragment to bind the source sequence to
     * @param source   the source sequence
     */
    public static <T> Observable<T> bindFragment(Fragment fragment, Observable<T> source) {
        Assertions.assertUiThread();
        return source.observeOn(mainThread())
                .lift(new OperatorConditionalBinding<T, Fragment>(fragment, FRAGMENT_VALIDATOR));
    }

    /**
     * Binds the given source sequence to a support-v4 fragment.
     *
     * @see #bindFragment(Fragment, Observable)
     */
    public static <T> Observable<T> bindSupportFragment(android.support.v4.app.Fragment fragment, Observable<T> source) {
        Assertions.assertUiThread();
        return source.observeOn(mainThread())
                .lift(new OperatorConditionalBinding<T, android.support.v4.app.Fragment>(fragment, FRAGMENTV4_VALIDATOR));
    }
}
