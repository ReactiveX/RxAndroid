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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;

import rx.Observable;
import rx.Observer;
import rx.android.exception.NavigationFailedException;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static rx.android.app.ReactiveNavigator.ActivityResult;

@RunWith(RobolectricTestRunner.class)
public class ReactiveNavigatorTest {

    @Mock
    private ActivityStarter mockActivityStarter;
    @Mock
    private Intent mockIntent;
    @Mock
    private Intent mockData;
    @Mock
    private Observer<ActivityResult> mockObserver;
    @Mock
    private Observer<Intent> mockIntentObserver;


    private ReactiveNavigator reactiveNavigator;

    @Before
    public void setUp() {
        initMocks(this);
        reactiveNavigator = new ReactiveNavigator(mockActivityStarter);
    }

    @Test
    public void itReturnsAllValuesWrapped() {
        ArgumentCaptor<ReactiveNavigator.ActivityResult> argumentCaptor = ArgumentCaptor.forClass(ActivityResult.class);
        Observable<ActivityResult> observable = reactiveNavigator.activityResult(mockIntent, 42);
        observable.subscribe(mockObserver);

        reactiveNavigator.onActivityResult(42, Activity.RESULT_OK, mockData);
        verify(mockObserver).onNext(argumentCaptor.capture());
        ActivityResult value = argumentCaptor.getValue();

        assertEquals(Activity.RESULT_OK, value.resultCode());
        assertEquals(mockData, value.data());
    }

    @Test
    public void itIgnoresEventsFromOtherRequestCodes() {
        Observable<ActivityResult> observable = reactiveNavigator.activityResult(mockIntent, 42);
        observable.subscribe(mockObserver);

        reactiveNavigator.onActivityResult(24, Activity.RESULT_CANCELED, mockData);
        verify(mockObserver, never()).onNext(any(ActivityResult.class));
    }

    @Test
    public void unwrappedObservableSendsIntendDataToObserver() {
        Observable<Intent> observable = reactiveNavigator.activityResult(mockIntent, 42)
                .flatMap(new ReactiveNavigator.FuncUnwrap());
        observable.subscribe(mockIntentObserver);

        reactiveNavigator.onActivityResult(42, Activity.RESULT_OK, mockData);

        verify(mockIntentObserver).onNext(mockData);
    }

    @Test
    public void unwrappedObservableIgnoresCancelledCancelledEvents() {
        Observable<Intent> observable = reactiveNavigator.activityResult(mockIntent, 42)
                .flatMap(new ReactiveNavigator.FuncUnwrap());
        observable.subscribe(mockIntentObserver);

        reactiveNavigator.onActivityResult(42, Activity.RESULT_CANCELED, mockData);

        verify(mockIntentObserver, never()).onNext(any(Intent.class));
    }

    @Test
    public void unwrappedObservableFailsIfUnkownResponseCode() {
        Observable<Intent> observable = reactiveNavigator.activityResult(mockIntent, 42)
                .flatMap(new ReactiveNavigator.FuncUnwrap());
        observable.subscribe(mockIntentObserver);
        reactiveNavigator.onActivityResult(42, 24, mockData);

        verify(mockIntentObserver).onError(any(NavigationFailedException.class));
    }
}