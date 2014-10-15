package rx.android.events;

import android.text.SpannableString;
import android.widget.TextView;

public class OnTextChangeEvent {
    public final TextView view;
    public final CharSequence text;

    public OnTextChangeEvent(final TextView view) {
        this(view, new SpannableString(view.getText()));
    }

    public OnTextChangeEvent(final TextView view, final CharSequence text) {
        this.view = view;
        this.text = text;
    }
}
