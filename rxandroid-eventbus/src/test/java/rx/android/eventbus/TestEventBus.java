package rx.android.eventbus;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import rx.Observer;
import rx.Subscription;
import rx.observers.TestObserver;
import rx.subjects.Subject;

import java.util.Collection;
import java.util.List;

public class TestEventBus implements EventBus {

    private final EventBus eventBus = new DefaultEventBus();
    private final Multimap<Queue, Observer> observedQueues = HashMultimap.create();
    private final Multimap<Queue, Subscription> subscriptions = HashMultimap.create();

    private <T> FluentIterable<T> internalEventsOn(Queue<T> queue) {
        return FluentIterable.from(observedQueues.get(queue))
                .filter(TestObserver.class)
                .transformAndConcat(new Function<TestObserver, Iterable<T>>() {
                    @Override
                    public Iterable<T> apply(TestObserver input) {
                        return input.getOnNextEvents();
                    }
                });
    }

    public <T> List<T> eventsOn(Queue<T> queue) {
        return internalEventsOn(queue).toList();
    }

    public <T> List<T> eventsOn(Queue<T> queue, Predicate<T> filter) {
        return internalEventsOn(queue).filter(filter).toList();
    }

    public <T> T firstEventOn(Queue<T> queue) {
        final List<T> events = this.eventsOn(queue);
        assertFalse("Attempted to access first event on queue " + queue + ", but no events fired", events.isEmpty());
        return events.get(0);
    }

    public <T> T lastEventOn(Queue<T> queue) {
        final List<T> events = this.eventsOn(queue);
        assertFalse("Attempted to access last event on queue " + queue + ", but no events fired", events.isEmpty());
        return Iterables.getLast(events);
    }

    public <T> void verifyNoEventsOn(Queue<T> queue) {
        final List<T> events = eventsOn(queue);
        assertTrue("Expected no events on queue " + queue + ", but found these events:\n" + events, events.isEmpty());
    }

    public <T> void verifyUnsubscribed(Queue<T> queue) {
        final Collection<Subscription> seenSubscriptions = subscriptions.get(queue);
        assertFalse("Expected to be unsubscribed from queue " + queue + ", but was never subscribed",
                seenSubscriptions.isEmpty());
        assertTrue("Expected to be unsubscribed from queue " + queue, areAllUnsubscribed(seenSubscriptions));
    }

    public void verifyUnsubscribed() {
        final Collection<Subscription> seenSubscriptions = subscriptions.values();
        assertFalse("Expected to be unsubscribed from all queues, but was never subscribed to any",
                seenSubscriptions.isEmpty());
        assertTrue("Expected to be unsubscribed from all queues, but found " + seenSubscriptions.size() + " subscriptions",
                areAllUnsubscribed(seenSubscriptions));
    }

    private boolean areAllUnsubscribed(Collection<Subscription> subscriptionCollection) {
        return FluentIterable.from(subscriptionCollection).allMatch(new Predicate<Subscription>() {
            @Override
            public boolean apply(Subscription input) {
                return input.isUnsubscribed();
            }
        });
    }

    @Override
    public <T> Subscription subscribe(Queue<T> queue, Observer<T> observer) {
        final Subscription subscription = eventBus.subscribe(queue, observer);
        subscriptions.put(queue, subscription);
        return subscription;
    }

    @Override
    public <T> Subscription subscribeImmediate(Queue<T> queue, Observer<T> observer) {
        return subscribe(queue, observer);
    }

    @Override
    public <T> void publish(Queue<T> queue, T event) {
        monitorQueue(queue);
        eventBus.publish(queue, event);
    }

    @Override
    public <T> Subject<T, T> queue(Queue<T> queue) {
        monitorQueue(queue);
        return eventBus.queue(queue);
    }

    private <T> void monitorQueue(Queue<T> queue) {
        if (!observedQueues.containsKey(queue)) {
            final Observer<T> testObserver = new TestObserver<T>();
            eventBus.subscribe(queue, testObserver);
            observedQueues.put(queue, testObserver);
        }
    }
}
