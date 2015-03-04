package rx.android.app.support;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
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

import static org.junit.Assert.*;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(RobolectricTestRunner.class)
public class ReactiveDialogTest {

    @Mock
    private Observer<String> mockObserver;

    @Mock
    private Observer<ReactiveDialog.Result<String>> mockResultObserver;

    private ReactiveDialog<String> reactiveDialog;
    private FragmentManager fragmentManager;

    @Before
    public void setUp() {
        initMocks(this);
        reactiveDialog = new ReactiveDialog<String>();
        FragmentActivity activity = Robolectric
                .buildActivity(FragmentActivity.class).create().start()
                .resume().get();
        fragmentManager = activity.getSupportFragmentManager();
    }

    @Test
    public void itSendsListenerEventToObserver() {
        ArgumentCaptor<ReactiveDialog.Result> argumentCaptor = ArgumentCaptor.forClass(ReactiveDialog.Result.class);
        Observable<ReactiveDialog.Result<String>> observable = reactiveDialog.show(fragmentManager);
        observable.subscribe(mockResultObserver);

        reactiveDialog.getListener().onCompleteWith("this");
        verify(mockResultObserver).onNext(argumentCaptor.capture());
        ReactiveDialog.Result result = argumentCaptor.getValue();

        assertNotNull(ShadowDialog.getLatestDialog());
        assertFalse(result.isCanceled());
        assertEquals(result.getValue(), "this");
    }

    @Test
    public void itSendsCanceledResultIfDialogIsCanceledToObserver() {
        ArgumentCaptor<ReactiveDialog.Result> argumentCaptor = ArgumentCaptor.forClass(ReactiveDialog.Result.class);
        Observable<ReactiveDialog.Result<String>> observable = reactiveDialog.show(fragmentManager);
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
        Observable<ReactiveDialog.Result<String>> observable = reactiveDialog.show(fragmentManager);
        observable.subscribe(mockResultObserver);
        Throwable throwable = new Throwable();

        reactiveDialog.getListener().onError(throwable);

        assertNotNull(ShadowDialog.getLatestDialog());
        verify(mockResultObserver).onError(throwable);
    }

    @Test(expected = IllegalStateException.class)
    public void itFailsIfDeliverAfterCompletion() {
        Observable<ReactiveDialog.Result<String>> observable = reactiveDialog.show(fragmentManager);
        observable.subscribe(mockResultObserver);

        reactiveDialog.getListener().onCompleted();
        reactiveDialog.getListener().onNext("this");
    }
}
