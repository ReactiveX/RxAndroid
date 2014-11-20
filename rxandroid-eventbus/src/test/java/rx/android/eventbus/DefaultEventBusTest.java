package rx.android.eventbus;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import rx.Observer;
import rx.Subscription;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class DefaultEventBusTest {

    private static final EventQueue<String> TEST_DEFAULT_QUEUE = EventQueue.build(String.class).get();
    private static final EventQueue<String> TEST_REPLAY_QUEUE = EventQueue.build(String.class).replay().get();
    private static final EventQueue<String> TEST_REPLAY_QUEUE_WITH_DEFAULT = EventQueue.build(String.class).replay("first!").get();

    private DefaultEventBus eventBus = new DefaultEventBus();

    @Mock private Observer<String> observer1;
    @Mock private Observer<String> observer2;
    @Mock private Observer<Integer> intObserver;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldLazilyCreateEventQueuesWhenFirstAccessingThem() {
        assertThat(eventBus.queue(TEST_DEFAULT_QUEUE), is(notNullValue()));
    }

    @Test
    public void shouldPublishEventsToSubscribers() {
        eventBus.subscribe(TEST_DEFAULT_QUEUE, observer1);
        eventBus.subscribe(TEST_DEFAULT_QUEUE, observer2);
        eventBus.publish(TEST_DEFAULT_QUEUE, "hello!");

        verify(observer1).onNext("hello!");
        verify(observer2).onNext("hello!");
    }

    @Test
    public void shouldNotPublishEventsToSubscribersAfterUnsubscribing() {
        final Subscription subscription = eventBus.subscribe(TEST_DEFAULT_QUEUE, observer1);
        eventBus.subscribe(TEST_DEFAULT_QUEUE, observer2);

        eventBus.publish(TEST_DEFAULT_QUEUE, "hello!");
        subscription.unsubscribe();

        eventBus.publish(TEST_DEFAULT_QUEUE, "world!");

        verify(observer1).onNext("hello!");
        verifyNoMoreInteractions(observer1);
        verify(observer2).onNext("hello!");
        verify(observer2).onNext("world!");
    }

    @Test
    public void shouldEmitDefaultEventOnConnectionToReplayQueue() {
        eventBus.subscribe(TEST_REPLAY_QUEUE_WITH_DEFAULT, observer1);
        verify(observer1).onNext("first!");
        verifyNoMoreInteractions(observer1);
    }

    @Test
    public void shouldEmitLastValueOnConnectionToReplayQueue() throws Exception {
        eventBus.publish(TEST_REPLAY_QUEUE, "1");
        eventBus.publish(TEST_REPLAY_QUEUE, "2");
        eventBus.subscribe(TEST_REPLAY_QUEUE, observer1);
        verify(observer1).onNext("2");
        verifyNoMoreInteractions(observer1);
    }

    @Test
    public void shouldEmitSubsequentValuesWhenConnectedToReplayQueue() throws Exception {
        eventBus.publish(TEST_REPLAY_QUEUE, "1");
        eventBus.subscribe(TEST_REPLAY_QUEUE, observer1);
        eventBus.publish(TEST_REPLAY_QUEUE, "2");
        verify(observer1).onNext("2");
    }
}
