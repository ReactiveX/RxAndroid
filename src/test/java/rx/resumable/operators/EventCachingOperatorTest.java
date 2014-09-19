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

public class EventCachingOperatorTest {

    @Mock
    private EventForwardingListener mockListener;
    @Mock
    private Observer<String> mockObserver;

    private EventCachingOperator<String> eventCachingOperator;
    private Observable<String> observable;

    @Before
    public void setUp() {
        initMocks(this);
        eventCachingOperator = new EventCachingOperator<String>(mockListener);
        observable = Observable.create(eventCachingOperator);
    }

    @Test
    public void itForwardsEventsWhenRegistered() {
        observable.subscribe(mockObserver);

        eventCachingOperator.onNext("this");
        eventCachingOperator.onCompleted();

        verify(mockObserver).onNext("this");
        verify(mockObserver).onCompleted();
    }

    @Test
    public void itForwardsErrorsWhenRegistered() {
        observable.subscribe(mockObserver);

        Exception throwable = new Exception();
        eventCachingOperator.onError(throwable);

        verify(mockObserver).onError(throwable);
    }

    @Test
    public void itUnregisterWhenAsked() {
        Subscription subscription = observable.subscribe(mockObserver);

        subscription.unsubscribe();
        eventCachingOperator.onNext("this");

        verify(mockObserver, never()).onNext(anyString());
    }

    @Test
    public void itCachesValuesWhileUnregistered() {
        Subscription subscription = observable.subscribe(mockObserver);

        subscription.unsubscribe();
        eventCachingOperator.onNext("this");
        observable.subscribe(mockObserver);
        eventCachingOperator.onNext("that");

        verify(mockObserver).onNext("this");
        verify(mockObserver).onNext("that");
    }

    @Test
    public void itSendCachedValuesWhenReSubscribed() {
        Subscription subscription = observable.subscribe(mockObserver);

        subscription.unsubscribe();
        eventCachingOperator.onCompleted();
        observable.subscribe(mockObserver);

        verify(mockObserver).onCompleted();
    }

    @Test
    public void itCachesErrorsWhileUnregistered() {
        Subscription subscription = observable.subscribe(mockObserver);

        subscription.unsubscribe();
        Throwable throwable = new Throwable();
        eventCachingOperator.onError(throwable);
        observable.subscribe(mockObserver);

        verify(mockObserver).onError(throwable);
    }

}