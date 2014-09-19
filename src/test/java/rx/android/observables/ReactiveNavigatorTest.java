package rx.android.observables;

import android.app.Activity;
import android.content.Intent;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;

import rx.Observable;
import rx.Observer;
import rx.android.exception.CancelledException;
import rx.android.exception.FailedException;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(RobolectricTestRunner.class)
public class ReactiveNavigatorTest {

    @Mock
    private ActivityStarter mockActivityStarter;
    @Mock
    private Intent mockIntent;
    @Mock
    private Intent mockData;
    @Mock
    private Observer<Intent> mockObserver;


    private ReactiveNavigator reactiveNavigator;

    @Before
    public void setUp() {
        initMocks(this);
        reactiveNavigator = new ReactiveNavigator(mockActivityStarter);
    }

    @Test
    public void itReturnsIntentDataIfSuccess() {
        Observable<Intent> observable = reactiveNavigator.toActivityForResult(mockIntent, 42);
        observable.subscribe(mockObserver);
        reactiveNavigator.onActivityResult(42, Activity.RESULT_OK, mockData);

        verify(mockObserver).onNext(mockData);
    }

    @Test
    public void itFailsIfUserCancelled() {
        Observable<Intent> observable = reactiveNavigator.toActivityForResult(mockIntent, 42);
        observable.subscribe(mockObserver);
        reactiveNavigator.onActivityResult(42, Activity.RESULT_CANCELED, mockData);

        verify(mockObserver).onError(any(CancelledException.class));
    }

    @Test
    public void itFailsIfUnkownResponseCode() {
        Observable<Intent> observable = reactiveNavigator.toActivityForResult(mockIntent, 42);
        observable.subscribe(mockObserver);
        reactiveNavigator.onActivityResult(42, 24, mockData);

        verify(mockObserver).onError(any(FailedException.class));
    }
}