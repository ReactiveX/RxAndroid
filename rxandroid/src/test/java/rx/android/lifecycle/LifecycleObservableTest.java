/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package rx.android.lifecycle;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class LifecycleObservableTest {

    private BehaviorSubject<LifecycleEvent> lifecycle;
    private Observable<Object> observable;

    @Before
    public void setup() {
        lifecycle = BehaviorSubject.create();

        // Simulate an actual lifecycle (hot Observable that does not end)
        observable = PublishSubject.create().asObservable();
    }

    @Test
    public void testBindUntilLifecycleEvent() {
        Subscription untilStop =
                LifecycleObservable.bindUntilLifecycleEvent(lifecycle, observable, LifecycleEvent.STOP).subscribe();

        lifecycle.onNext(LifecycleEvent.CREATE);
        assertFalse(untilStop.isUnsubscribed());
        lifecycle.onNext(LifecycleEvent.START);
        assertFalse(untilStop.isUnsubscribed());
        lifecycle.onNext(LifecycleEvent.RESUME);
        assertFalse(untilStop.isUnsubscribed());
        lifecycle.onNext(LifecycleEvent.PAUSE);
        assertFalse(untilStop.isUnsubscribed());
        lifecycle.onNext(LifecycleEvent.STOP);
        assertTrue(untilStop.isUnsubscribed());
    }

    @Test
    public void testBindActivityLifecycle() {
        lifecycle.onNext(LifecycleEvent.CREATE);
        Subscription createSub = LifecycleObservable.bindActivityLifecycle(lifecycle, observable).subscribe();

        lifecycle.onNext(LifecycleEvent.START);
        assertFalse(createSub.isUnsubscribed());
        Subscription startSub = LifecycleObservable.bindActivityLifecycle(lifecycle, observable).subscribe();

        lifecycle.onNext(LifecycleEvent.RESUME);
        assertFalse(createSub.isUnsubscribed());
        assertFalse(startSub.isUnsubscribed());
        Subscription resumeSub = LifecycleObservable.bindActivityLifecycle(lifecycle, observable).subscribe();

        lifecycle.onNext(LifecycleEvent.PAUSE);
        assertFalse(createSub.isUnsubscribed());
        assertFalse(startSub.isUnsubscribed());
        assertTrue(resumeSub.isUnsubscribed());
        Subscription pauseSub = LifecycleObservable.bindActivityLifecycle(lifecycle, observable).subscribe();

        lifecycle.onNext(LifecycleEvent.STOP);
        assertFalse(createSub.isUnsubscribed());
        assertTrue(startSub.isUnsubscribed());
        assertTrue(pauseSub.isUnsubscribed());
        Subscription stopSub = LifecycleObservable.bindActivityLifecycle(lifecycle, observable).subscribe();

        lifecycle.onNext(LifecycleEvent.DESTROY);
        assertTrue(createSub.isUnsubscribed());
        assertTrue(stopSub.isUnsubscribed());
    }

    @Test(expected = RuntimeException.class)
    public void testThrowsExceptionOutsideActivityLifecycle() {
        lifecycle.onNext(LifecycleEvent.CREATE);
        lifecycle.onNext(LifecycleEvent.START);
        lifecycle.onNext(LifecycleEvent.RESUME);
        lifecycle.onNext(LifecycleEvent.PAUSE);
        lifecycle.onNext(LifecycleEvent.STOP);
        lifecycle.onNext(LifecycleEvent.DESTROY);

        LifecycleObservable.bindActivityLifecycle(lifecycle, observable)
                .subscribe(null, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throw new RuntimeException(throwable);
                    }
                });
    }

    @Test
    public void testBindFragmentLifecycle() {
        lifecycle.onNext(LifecycleEvent.ATTACH);
        Subscription attachSub = LifecycleObservable.bindFragmentLifecycle(lifecycle, observable).subscribe();

        lifecycle.onNext(LifecycleEvent.CREATE);
        assertFalse(attachSub.isUnsubscribed());
        Subscription createSub = LifecycleObservable.bindFragmentLifecycle(lifecycle, observable).subscribe();

        lifecycle.onNext(LifecycleEvent.CREATE_VIEW);
        assertFalse(attachSub.isUnsubscribed());
        assertFalse(createSub.isUnsubscribed());
        Subscription createViewSub = LifecycleObservable.bindFragmentLifecycle(lifecycle, observable).subscribe();

        lifecycle.onNext(LifecycleEvent.START);
        assertFalse(attachSub.isUnsubscribed());
        assertFalse(createSub.isUnsubscribed());
        assertFalse(createViewSub.isUnsubscribed());
        Subscription startSub = LifecycleObservable.bindFragmentLifecycle(lifecycle, observable).subscribe();

        lifecycle.onNext(LifecycleEvent.RESUME);
        assertFalse(attachSub.isUnsubscribed());
        assertFalse(createSub.isUnsubscribed());
        assertFalse(createViewSub.isUnsubscribed());
        assertFalse(startSub.isUnsubscribed());
        Subscription resumeSub = LifecycleObservable.bindFragmentLifecycle(lifecycle, observable).subscribe();

        lifecycle.onNext(LifecycleEvent.PAUSE);
        assertFalse(attachSub.isUnsubscribed());
        assertFalse(createSub.isUnsubscribed());
        assertFalse(createViewSub.isUnsubscribed());
        assertFalse(startSub.isUnsubscribed());
        assertTrue(resumeSub.isUnsubscribed());
        Subscription pauseSub = LifecycleObservable.bindFragmentLifecycle(lifecycle, observable).subscribe();

        lifecycle.onNext(LifecycleEvent.STOP);
        assertFalse(attachSub.isUnsubscribed());
        assertFalse(createSub.isUnsubscribed());
        assertFalse(createViewSub.isUnsubscribed());
        assertTrue(startSub.isUnsubscribed());
        assertTrue(pauseSub.isUnsubscribed());
        Subscription stopSub = LifecycleObservable.bindFragmentLifecycle(lifecycle, observable).subscribe();

        lifecycle.onNext(LifecycleEvent.DESTROY_VIEW);
        assertFalse(attachSub.isUnsubscribed());
        assertFalse(createSub.isUnsubscribed());
        assertTrue(createViewSub.isUnsubscribed());
        assertTrue(stopSub.isUnsubscribed());
        Subscription destroyViewSub = LifecycleObservable.bindFragmentLifecycle(lifecycle, observable).subscribe();

        lifecycle.onNext(LifecycleEvent.DESTROY);
        assertFalse(attachSub.isUnsubscribed());
        assertTrue(createSub.isUnsubscribed());
        assertTrue(destroyViewSub.isUnsubscribed());
        Subscription destroySub = LifecycleObservable.bindFragmentLifecycle(lifecycle, observable).subscribe();

        lifecycle.onNext(LifecycleEvent.DETACH);
        assertTrue(attachSub.isUnsubscribed());
        assertTrue(destroySub.isUnsubscribed());
    }

    @Test(expected = RuntimeException.class)
    public void testThrowsExceptionOutsideFragmentLifecycle() {
        lifecycle.onNext(LifecycleEvent.ATTACH);
        lifecycle.onNext(LifecycleEvent.CREATE);
        lifecycle.onNext(LifecycleEvent.CREATE_VIEW);
        lifecycle.onNext(LifecycleEvent.START);
        lifecycle.onNext(LifecycleEvent.RESUME);
        lifecycle.onNext(LifecycleEvent.PAUSE);
        lifecycle.onNext(LifecycleEvent.STOP);
        lifecycle.onNext(LifecycleEvent.DESTROY_VIEW);
        lifecycle.onNext(LifecycleEvent.DESTROY);
        lifecycle.onNext(LifecycleEvent.DETACH);

        LifecycleObservable.bindFragmentLifecycle(lifecycle, observable)
                .subscribe(null, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throw new RuntimeException(throwable);
                    }
                });
    }
}
