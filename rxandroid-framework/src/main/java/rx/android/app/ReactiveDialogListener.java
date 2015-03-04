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

import rx.Observer;

/**
 * Interface used for receiving the dialog result. Two cases : the dialog is completed with some value, or it is canceled.
 *
 * @param <V> The type of data expected as return value from the dialog.
 */
public interface ReactiveDialogListener<V> extends Observer<V> {
    void onCompleteWith(V value);

    void onCancel();
}
