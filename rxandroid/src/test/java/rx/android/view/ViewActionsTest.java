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
import android.widget.TextView;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import rx.subjects.PublishSubject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static rx.android.TestUtil.createView;

@RunWith(RobolectricTestRunner.class)
public class ViewActionsTest {

    @Test
    @SuppressWarnings("unchecked")
    public void activated() {
        final View view = createView();
        final PublishSubject<Boolean> subject = PublishSubject.create();
        subject.subscribe(ViewActions.setActivated(view));

        assertFalse(view.isActivated());
        subject.onNext(true);
        assertTrue(view.isActivated());
        subject.onNext(false);
        assertFalse(view.isActivated());
    }

    @Test
    public void activatedRejectsNull() {
        try {
            ViewActions.setActivated(null);
        } catch (NullPointerException e) {
            assertEquals("view", e.getMessage());
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void clickable() {
        final View view = createView();
        final PublishSubject<Boolean> subject = PublishSubject.create();
        subject.subscribe(ViewActions.setClickable(view));

        assertFalse(view.isClickable());
        subject.onNext(true);
        assertTrue(view.isClickable());
        subject.onNext(false);
        assertFalse(view.isClickable());
    }

    @Test
    public void clickableRejectsNull() {
        try {
            ViewActions.setClickable(null);
        } catch (NullPointerException e) {
            assertEquals("view", e.getMessage());
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void enabled() {
        final View view = createView();
        final PublishSubject<Boolean> subject = PublishSubject.create();
        subject.subscribe(ViewActions.setEnabled(view));

        assertTrue(view.isEnabled());
        subject.onNext(false);
        assertFalse(view.isEnabled());
        subject.onNext(true);
        assertTrue(view.isEnabled());
    }

    @Test
    public void enabledRejectsNull() {
        try {
            ViewActions.setEnabled(null);
        } catch (NullPointerException e) {
            assertEquals("view", e.getMessage());
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void focusable() {
        final View view = createView();
        final PublishSubject<Boolean> subject = PublishSubject.create();
        subject.subscribe(ViewActions.setFocusable(view));

        assertFalse(view.isFocusable());
        subject.onNext(true);
        assertTrue(view.isFocusable());
        subject.onNext(false);
        assertFalse(view.isFocusable());
    }

    @Test
    public void focusableRejectsNull() {
        try {
            ViewActions.setFocusable(null);
        } catch (NullPointerException e) {
            assertEquals("view", e.getMessage());
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void selected() {
        final View view = createView();
        final PublishSubject<Boolean> subject = PublishSubject.create();
        subject.subscribe(ViewActions.setSelected(view));

        assertFalse(view.isSelected());
        subject.onNext(true);
        assertTrue(view.isSelected());
        subject.onNext(false);
        assertFalse(view.isSelected());
    }

    @Test
    public void selectedRejectsNull() {
        try {
            ViewActions.setSelected(null);
        } catch (NullPointerException e) {
            assertEquals("view", e.getMessage());
        }
    }

    @Test
    public void text() {
        TextView textView = mock(TextView.class);
        final PublishSubject<String> subject = PublishSubject.create();
        subject.subscribe(ViewActions.setText(textView));

        subject.onNext("Hello");
        verify(textView).setText("Hello");

        subject.onNext("World");
        verify(textView).setText("World");
    }

    @Test
    public void textRejectsNull() {
        try {
            ViewActions.setText(null);
        } catch (NullPointerException e) {
            assertEquals("textView", e.getMessage());
        }
    }

    @Test
    public void textResource() {
        TextView textView = mock(TextView.class);
        final PublishSubject<Integer> subject = PublishSubject.create();
        subject.subscribe(ViewActions.setTextResource(textView));

        subject.onNext(1);
        verify(textView).setText(1);

        subject.onNext(3);
        verify(textView).setText(3);
    }

    @Test
    public void textResourceRejectsNull() {
        try {
            ViewActions.setTextResource(null);
        } catch (NullPointerException e) {
            assertEquals("textView", e.getMessage());
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void visibility() {
        final View view = createView();
        final PublishSubject<Boolean> subject = PublishSubject.create();
        subject.subscribe(ViewActions.setVisibility(view));

        assertEquals(View.VISIBLE, view.getVisibility());
        subject.onNext(false);
        assertEquals(View.GONE, view.getVisibility());
        subject.onNext(true);
        assertEquals(View.VISIBLE, view.getVisibility());
    }

    @Test
    public void visibilityRejectsNull() {
        try {
            ViewActions.setVisibility(null);
        } catch (NullPointerException e) {
            assertEquals("view", e.getMessage());
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void visibilityWithCustomFalseValue() {
        final View view = createView();
        final PublishSubject<Boolean> subject = PublishSubject.create();
        subject.subscribe(ViewActions.setVisibility(view, View.INVISIBLE));

        assertEquals(View.VISIBLE, view.getVisibility());
        subject.onNext(false);
        assertEquals(View.INVISIBLE, view.getVisibility());
        subject.onNext(true);
        assertEquals(View.VISIBLE, view.getVisibility());
    }

    @Test
    public void visibilityInvalidValues() {
        View view = createView();
        try {
            ViewActions.setVisibility(view, View.VISIBLE);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("Binding false to VISIBLE has no effect and is thus disallowed.", e.getMessage());
        }
        try {
            ViewActions.setVisibility(view, 42);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("42 is not a valid visibility value.", e.getMessage());
        }
    }

}
