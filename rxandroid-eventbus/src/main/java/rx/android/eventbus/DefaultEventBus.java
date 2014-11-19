package rx.android.eventbus;

import rx.Observer;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.Subject;

import android.util.Log;
import android.util.SparseArray;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.List;

/**
 * An {@link rx.android.eventbus.EventBus} implementation backed by lazy queues.
 */
public class DefaultEventBus implements EventBus {

    private static final String TAG = EventBus.class.getSimpleName();
    private static final boolean LOG_EVENTS = Log.isLoggable(TAG, Log.DEBUG);
    private static SparseArray<List<Reference<Observer<?>>>> loggedObservers;

    private final SparseArray<Subject<?, ?>> queues = new SparseArray<Subject<?, ?>>();

    static {
        if (LOG_EVENTS) {
            loggedObservers = new SparseArray<List<Reference<Observer<?>>>>();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> Subject<T, T> queue(Queue<T> queue) {
        Subject<T, T> subject = (Subject<T, T>) queues.get(queue.id);
        if (subject == null) {
            if (queue.defaultEvent != null) {
                subject = ReplayEventSubject.create(queue.defaultEvent, queue.onError);
            } else if (queue.replayLast) {
                subject = ReplayEventSubject.create(queue.onError);
            } else {
                subject = DefaultEventSubject.create(queue.onError);
            }
            queues.put(queue.id, subject);
        }
        return subject;
    }

    @Override
    public <T> Subscription subscribe(Queue<T> queue, Observer<T> observer) {
        if (LOG_EVENTS) {
            registerObserver(queue, observer);
        }
        return this.queue(queue).observeOn(AndroidSchedulers.mainThread()).subscribe(observer);
    }

    @Override
    public <T> Subscription subscribeImmediate(Queue<T> queue, Observer<T> observer) {
        if (LOG_EVENTS) {
            registerObserver(queue, observer);
        }
        return this.queue(queue).subscribe(observer);
    }

    @Override
    public <T> void publish(Queue<T> queue, T event) {
        if (LOG_EVENTS) {
            logEvent(queue, event);
        }
        this.queue(queue).onNext(event);
    }

    // for logging events in debug mode
    private <T> void registerObserver(Queue<T> queue, Observer<T> observer) {
        List<Reference<Observer<?>>> observerRefs = loggedObservers.get(queue.id);
        if (observerRefs == null) {
            observerRefs = new LinkedList<Reference<Observer<?>>>();
            loggedObservers.put(queue.id, observerRefs);
        }
        observerRefs.add(new WeakReference<Observer<?>>(observer));
    }

    private <T> void logEvent(Queue<T> queue, T event) {
        final StringBuilder message = new StringBuilder(5000);
        message.append("Publishing to ").append(queue.name)
                .append(" [").append(event.toString()).append("]\n");
        final List<Reference<Observer<?>>> observerRefs = loggedObservers.get(queue.id);
        if (observerRefs == null || observerRefs.isEmpty()) {
            message.append("No observers found.");
        } else {
            message.append("Delivering to: \n");
            for (Reference<Observer<?>> ref : observerRefs) {
                final Observer<?> observer = ref.get();
                if (observer != null) {
                    message.append("-> ").append(observer.getClass().getCanonicalName()).append('\n');
                }
            }
        }
        Log.d(TAG, message.toString());
    }
}
