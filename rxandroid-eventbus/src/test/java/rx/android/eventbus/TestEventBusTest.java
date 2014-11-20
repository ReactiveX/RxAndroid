package rx.android.eventbus;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import rx.Observer;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class TestEventBusTest {

    private static final EventQueue<String> STRING_QUEUE = EventQueue.build(String.class).get();
    private static final EventQueue<Integer> INT_QUEUE = EventQueue.build(Integer.class).get();

    private TestEventBus eventBus = new TestEventBus();

    @Mock private Observer observer;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldRecordQueueEventsViaPublish() {
        eventBus.publish(STRING_QUEUE, "one");
        eventBus.publish(STRING_QUEUE, "two");
        eventBus.publish(INT_QUEUE, 1);
        eventBus.publish(EventQueue.build(Integer.class).get(), 1);

        assertThat(eventBus.eventsOn(STRING_QUEUE), contains("one", "two"));
        assertThat(eventBus.eventsOn(INT_QUEUE), contains(1));
    }

    @Test
    public void shouldRecordQueueEventsViaQueue() {
        eventBus.queue(STRING_QUEUE).onNext("one");
        eventBus.queue(STRING_QUEUE).onNext("two");
        eventBus.queue(INT_QUEUE).onNext(1);
        eventBus.queue(EventQueue.build(Integer.class).get()).onNext(1);

        assertThat(eventBus.eventsOn(STRING_QUEUE), contains("one", "two"));
        assertThat(eventBus.eventsOn(INT_QUEUE), contains(1));
    }

    @Test(expected = AssertionError.class)
    public void shouldFailWhenTryingToAccessFirstEventOnQueueButNeverFired() {
        eventBus.firstEventOn(STRING_QUEUE);
    }

    @Test(expected = AssertionError.class)
    public void shouldFailWhenTryingToAccessLastEventOnQueueButNeverFired() {
        eventBus.lastEventOn(STRING_QUEUE);
    }

    @Test(expected = AssertionError.class)
    public void shouldFailWhenNotExpectingEventOnQueueButEventFired() {
        eventBus.publish(STRING_QUEUE, "one");
        eventBus.verifyNoEventsOn(STRING_QUEUE);
    }

    @Test
    public void shouldPassWhenNotExpectingEventOnQueueAndDidNotFire() {
        try {
            eventBus.verifyNoEventsOn(STRING_QUEUE);
        } catch (AssertionError e) {
            fail();
        }
    }

    @Test(expected = AssertionError.class)
    public void shouldFailWhenExpectingToBeUnsubscribedFromQueueWasNeverSubscribed() {
        eventBus.verifyUnsubscribed(STRING_QUEUE);
    }

    @Test(expected = AssertionError.class)
    public void shouldFailWhenExpectingToBeUnsubscribedFromQueueButIsNot() {
        eventBus.subscribe(STRING_QUEUE, observer);
        eventBus.verifyUnsubscribed(STRING_QUEUE);
    }

    @Test
    public void shouldPassWhenExpectingToBeUnsubscribedFromQueueAndIsYesIndeed() {
        eventBus.subscribe(STRING_QUEUE, observer).unsubscribe();
        try {
            eventBus.verifyUnsubscribed(STRING_QUEUE);
        } catch (AssertionError e) {
            fail();
        }
    }

    @Test(expected = AssertionError.class)
    public void shouldFailWhenExpectingToBeUnsubscribedFromAllQueuesButWasNeverSubscribed() {
        eventBus.verifyUnsubscribed();
    }

    @Test(expected = AssertionError.class)
    public void shouldFailWhenExpectingToBeUnsubscribedFromAllQueuesButIsNot() {
        eventBus.subscribe(STRING_QUEUE, observer).unsubscribe();
        eventBus.subscribe(INT_QUEUE, observer);
        eventBus.verifyUnsubscribed();
    }

    @Test
    public void shouldPassWhenExpectingToBeUnsubscribedFromAllQueuesAndIsYesIndeedYep() {
        eventBus.subscribe(STRING_QUEUE, observer).unsubscribe();
        eventBus.subscribe(INT_QUEUE, observer).unsubscribe();
        try {
            eventBus.verifyUnsubscribed(STRING_QUEUE);
        } catch (AssertionError e) {
            fail();
        }
    }
}