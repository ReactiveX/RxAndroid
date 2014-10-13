package rx.resumable;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import rx.Subscriber;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

public class SubscriberVaultTest {

    private SubscriberVault subscriberVault;

    @Before
    public void setUp() {
        subscriberVault = new SubscriberVault();
    }

    @Test
    public void itStoresValuesForAGivenKey() {
        Subscriber mockSubscriber = mock(Subscriber.class);
        UUID id = subscriberVault.store(mockSubscriber);

        assertSame(subscriberVault.get(id), mockSubscriber);
    }

    @Test
    public void itRemovesValuesForAGivenKey() {
        Subscriber mockSubscriber = mock(Subscriber.class);
        UUID id = subscriberVault.store(mockSubscriber);
        subscriberVault.remove(id);

        assertNull(subscriberVault.get(id));
    }
}