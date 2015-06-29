package rx.android.lifecycle.scenarios;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;

import rx.Subscription;

public abstract class  BaseLifecycleActivity extends Activity {
    protected Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        button = new Button(this);
        button.setText("Click Me!");
        setContentView(button);
    }

    public abstract Subscription getSubscription ();
}
