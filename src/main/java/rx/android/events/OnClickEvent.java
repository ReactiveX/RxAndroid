package rx.android.events;

import android.view.View;

public class OnClickEvent {
    public final View view;

    public OnClickEvent(final View view) {
        this.view = view;
    }
}
