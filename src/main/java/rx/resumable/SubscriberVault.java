package rx.resumable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import rx.Subscriber;

public class SubscriberVault {

    private final Map<UUID, Subscriber> subscriberMap = new HashMap<UUID, Subscriber>();

    public <T> UUID store(Subscriber<T> subscriber) {
        UUID uuid = UUID.randomUUID();
        subscriberMap.put(uuid, subscriber);
        return uuid;
    }

    public <T> Subscriber<T> get(UUID key) {
        return subscriberMap.get(key);
    }

    public boolean containsKey(UUID key) {
        return subscriberMap.containsKey(key);
    }

    public <T> Subscriber<T> remove(UUID subscriberKey) {
        return subscriberMap.remove(subscriberKey);
    }
}
