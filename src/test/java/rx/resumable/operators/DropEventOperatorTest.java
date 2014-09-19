package rx.resumable.operators;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import rx.Observable;
import rx.Observer;
import rx.Subscription;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class DropEventOperatorTest {

    @Mock
    private EventForwardingListener mockListener;
    @Mock
    private Observer<String> mockObserver;

    private DropEventOperator<String> dropEventOperator;
    private Observable<String> observable;

    @Before
    public void setUp() {
        initMocks(this);
        dropEventOperator = new DropEventOperator<String>(mockListener);
        observable = Observable.create(dropEventOperator);
    }


    @Test
    public void itForwardsEventsWhenRegistered() {
        observable.subscribe(mockObserver);

        dropEventOperator.onNext("this");
        dropEventOperator.onCompleted();

        verify(mockObserver).onNext("this");
        verify(mockObserver).onCompleted();
    }

    @Test
    public void itForwardsErrorsWhenRegistered() {
        observable.subscribe(mockObserver);

        Exception throwable = new Exception();
        dropEventOperator.onError(throwable);

        verify(mockObserver).onError(throwable);
    }

    @Test
    public void itUnregisterWhenAsked() {
        Subscription subscription = observable.subscribe(mockObserver);

        subscription.unsubscribe();
        dropEventOperator.onNext("this");

        verify(mockObserver, never()).onNext(anyString());
    }

    @Test
    public void itDropsValuesWhileUnregistered() {
        Subscription subscription = observable.subscribe(mockObserver);

        subscription.unsubscribe();
        dropEventOperator.onNext("this");
        observable.subscribe(mockObserver);
        dropEventOperator.onNext("that");

        verify(mockObserver, never()).onNext("this");
        verify(mockObserver).onNext("that");
    }
}