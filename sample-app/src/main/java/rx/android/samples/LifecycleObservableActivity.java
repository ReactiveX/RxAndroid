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

package rx.android.samples;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;
import rx.Subscription;
import rx.android.app.RxActivity;
import rx.android.lifecycle.LifecycleObservable;
import rx.android.view.OnClickEvent;
import rx.android.view.ViewObservable;
import rx.functions.Action1;

/**
 * Simple example of creating a Subscription that is bound to the lifecycle
 * (and thus automatically unsubscribed when the Activity is destroyed).
 */
public class LifecycleObservableActivity extends RxActivity {

    private static final String TAG = LifecycleObservableActivity.class.getSimpleName();

    private Button button;

    private Subscription subscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        button = new Button(this);
        button.setText("Click Me!");
        setContentView(button);
    }

    @Override
    protected void onStart() {
        super.onStart();

        subscription =
                LifecycleObservable.bindActivityLifecycle(lifecycle(), ViewObservable.clicks(button))
                        .subscribe(new Action1<OnClickEvent>() {
                            @Override
                            public void call(OnClickEvent onClickEvent) {
                                Toast.makeText(LifecycleObservableActivity.this,
                                        "Clicked button!",
                                        Toast.LENGTH_SHORT)
                                        .show();
                            }
                        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Should output "false"
        Log.i(TAG, "onPause(), isUnsubscribed=" + subscription.isUnsubscribed());
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Should output "true"
        Log.i(TAG, "onStop(), isUnsubscribed=" + subscription.isUnsubscribed());
    }
}