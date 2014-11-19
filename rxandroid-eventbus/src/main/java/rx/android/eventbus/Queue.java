package rx.android.eventbus;

import rx.functions.Action1;
import rx.functions.Actions;

import java.util.concurrent.atomic.AtomicInteger;

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
