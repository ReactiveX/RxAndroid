package rx.android.samples.eventbus;

import rx.android.eventbus.EventQueue;

public final class Events {

    public static final EventQueue<String> STRING_QUEUE = EventQueue.build(String.class).replay("Default event").get();

}
