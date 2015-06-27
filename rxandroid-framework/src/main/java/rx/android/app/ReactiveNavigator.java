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
import android.content.Intent;

import com.google.auto.value.AutoValue;

import rx.Observable;
import rx.android.exception.NavigationFailedException;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

/**
 * Allow to navigate using startActivityForResult in a reactive way.
 * You get an observable for the ActivityResult and can observe without having to filter your request code.
 * You need to hook the Activity onActivityResult call to this object to make it work.
 */
public class ReactiveNavigator {

    private static final PublishSubject<ActivityResult> onActivityResultSubject = PublishSubject.create();
    private final ActivityStarter activityStarter;

    public ReactiveNavigator(ActivityStarter activityStarter) {
        this.activityStarter = activityStarter;
    }

    /**
     * Start an activity and observe the result using an Observable
     *
     * @param intent      The request intent for which you intend to start the activity
     * @param requestCode The request code to identify your request
     * @return An observable of the Intent data response
     */
    public Observable<ActivityResult> activityResult(final Intent intent, final int requestCode) {
        return Observable.defer(new DeferedActivityNavigation(activityStarter, intent, requestCode))
                .filter(new RequestCodeFilter(requestCode)).first();
    }

    /**
     * The entry points for the onActivityResult events.
     * You need to call this method with the values received from onActivityResult from you activity/fragment to populate the observables.
     *
     * @param requestCode The request code from the response
     * @param resultCode  The response code from the action
     * @param data        The data returned by the action
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        onActivityResultSubject.onNext(ActivityResult.create(requestCode, resultCode, data));
    }

    /**
     * A class to wrap the values returned by the Android framework onActivityResult.
     */
    @AutoValue
    public abstract static class ActivityResult {
        public abstract int requestCode();

        public abstract int resultCode();

        public abstract Intent data();

        public static ActivityResult create(final int requestCode, final int resultCode, final Intent data) {
            return new AutoValue_ReactiveNavigator_ActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * A function to flatmap in order to simplify the result handling from the Wrapped observable.
     * It will ignore cancel events and treat and non OK Result code as an error.
     */
    public final static class FuncUnwrap implements Func1<ActivityResult, Observable<Intent>> {
        @Override
        public Observable<Intent> call(ActivityResult activityResult) {
            switch (activityResult.resultCode()) {
                case Activity.RESULT_OK:
                    return Observable.just(activityResult.data());
                case Activity.RESULT_CANCELED:
                    return Observable.empty();
                default:
                    return Observable.error(new NavigationFailedException(activityResult.resultCode()));
            }
        }
    }

    private static class DeferedActivityNavigation implements Func0<Observable<ActivityResult>> {

        private final Intent intent;
        private final int requestCode;

        private ActivityStarter activityStarter;

        public DeferedActivityNavigation(ActivityStarter activityStarter, Intent intent, int requestCode) {
            this.activityStarter = activityStarter;
            this.intent = intent;
            this.requestCode = requestCode;
        }

        @Override
        public Observable<ActivityResult> call() {
            activityStarter.startActivityForResult(intent, requestCode);
            //Free the reference to the activity once we have started the navigation, this avoids leaking the reference to the activity.
            activityStarter = null;
            return onActivityResultSubject;
        }
    }

    private static class RequestCodeFilter implements Func1<ActivityResult, Boolean> {
        private final int requestCode;

        public RequestCodeFilter(int requestCode) {
            this.requestCode = requestCode;
        }

        @Override
        public Boolean call(ActivityResult activityResult) {
            return activityResult.requestCode() == requestCode;
        }
    }
}
