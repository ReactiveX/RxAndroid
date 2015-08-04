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

public interface LifecycleObservationCalculator {
    LifecycleObservationCalculator ACTIVITY = new LifecycleObservationCalculator() {
        @Override public LifecycleEvent observeUntil(LifecycleEvent current) {
            if (current == null) {
                return LifecycleEvent.DESTROY;
            }
            switch (current) {
                case CREATE:
                    return LifecycleEvent.DESTROY;
                case START:
                    return LifecycleEvent.STOP;
                case RESUME:
                    return LifecycleEvent.PAUSE;
                case PAUSE:
                    return LifecycleEvent.STOP;
                case STOP:
                case DESTROY:
                default:
                    return LifecycleEvent.DESTROY;
            }
        }
    };

    LifecycleObservationCalculator FRAGMENT = new LifecycleObservationCalculator() {
        @Override public LifecycleEvent observeUntil(LifecycleEvent current) {
            if (current == null) {
                return LifecycleEvent.DETACH;
            }
            switch (current) {
                case ATTACH:
                    return LifecycleEvent.DETACH;
                case CREATE:
                    return LifecycleEvent.DESTROY;
                case CREATE_VIEW:
                    return LifecycleEvent.DESTROY_VIEW;
                case START:
                    return LifecycleEvent.STOP;
                case RESUME:
                    return LifecycleEvent.PAUSE;
                case PAUSE:
                    return LifecycleEvent.STOP;
                case STOP:
                    return LifecycleEvent.DESTROY_VIEW;
                case DESTROY_VIEW:
                    return LifecycleEvent.DESTROY;
                case DESTROY:
                case DETACH:
                default:
                    return LifecycleEvent.DETACH;
            }
        }
    };

    LifecycleObservationCalculator UNTIL_STOP = new LifecycleObservationCalculator() {
        @Override public LifecycleEvent observeUntil(LifecycleEvent current) {
            return LifecycleEvent.STOP;
        }
    };

    LifecycleEvent observeUntil(LifecycleEvent current);
}
