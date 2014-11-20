package rx.android.samples.eventbus;

import rx.android.eventbus.Queue;

public final class EventQueue {

    public static final Queue<String> STRING_QUEUE = Queue.build(String.class).replay("Default event").get();

}
