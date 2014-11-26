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

import android.widget.AbsListView;

import com.google.auto.value.AutoValue;

@AutoValue
public abstract class OnListViewScrollEvent {
    public abstract AbsListView listView();
    public abstract int scrollState();
    public abstract int firstVisibleItem();
    public abstract int visibleItemCount();
    public abstract int totalItemCount();

    public static OnListViewScrollEvent create(
        AbsListView listView, int scrollState, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        return new AutoValue_OnListViewScrollEvent(listView, scrollState, firstVisibleItem, visibleItemCount, totalItemCount);
    }

}
