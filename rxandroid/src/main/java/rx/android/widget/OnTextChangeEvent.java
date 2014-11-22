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
package rx.android.widget;

import android.text.SpannableString;
import android.widget.TextView;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class OnTextChangeEvent {
    public abstract TextView view();
    public abstract CharSequence text();

    public static OnTextChangeEvent create(final TextView view) {
        return create(view, new SpannableString(view.getText()));
    }

    public static OnTextChangeEvent create(final TextView view, final CharSequence text) {
        return new AutoValue_OnTextChangeEvent(view, text);
    }
}
