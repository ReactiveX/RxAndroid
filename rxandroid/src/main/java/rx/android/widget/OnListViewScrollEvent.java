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

public class OnListViewScrollEvent {
    public final AbsListView listView;
    public final int scrollState;
    public final int firstVisibleItem;
    public final int visibleItemCount;
    public final int totalItemCount;

    public OnListViewScrollEvent(
        AbsListView listView, int scrollState, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        this.listView = listView;
        this.scrollState = scrollState;
        this.firstVisibleItem = firstVisibleItem;
        this.visibleItemCount = visibleItemCount;
        this.totalItemCount = totalItemCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        OnListViewScrollEvent that = (OnListViewScrollEvent) o;

        if (firstVisibleItem != that.firstVisibleItem) {
            return false;
        }
        if (scrollState != that.scrollState) {
            return false;
        }
        if (totalItemCount != that.totalItemCount) {
            return false;
        }
        if (visibleItemCount != that.visibleItemCount) {
            return false;
        }
        if (!listView.equals(that.listView)) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = listView.hashCode();
        result = 31 * result + scrollState;
        result = 31 * result + firstVisibleItem;
        result = 31 * result + visibleItemCount;
        result = 31 * result + totalItemCount;
        return result;
    }

    @Override
    public String toString() {
        return "OnListViewScrollEvent{" +
            "listView=" + listView +
            ", scrollState=" + scrollState +
            ", firstVisibleItem=" + firstVisibleItem +
            ", visibleItemCount=" + visibleItemCount +
            ", totalItemCount=" + totalItemCount +
            '}';
    }
}
