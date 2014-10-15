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
