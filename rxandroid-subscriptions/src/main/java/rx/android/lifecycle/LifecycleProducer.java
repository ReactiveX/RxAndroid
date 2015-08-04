/*
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

import rx.Observable;
import rx.subjects.BehaviorSubject;

public final class LifecycleProducer {
    private final BehaviorSubject<LifecycleEvent> subject = BehaviorSubject.create();

    private LifecycleProducer() {}

    public static LifecycleProducer create() {
        return new LifecycleProducer();
    }

    public void onAttach() {
        subject.onNext(LifecycleEvent.ATTACH);
    }

    public void onCreate() {
        subject.onNext(LifecycleEvent.CREATE);
    }

    public void onViewCreated() {
        subject.onNext(LifecycleEvent.CREATE_VIEW);
    }

    public void onStart() {
        subject.onNext(LifecycleEvent.START);
    }

    public void onResume() {
        subject.onNext(LifecycleEvent.RESUME);
    }

    public void onPause() {
        subject.onNext(LifecycleEvent.PAUSE);
    }

    public void onStop() {
        subject.onNext(LifecycleEvent.STOP);
    }

    public void onDestroyView() {
        subject.onNext(LifecycleEvent.DESTROY_VIEW);
    }

    public void onDestroy() {
        subject.onNext(LifecycleEvent.DESTROY);
    }

    public void onDetach() {
        subject.onNext(LifecycleEvent.DETACH);
    }

    public Observable<LifecycleEvent> asObservable() {
        return subject.asObservable();
    }
}
