package rx.android.samples.widget;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import rx.Subscription;
import rx.android.samples.R;
import rx.android.widget.OnTextChangeEvent;
import rx.android.widget.WidgetObservable;
import rx.functions.Action1;
import rx.subscriptions.Subscriptions;

/**
 * An activity demonstrating how to observe changes on a {@link TextView} using the
 * view observable methods {@link WidgetObservable#text}
 */
public class TextViewActivity extends Activity {

    private TextView textView;
    private EditText inputTextView;
    private Subscription subscription = Subscriptions.empty();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.text_view_operator_activity);
        textView = (TextView) findViewById(R.id.text_view);
        inputTextView = (EditText) findViewById(R.id.input);

        subscription = WidgetObservable.text(inputTextView, true).subscribe(new Action1<OnTextChangeEvent>() {
            @Override
            public void call(OnTextChangeEvent onTextChangeEvent) {
                textView.setText(onTextChangeEvent.text());
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        subscription.unsubscribe();
    }
}
