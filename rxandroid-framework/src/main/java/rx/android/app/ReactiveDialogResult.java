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
package rx.android.app;

/**
 * A wrapper for returning value from ReactiveDialog dialog.
 *
 * @param <V> The type of data expected as return value from the dialog.
 */
public final class ReactiveDialogResult<V> {

    public final V value;
    public final boolean canceled;

    static <V> ReactiveDialogResult<V> asSuccess(V value) {
        return new ReactiveDialogResult<V>(value, false);
    }

    static <V> ReactiveDialogResult<V> asCanceled() {
        return new ReactiveDialogResult<V>(null, true);
    }

    private ReactiveDialogResult(V value, boolean canceled) {
        this.value = value;
        this.canceled = canceled;
    }

    public V getValue() {
        return value;
    }

    public boolean isCanceled() {
        return canceled;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReactiveDialogResult that = (ReactiveDialogResult) o;

        if (canceled != that.canceled) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = value.hashCode();
        result = 31 * result + (canceled ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ReactiveDialogResult{" +
                "value=" + value +
                ", canceled=" + canceled +
                '}';
    }
}
