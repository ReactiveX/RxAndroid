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
package rx.android.events;

import android.widget.CompoundButton;

public class OnCheckedChangeEvent {
    public final CompoundButton view;
    public final boolean value;

    public OnCheckedChangeEvent(final CompoundButton view) {
        this(view, view.isChecked());
    }

    public OnCheckedChangeEvent(final CompoundButton view, final boolean value) {
        this.view = view;
        this.value = value;
    }
}
