package rx.android.eventbus;

import rx.Observer;
import rx.Subscription;
import rx.subjects.Subject;

public interface EventBus {

    /**
     * Subscribes <code>observer</code> to <code>queue</code>.
     * <p>
     * This variant always delivers notifications on the Android main thread.
     */
    <T> Subscription subscribe(EventQueue<T> queue, Observer<T> observer);

    /**
     * Subscribes <code>observer</code> to <code>queue</code>.
     * <p>
     * Unlike {@link #subscribe(EventQueue, Observer)}, this variant delivers notifications
     * on the same thread as the event source.
     */
    <T> Subscription subscribeImmediate(EventQueue<T> queue, Observer<T> observer);

    /**
     * Publishes an event by putting it on the given <code>queue</code>.
     */
    <T> void publish(EventQueue<T> queue, T event);

    /**
     * @return The Rx {@link Subject} backing the given event <code>queue</code>.
     */
    <T> Subject<T, T> queue(EventQueue<T> queue);
}
