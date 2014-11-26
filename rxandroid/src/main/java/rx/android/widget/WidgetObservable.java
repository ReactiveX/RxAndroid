package rx.android.widget;

import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.TextView;

import rx.Observable;
import rx.android.view.OnCheckedChangeEvent;

public final class WidgetObservable {
    private WidgetObservable() {
        throw new AssertionError("No instances");
    }

    public static Observable<OnTextChangeEvent> text(final TextView input) {
        return text(input, false);
    }

    public static Observable<OnTextChangeEvent> text(final TextView input, final boolean emitInitialValue) {
        return Observable.create(new OnSubscribeTextViewInput(input, emitInitialValue));
    }

    public static Observable<OnCheckedChangeEvent> input(final CompoundButton button) {
        return input(button, false);
    }

    public static Observable<OnCheckedChangeEvent> input(final CompoundButton button, final boolean emitInitialValue) {
        return Observable.create(new OnSubscribeCompoundButtonInput(button, emitInitialValue));
    }

    public static Observable<OnItemClickEvent> itemClicks(final AdapterView<?> adapterView) {
        return Observable.create(new OnSubscribeAdapterViewOnItemClick(adapterView));
    }

    /**
     * Returns an observable that emits all the scroll events from the provided ListView.
     * Note that this will replace any listeners previously set through
     * {@link android.widget.AbsListView#setOnScrollListener(android.widget.AbsListView.OnScrollListener)} unless those
     * were set by this method or {@link OnSubscribeListViewScroll}.
     */
    public static Observable<OnListViewScrollEvent> listScrollEvents(final AbsListView listView) {
        return Observable.create(new OnSubscribeListViewScroll(listView));
    }
}
