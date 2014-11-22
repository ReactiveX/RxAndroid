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

import android.widget.CompoundButton;
import com.google.auto.value.AutoValue;

@AutoValue
public abstract class OnCheckedChangeEvent {
    public abstract CompoundButton view();
    public abstract boolean value();

    public static OnCheckedChangeEvent create(final CompoundButton view) {
        return create(view, view.isChecked());
    }

    public static OnCheckedChangeEvent create(final CompoundButton view, final boolean value) {
        return new AutoValue_OnCheckedChangeEvent(view, value);
    }
}
