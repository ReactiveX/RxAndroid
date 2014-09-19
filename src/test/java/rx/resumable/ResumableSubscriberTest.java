package rx.resumable;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import rx.Observable;
import rx.Subscriber;
import rx.resumable.observer.ResumableObserver;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class ResumableSubscriberTest {

    private ResumableReference ref = new ResumableReference() {
        @Override
        public int getResumableId() {
            return 42;
        }
    };

    private ObservableVault vault = new ObservableVault();

    @Mock
    private ObserverFactory mockFactory;
    @Mock
    private ResumableObserver<String> mockObserver;
    @Mock
    private ResumableObserver<String> mockObserver2;


    private ResumableSubscriber resumableSubscriber;
    private TestOperator testOperator;

    @Before
    public void setUp() {
        initMocks(this);
        testOperator = new TestOperator();
        resumableSubscriber = new ResumableSubscriber(ref, mockFactory, vault);
        when(mockObserver.getId()).thenReturn(24);
        when(mockFactory.createObserver(24)).thenReturn(mockObserver2);
    }

    @Test
    public void itSubscribesObserver() {
        Observable<String> observable = Observable.create(testOperator);
        resumableSubscriber.subscribe(observable, mockObserver);
        testOperator.getSubscriber().onNext("this");

        verify(mockObserver).onNext("this");
    }

    @Test
    public void itUnsubscribesObserverOnPause() {
        Observable<String> observable = Observable.create(testOperator);
        resumableSubscriber.subscribe(observable, mockObserver);
        resumableSubscriber.pause();
        testOperator.getSubscriber().onNext("this");

        verify(mockObserver, never()).onNext("this");
    }

    @Test
    public void itSubscribesUsingANewObserverOnResume() {
        Observable<String> observable = Observable.create(testOperator);
        resumableSubscriber.subscribe(observable, mockObserver);
        resumableSubscriber.pause();
        resumableSubscriber.resume();
        testOperator.getSubscriber().onNext("this");

        verify(mockObserver, never()).onNext("this");
        verify(mockObserver2).onNext("this");
    }

    @Test
    public void itSubscribesWithCachingByDefault() {
        Observable<String> observable = Observable.create(testOperator);
        resumableSubscriber.subscribe(observable, mockObserver);
        resumableSubscriber.pause();
        testOperator.getSubscriber().onNext("this");
        resumableSubscriber.resume();

        verify(mockObserver, never()).onNext("this");
        verify(mockObserver2).onNext("this");
    }

    @Test
    public void itSubscribesWithDropIfSpecified() {
        Observable<String> observable = Observable.create(testOperator);
        resumableSubscriber.subscribeWithDrop(observable, mockObserver);
        resumableSubscriber.pause();
        testOperator.getSubscriber().onNext("this");
        resumableSubscriber.resume();

        verify(mockObserver, never()).onNext("this");
        verify(mockObserver2, never()).onNext("this");
    }

    @Test
    public void itSubscribesWithReplayIfSpecified() {
        Observable<String> observable = Observable.create(testOperator);
        resumableSubscriber.subscribeWithReplay(observable, mockObserver);
        testOperator.getSubscriber().onNext("this");
        testOperator.getSubscriber().onNext("that");
        resumableSubscriber.pause();
        resumableSubscriber.resume();

        verify(mockObserver).onNext("this");
        verify(mockObserver).onNext("that");
        verify(mockObserver2).onNext("this");
        verify(mockObserver2).onNext("that");
    }

    @Test
    public void itCachesEventWhenUsingReplay() {
        Observable<String> observable = Observable.create(testOperator);
        resumableSubscriber.subscribeWithReplay(observable, mockObserver);
        testOperator.getSubscriber().onNext("this");
        resumableSubscriber.pause();
        testOperator.getSubscriber().onNext("that");
        resumableSubscriber.resume();

        verify(mockObserver).onNext("this");
        verify(mockObserver2).onNext("this");
        verify(mockObserver2).onNext("that");
    }

    @Test
    public void itUnsubscribesManually() {
        Observable<String> observable = Observable.create(testOperator);
        resumableSubscriber.subscribeWithReplay(observable, mockObserver);
        testOperator.getSubscriber().onNext("this");
        resumableSubscriber.unsubscribe(mockObserver.getId());
        testOperator.getSubscriber().onNext("that");

        verify(mockObserver).onNext("this");
        verify(mockObserver, never()).onNext("that");
    }

    private static class TestOperator implements Observable.OnSubscribe<String> {

        private Subscriber<? super String> subscriber;

        @Override
        public void call(Subscriber<? super String> subscriber) {
            this.subscriber = subscriber;
        }

        public Subscriber<? super String> getSubscriber() {
            return subscriber;
        }
    }
}