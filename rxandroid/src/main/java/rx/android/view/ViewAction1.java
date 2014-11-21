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
package rx.android.view;

import android.view.View;

import java.lang.ref.WeakReference;

import rx.functions.Action1;

/**
 * An {@link Action1} implementation specific for {@link View}s.
 *
 * @param <V> the type of {@link View} upon which to perform the action.
 * @param <T> the type being observed
 */
public abstract class ViewAction1<V extends View, T> implements Action1<T> {

    private final WeakReference<V> viewReference;

    public ViewAction1(V view) {
        viewReference = new WeakReference<V>(view);
    }

    @Override
    public final void call(T t) {
        V view = viewReference.get();
        if (view != null) {
            call(view, t);
        }
    }

    /**
     * Implement this instead of {@link Action1#call(Object)}.
     * @param view the view given in the constructor.
     * @param t the object being observed
     */
    public abstract void call(V view, T t);

}
