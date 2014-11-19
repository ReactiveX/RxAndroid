package rx.android.eventbus;

import rx.Observer;
import rx.Subscription;
import rx.subjects.Subject;

public interface EventBus {

    /**
     * Subscribes <code>observer</code> to <code>queue</code>.
     * <p/>
     * This variant always delivers notifications on the Android main thread.
     */
    <T> Subscription subscribe(Queue<T> queue, Observer<T> observer);

    /**
     * Subscribes <code>observer</code> to <code>queue</code>.
     * <p/>
     * Unlike {@link #subscribe(Queue, rx.Observer)}, this variant delivers notifications
     * on the same thread as the event source.
     */
    <T> Subscription subscribeImmediate(Queue<T> queue, Observer<T> observer);

    /**
     * Publishes an event by putting it on the given <code>queue</code>.
     */
    <T> void publish(Queue<T> queue, T event);

    /**
     * @return The Rx {@link rx.subjects.Subject} backing the given event <code>queue</code>.
     */
    <T> Subject<T, T> queue(Queue<T> queue);
}
