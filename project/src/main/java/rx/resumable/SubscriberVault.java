/**
 * Copyright 2014 Novoda, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
