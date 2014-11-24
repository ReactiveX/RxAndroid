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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import rx.subjects.PublishSubject;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static rx.android.TestUtil.createView;

@RunWith(RobolectricTestRunner.class)
public class ViewActionSetSelectedTest {

    @Test
    @SuppressWarnings("unchecked")
    public void testSetsViewSelected() {
        final View view = createView();
        final PublishSubject<Boolean> subject = PublishSubject.create();
        subject.subscribe(ViewActions.setSelected(view));

        assertFalse(view.isSelected());
        subject.onNext(true);
        assertTrue(view.isSelected());
        subject.onNext(false);
        assertFalse(view.isSelected());
    }

}
