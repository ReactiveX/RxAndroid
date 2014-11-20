package rx.android.eventbus;

import rx.functions.Action1;
import rx.functions.Actions;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A descriptor type representing an event queue. Use {@link Builder} to obtain
 * instances of this class:
 * <pre>
 *     EventQueue<MyEventType> queue = EventQueue.build(MyEventType.class).get();
 * </pre>
 * There are several ways to modify the queue behavior:
 * <h3>Replaying queues</h3>
 * If by the time a subscriber subscribes to a queue you want the last event the queue has seen to be
 * emitted immediately to the subscriber, you can create a replaying queue, optionally specifying an event
 * instance that is emitted in case there were zero events prior to subscribing:
 * <pre>
 *     EventQueue<MyEventType> queue1 = EventQueue.build(MyEventType.class).replay().get();
 *     EventQueue<MyEventType> queue2 = EventQueue.build(MyEventType.class).replay(MyEventType.DEFAULT_EVENT).get();
 * </pre>
 * <h3>Custom error hooks</h3>
 * Event queues do not process <code>onError</code> notifications (or completed events) as this would lead
 * to the queue being closed and entering a terminal state. However, you can be notified of error by
 * providing a custom hook:
 * <pre>
 *     EventQueue<MyEventType> queue = EventQueue.build(MyEventType.class).onError(myErrorHook).get();
 * </pre>
 * A convenient way to group event queues together is in a sealed container class:
 * <pre>
 *     public final class Events {
 *         public static final EventQueue<Event1> EVENT1 = EventQueue.build(Event1.class).get();
 *         public static final EventQueue<Event2> EVENT2 = EventQueue.build(Event2.class).get();
 *         ...
 *     }
 * </pre>
 */
public final class EventQueue<T> {

    private static final AtomicInteger runningId = new AtomicInteger();

    public final String name;
    public final Class<T> eventType;

    final int id;
    final boolean replayLast;
    final T defaultEvent;
    final Action1<Throwable> onError;

    public static final class Builder<T> {
        private String name;
        private final Class<T> eventType;
        private boolean replayLast;
        private T defaultEvent;
        private Action1<Throwable> onError = Actions.empty();

        Builder(Class<T> eventType) {
            this.eventType = eventType;
        }

        public Builder<T> name(String name) {
            this.name = name;
            return this;
        }

        public Builder<T> replay() {
            this.replayLast = true;
            return this;
        }

        public Builder<T> replay(T defaultEvent) {
            this.replayLast = true;
            this.defaultEvent = defaultEvent;
            return this;
        }

        public Builder<T> onError(Action1<Throwable> onError) {
            this.onError = onError;
            return this;
        }

        public EventQueue<T> get() {
            if (name == null) {
                name = eventType.getSimpleName() + "Queue";
            }
            return new EventQueue<T>(name, eventType, replayLast, defaultEvent, onError);
        }
    }

    public static <T> Builder<T> build(Class<T> eventType) {
        return new Builder<T>(eventType);
    }

    EventQueue(String name, Class<T> eventType, boolean replayLast, T defaultEvent, Action1<Throwable> onError) {
        this.name = name;
        this.eventType = eventType;
        this.replayLast = replayLast;
        this.defaultEvent = defaultEvent;
        this.onError = onError;
        this.id = runningId.getAndIncrement();
    }

    @Override
    public int hashCode() {
        return this.id;
    }

    @Override
    public boolean equals(Object that) {
        return (that != null && that instanceof EventQueue && ((EventQueue) that).id == this.id);
    }

    @Override
    public String toString() {
        return this.name + "[" + this.eventType.getCanonicalName() + "]";
    }
}
