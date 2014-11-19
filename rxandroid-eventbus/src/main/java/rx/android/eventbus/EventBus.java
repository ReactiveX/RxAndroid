package rx.android.eventbus;

import rx.Observer;
import rx.Subscription;
import rx.subjects.Subject;

public interface EventBus {

    <T> Subscription subscribe(Queue<T> queue, Observer<T> observer);

    <T> Subscription subscribeImmediate(Queue<T> queue, Observer<T> observer);

    <T> void publish(Queue<T> queue, T event);

    <T> Subject<T, T> queue(Queue<T> queue);
}
