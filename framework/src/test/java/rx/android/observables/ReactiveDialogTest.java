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
import org.mockito.Mock;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowDialog;

import rx.Observable;
import rx.Observer;
import rx.android.exception.CancelledException;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(RobolectricTestRunner.class)
public class ReactiveDialogTest {

    @Mock
    private Observer<String> mockObserver;

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
        Observable<String> observable = reactiveDialog.show(fragmentManager);
        observable.subscribe(mockObserver);

        reactiveDialog.getListener().onCompleteWith("this");

        assertNotNull(ShadowDialog.getLatestDialog());
        verify(mockObserver).onNext("this");
        verify(mockObserver).onCompleted();
    }

    @Test()
    public void itTreatsCancelAsAnExceptionForObserver() {
        Observable<String> observable = reactiveDialog.show(fragmentManager);
        observable.subscribe(mockObserver);

        reactiveDialog.getListener().onCancel();

        assertNotNull(ShadowDialog.getLatestDialog());
        verify(mockObserver).onError(any(CancelledException.class));
    }

    @Test()
    public void itSendsListenerErrorsToObserver() {
        Observable<String> observable = reactiveDialog.show(fragmentManager);
        observable.subscribe(mockObserver);
        Throwable throwable = new Throwable();

        reactiveDialog.getListener().onError(throwable);

        assertNotNull(ShadowDialog.getLatestDialog());
        verify(mockObserver).onError(throwable);
    }

    @Test(expected = IllegalStateException.class)
    public void itFailsIfDeliverAfterCompletion() {
        Observable<String> observable = reactiveDialog.show(fragmentManager);
        observable.subscribe(mockObserver);

        reactiveDialog.getListener().onCompleted();
        reactiveDialog.getListener().onNext("this");
    }
}