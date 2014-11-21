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
public class ViewAction1Test {

    @Test
    @SuppressWarnings("unchecked")
    public void callIsNotExecutedWithAReleasedReference() {
        final View view = null; // simulate a released WeakReference
        final PublishSubject<Boolean> subject = PublishSubject.create();
        final ViewAction1Impl action = new ViewAction1Impl(view);
        subject.subscribe(action);

        assertFalse(action.wasCalled);
        subject.onNext(true);
        assertFalse(action.wasCalled);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void callIsExecutedWithARetainedReference() {
        final View view = createView();
        final PublishSubject<Boolean> subject = PublishSubject.create();
        final ViewAction1Impl action = new ViewAction1Impl(view);
        subject.subscribe(action);

        assertFalse(action.wasCalled);
        subject.onNext(true);
        assertTrue(action.wasCalled);
    }

    private static class ViewAction1Impl extends ViewAction1<View, Boolean> {

        boolean wasCalled = false;

        ViewAction1Impl(View view) {
            super(view);
        }

        @Override
        public void call(View view, Boolean aBoolean) {
            wasCalled = true;
        }

    }

}
