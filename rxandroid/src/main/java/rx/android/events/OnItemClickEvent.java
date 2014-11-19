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

import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;

public class OnItemClickEvent {
    public final AdapterView<?> parent;
    public final View view;
    public final int position;
    public final long id;

    public OnItemClickEvent(AdapterView<?> parent, View view, int position, long id) {
        this.parent = parent;
        this.view = view;
        this.position = position;
        this.id = id;
    }
}
