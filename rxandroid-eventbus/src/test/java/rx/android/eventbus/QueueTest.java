package rx.android.eventbus;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.hamcrest.Matchers;
import org.junit.Test;
import rx.functions.Action1;
import rx.functions.Actions;

public class QueueTest {

    @Test
    public void verifyEqualsAndHashCodeContract() {
        EqualsVerifier.forClass(Queue.class).verify();
    }

    @Test
    public void shouldBuildDefaultQueue() {
        final Queue<String> stringQueue = Queue.of(String.class).get();
        assertThat(stringQueue.name, is("StringQueue"));
        assertThat(stringQueue.replayLast, is(false));
        assertThat(stringQueue.defaultEvent, is(nullValue()));
    }

    @Test
    public void shouldBuildNamedQueue() {
        final Queue<String> stringQueue = Queue.of(String.class).name("custom").get();
        assertThat(stringQueue.name, is("custom"));
        assertThat(stringQueue.replayLast, is(false));
        assertThat(stringQueue.defaultEvent, is(nullValue()));
    }

    @Test
    public void shouldBuildReplayQueue() {
        final Queue<String> stringQueue = Queue.of(String.class).replay().get();
        assertThat(stringQueue.replayLast, is(true));
    }

    @Test
    public void shouldBuildReplayQueueWithDefaultEvent() {
        final Queue<String> stringQueue = Queue.of(String.class).replay("def").get();
        assertThat(stringQueue.replayLast, is(true));
        assertThat(stringQueue.defaultEvent, is("def"));
    }

    @Test
    public void shouldDefaultErrorHookToEmptyAction() {
        final Queue<String> stringQueue = Queue.of(String.class).get();
        assertThat(stringQueue.onError, Matchers.<Action1<?>>is(Actions.empty()));
    }

    @Test
    public void shouldBuildQueueWithCustomErrorHook() {
        Action1<Throwable> hook = mock(Action1.class);
        final Queue<String> stringQueue = Queue.of(String.class).onError(hook).get();
        assertThat(stringQueue.onError, is(hook));
    }
}