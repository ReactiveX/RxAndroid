package rx.android.eventbus;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import rx.Observer;
import rx.functions.Action1;

public class DefaultEventSubjectTest {

    private DefaultEventSubject<Integer> subject = DefaultEventSubject.create();

    @Mock private Observer<Integer> observer1;
    @Mock private Observer<Integer> observer2;
    @Mock private Action1<Throwable> onError;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldBehaveLikePublishSubjectForOnNext() {
        subject.subscribe(observer1);
        subject.onNext(1);
        subject.subscribe(observer2);
        subject.onNext(2);

        verify(observer1).onNext(1);
        verify(observer1).onNext(2);
        verifyNoMoreInteractions(observer1);

        verify(observer2).onNext(2);
        verifyNoMoreInteractions(observer2);
    }

    @Test
    public void shouldNeverForwardOnCompletedToKeepTheQueueOpen() {
        subject.subscribe(observer1);
        subject.onCompleted();
        verifyZeroInteractions(observer1);
    }

    @Test
    public void shouldNeverForwardOnErrorToKeepTheQueueOpen() {
        subject.subscribe(observer1);
        subject.onError(new Exception());
        verifyZeroInteractions(observer1);
    }

    @Test
    public void shouldRunErrorsThroughCustomOnErrorHook() {
        subject = DefaultEventSubject.create(onError);
        final Exception e = new Exception();
        subject.onError(e);
        verify(onError).call(e);
    }
}