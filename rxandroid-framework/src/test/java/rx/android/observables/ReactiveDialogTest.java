/**
 * Copyright 2014 Novoda, Inc.
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

import android.app.Activity;
import android.app.FragmentManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowDialog;

import rx.Observable;
import rx.Observer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static rx.android.observables.ReactiveDialog.Result;

@RunWith(RobolectricTestRunner.class)
public class ReactiveDialogTest {

    @Mock
    private Observer<String> mockObserver;

    @Mock
    private Observer<Result<String>> mockResultObserver;

    private ReactiveDialog<String> reactiveDialog;
    private FragmentManager fragmentManager;

    @Before
    public void setUp() {
        initMocks(this);
        reactiveDialog = new ReactiveDialog<String>();
        Activity activity = Robolectric
                .buildActivity(Activity.class).create().start()
                .resume().get();
        fragmentManager = activity.getFragmentManager();
    }

    @Test
    public void itSendsListenerEventToObserver() {
        ArgumentCaptor<Result> argumentCaptor = ArgumentCaptor.forClass(Result.class);
        Observable<Result<String>> observable = reactiveDialog.show(fragmentManager);
        observable.subscribe(mockResultObserver);

        reactiveDialog.getListener().onCompleteWith("this");
        verify(mockResultObserver).onNext(argumentCaptor.capture());
        Result result = argumentCaptor.getValue();

        assertNotNull(ShadowDialog.getLatestDialog());
        assertFalse(result.isCanceled());
        assertEquals(result.getValue(), "this");
    }

    @Test
    public void itSendsCanceledResultIfDialogIsCanceledToObserver() {
        ArgumentCaptor<Result> argumentCaptor = ArgumentCaptor.forClass(Result.class);
        Observable<Result<String>> observable = reactiveDialog.show(fragmentManager);
        observable.subscribe(mockResultObserver);

        reactiveDialog.getListener().onCancel();
        verify(mockResultObserver).onNext(argumentCaptor.capture());

        assertNotNull(ShadowDialog.getLatestDialog());
        assertTrue(argumentCaptor.getValue().isCanceled());
    }

    @Test
    public void unwrappedObservableSendsListenerEventToObserver() {
        Observable<String> observable = reactiveDialog.showIgnoringCancelEvents(fragmentManager);
        observable.subscribe(mockObserver);

        reactiveDialog.getListener().onCompleteWith("this");

        assertNotNull(ShadowDialog.getLatestDialog());
        verify(mockObserver).onNext("this");
    }

    @Test
    public void unwrappedObservableIgnoresCanceledCanceledEvents() {
        Observable<String> observable = reactiveDialog.showIgnoringCancelEvents(fragmentManager);
        observable.subscribe(mockObserver);

        reactiveDialog.getListener().onCancel();

        assertNotNull(ShadowDialog.getLatestDialog());
        verify(mockObserver, never()).onNext(anyString());
    }

    @Test
    public void itSendsListenerErrorsToObserver() {
        Observable<Result<String>> observable = reactiveDialog.show(fragmentManager);
        observable.subscribe(mockResultObserver);
        Throwable throwable = new Throwable();

        reactiveDialog.getListener().onError(throwable);

        assertNotNull(ShadowDialog.getLatestDialog());
        verify(mockResultObserver).onError(throwable);
    }

    @Test(expected = IllegalStateException.class)
    public void itFailsIfDeliverAfterCompletion() {
        Observable<Result<String>> observable = reactiveDialog.show(fragmentManager);
        observable.subscribe(mockResultObserver);

        reactiveDialog.getListener().onCompleted();
        reactiveDialog.getListener().onNext("this");
    }
}
