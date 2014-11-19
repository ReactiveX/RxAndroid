package rx.android.eventbus;

import rx.functions.Action1;
import rx.functions.Actions;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A descriptor type representing an event queue. Use {@link rx.android.eventbus.Queue.Builder} to obtain
 * instances of this class:
 * <pre>
 *     Queue<MyEventType> queue = Queue.of(MyEventType.class).get();
 * </pre>
 * There are several ways to modify the queue behavior:
 * <h3>Replaying queues</h3>
 * If by the time a subscriber subscribes to a queue you want the last event the queue has seen to be
 * emitted immediately to the subscriber, you can create a replaying queue, optionally specifying an event
 * instance that is emitted in case there were zero events prior to subscribing:
 * <pre>
 *     Queue<MyEventType> queue1 = Queue.of(MyEventType.class).replay().get();
 *     Queue<MyEventType> queue2 = Queue.of(MyEventType.class).replay(MyEventType.DEFAULT_EVENT).get();
 * </pre>
 * <h3>Custom error hooks</h3>
 * Event queues do not process <code>onError</code> notifications (or completed events) as this would lead
 * to the queue being closed and entering a terminal state. However, you can be notified of error by
 * providing a custom hook:
 * <pre>
 *     Queue<MyEventType> queue = Queue.of(MyEventType.class).onError(myErrorHook).get();
 * </pre>
 * A convenient way to group event queues together is in a sealed container class:
 * <pre>
 *     public final class EventQueue {
 *         public static final Queue<Event1> EVENT1 = Queue.of(Event1.class).get();
 *         public static final Queue<Event2> EVENT2 = Queue.of(Event2.class).get();
 *         ...
 *     }
 * </pre>
 */
public final class Queue<T> {

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

        public Queue<T> get() {
            if (name == null) {
                name = eventType.getSimpleName() + "Queue";
            }
            return new Queue<T>(name, eventType, replayLast, defaultEvent, onError);
        }
    }

    public static <T> Builder<T> of(Class<T> eventType) {
        return new Builder<T>(eventType);
    }

    Queue(String name, Class<T> eventType, boolean replayLast, T defaultEvent, Action1<Throwable> onError) {
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
        return (that != null && that instanceof Queue && ((Queue) that).id == this.id);
    }

    @Override
    public String toString() {
        return this.name + "[" + this.eventType.getCanonicalName() + "]";
    }
}
