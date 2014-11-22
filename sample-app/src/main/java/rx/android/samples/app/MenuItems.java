package rx.android.samples.app;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import rx.android.samples.lifeycle.ListFragmentActivity;
import rx.android.samples.lifeycle.ListenInOutActivity;
import rx.android.samples.lifeycle.ListeningFragmentActivity;
import rx.android.samples.lifeycle.RetainedFragmentActivity;
import rx.android.samples.lifeycle.UIBindingActivity;
import rx.android.samples.widget.TextViewActivity;

import java.util.Arrays;
import java.util.List;

class MenuItems {

    static final String[] titles = new String[]{
            "List Fragment Activity",
            "Listening Fragment",
            "Listen In and Out",
            "Retained Fragment",
            "UI Binding Activity",
            "Text View Activity"
    };

    static final List<Class<? extends Activity>> samples = Arrays.asList(
            ListFragmentActivity.class,
            ListeningFragmentActivity.class,
            ListenInOutActivity.class,
            RetainedFragmentActivity.class,
            UIBindingActivity.class,
            TextViewActivity.class
    );

    public static Intent intentForSample(Context context, int position) {
        if (position > samples.size()) throw new IllegalArgumentException("No sample found at position " + position);
        return new Intent(context, samples.get(position));
    }
}

