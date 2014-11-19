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

import org.junit.Before;
import org.junit.Test;

import rx.Subscriber;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;

public class SubscriberVaultTest {

    private SubscriberVault subscriberVault;

    @Before
    public void setUp() {
        subscriberVault = new SubscriberVault();
    }

    @Test
    public void itStoresValuesForAGivenKey() {
        Subscriber mockSubscriber = mock(Subscriber.class);
        long id = subscriberVault.store(mockSubscriber);

        assertSame(subscriberVault.get(id), mockSubscriber);
    }

    @Test
    public void itRemovesValuesForAGivenKey() {
        Subscriber mockSubscriber = mock(Subscriber.class);
        long id = subscriberVault.store(mockSubscriber);
        subscriberVault.remove(id);

        assertNull(subscriberVault.get(id));
    }
}
