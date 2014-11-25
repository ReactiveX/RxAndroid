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
package rx.android.observables;

import java.util.HashMap;
import java.util.Map;

import rx.Subscriber;

final class SubscriberVault {

    private static long idGenerator;

    private final Map<Long, Subscriber> subscriberMap = new HashMap<Long, Subscriber>();

    <T> long store(Subscriber<T> subscriber) {
        long id = idGenerator++;
        subscriberMap.put(id, subscriber);
        return id;
    }

    public <T> Subscriber<T> get(long key) {
        return subscriberMap.get(key);
    }

    public boolean containsKey(long key) {
        return subscriberMap.containsKey(key);
    }

    public <T> Subscriber<T> remove(long subscriberKey) {
        return subscriberMap.remove(subscriberKey);
    }
}
