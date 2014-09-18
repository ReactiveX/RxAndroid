package rx.android.observables;

import android.content.Intent;

public interface ActivityStarter {

    void startActivityForResult(Intent intent, int requestCode);

}
