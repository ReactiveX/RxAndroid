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
import rx.functions.Action1;

import static rx.android.internal.Preconditions.checkNotNull;
import static rx.android.internal.Preconditions.checkArgument;

/**
 * Utility class for the {@code Action} interfaces for use with {@linkplain View views}.
 */
public final class ViewActions {

    private ViewActions() {
        throw new IllegalStateException("No instances!");
    }

    /**
     * Create an {@linkplain Action1 action} which controls the supplied view's
     * {@linkplain View#setEnabled enabled property}.
     * <p>
     * Note: the created action will not keep a strong reference to the view.
     */
    public static Action1<? super Boolean> setEnabled(View view) {
        checkNotNull(view, "view");
        return new ViewAction1<View, Boolean>(view) {
            @Override
            public void call(View view, Boolean enabled) {
                view.setEnabled(enabled);
            }
        };
    }

    /**
     * Create an {@linkplain Action1 action} which controls the supplied view's
     * {@linkplain View#setActivated activated property}.
     * <p>
     * Note: the created action will not keep a strong reference to the view.
     */
    public static Action1<? super Boolean> setActivated(View view) {
        checkNotNull(view, "view");
        return new ViewAction1<View, Boolean>(view) {
            @Override
            public void call(View view, Boolean activated) {
                view.setActivated(activated);
            }
        };
    }

    /**
     * Create an {@linkplain Action1 action} which controls the supplied view's
     * {@linkplain View#setClickable clickable property}.
     * <p>
     * Note: the created action will not keep a strong reference to the view.
     */
    public static Action1<? super Boolean> setClickable(View view) {
        checkNotNull(view, "view");
        return new ViewAction1<View, Boolean>(view) {
            @Override
            public void call(View view, Boolean clickable) {
                view.setClickable(clickable);
            }
        };
    }

    /**
     * Create an {@linkplain Action1 action} which controls the supplied view's
     * {@linkplain View#setFocusable focusable property}.
     * <p>
     * Note: the created action will not keep a strong reference to the view.
     */
    public static Action1<? super Boolean> setFocusable(View view) {
        checkNotNull(view, "view");
        return new ViewAction1<View, Boolean>(view) {
            @Override
            public void call(View view, Boolean focusable) {
                view.setFocusable(focusable);
            }
        };
    }

    /**
     * Create an {@linkplain Action1 action} which controls the supplied view's
     * {@linkplain View#setSelected selected property}.
     * <p>
     * Note: the created action will not keep a strong reference to the view.
     */
    public static Action1<? super Boolean> setSelected(View view) {
        checkNotNull(view, "view");
        return new ViewAction1<View, Boolean>(view) {
            @Override
            public void call(View view, Boolean selected) {
                view.setSelected(selected);
            }
        };
    }

    /**
     * Create an {@linkplain Action1 action} which controls the supplied view's
     * {@linkplain View#setVisibility visibility property}. {@code true} values are mapped to
     * {@link View#VISIBLE} and {@code false} values to {@link View#GONE}.
     * <p>
     * Note: the created action will not keep a strong reference to the view.
     *
     * @see #setVisibility(View, int)
     */
    public static Action1<? super Boolean> setVisibility(View view) {
        return setVisibility(view, View.GONE);
    }

    /**
     * Create an {@linkplain Action1 action} which controls the supplied view's
     * {@linkplain View#setVisibility visibility property}. {@code true} values are mapped to
     * {@link View#VISIBLE} and {@code false} values to the supplied visibility.
     * <p>
     * Note: the created action will not keep a strong reference to the view.
     *
     * @param visibilityOnFalse {@link View#INVISIBLE} or {@link View#GONE}.
     *
     * @see #setVisibility(View, int)
     */
    public static Action1<? super Boolean> setVisibility(View view, final int visibilityOnFalse) {
        checkNotNull(view, "view");
        // TODO use support annotation's validation when we become a real Android library.
        checkArgument(visibilityOnFalse != View.VISIBLE,
                    "Binding false to VISIBLE has no effect and is thus disallowed.");
        if (visibilityOnFalse != View.INVISIBLE && visibilityOnFalse != View.GONE) {
            throw new IllegalArgumentException(visibilityOnFalse + " is not a valid visibility value.");
        }
        return new ViewAction1<View, Boolean>(view) {
            @Override
            public void call(View view, Boolean value) {
                int visibility = value ? View.VISIBLE : visibilityOnFalse;
                view.setVisibility(visibility);
            }
        };
    }

    /**
     * Create an {@linkplain Action1 action} which controls the supplied view's
     * {@linkplain TextView#setText(CharSequence) text property}.
     * <p>
     * Note: the created action will not keep a strong reference to the view.
     *
     * @see #setTextResource
     */
    public static Action1<? super CharSequence> setText(TextView textView) {
        checkNotNull(textView, "textView");
        return new ViewAction1<TextView, CharSequence>(textView) {
            @Override
            public void call(TextView view, CharSequence text) {
                view.setText(text);
            }
        };
    }

    /**
     * Create an {@linkplain Action1 action} which controls the supplied view's
     * {@linkplain TextView#setText(int) text property}.
     * <p>
     * Note: the created action will not keep a strong reference to the view.
     *
     * @see #setText
     */
    public static Action1<? super Integer> setTextResource(TextView textView) {
        checkNotNull(textView, "textView");
        return new ViewAction1<TextView, Integer>(textView) {
            @Override
            public void call(TextView view, Integer resId) {
                view.setText(resId);
            }
        };
    }
}
