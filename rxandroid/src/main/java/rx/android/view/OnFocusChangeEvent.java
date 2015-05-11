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
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class OnFocusChangeEvent {
    public abstract View view();
    public abstract boolean hasFocus();

    public static OnFocusChangeEvent create(final View view) {
        return create(view, view.hasFocus());
    }

    public static OnFocusChangeEvent create(final View view, boolean hasFocus) {
        return new AutoValue_OnFocusChangeEvent(view, hasFocus);
    }
}
