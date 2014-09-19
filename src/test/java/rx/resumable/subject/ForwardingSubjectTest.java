package rx.resumable.subject;

import org.junit.Test;

import rx.resumable.operators.ObserverOperator;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class ForwardingSubjectTest {

    @Test
    public void itForwardsEventsToTheOperator() {
        ObserverOperator<String>  mockOperator = mock(ObserverOperator.class);
        ForwardingSubject<String> forwardingSubject = new ForwardingSubject<String>(mockOperator);

        forwardingSubject.onNext("this");
        forwardingSubject.onCompleted();

        verify(mockOperator).onNext("this");
        verify(mockOperator).onCompleted();
    }

    @Test
    public void itForwardsErrorsToTheOperator() {
        ObserverOperator<String> mockOperator = mock(ObserverOperator.class);
        ForwardingSubject<String> forwardingSubject = new ForwardingSubject<String>(mockOperator);

        Throwable throwable = new Throwable();
        forwardingSubject.onError(throwable);

        verify(mockOperator).onError(throwable);
    }
}
