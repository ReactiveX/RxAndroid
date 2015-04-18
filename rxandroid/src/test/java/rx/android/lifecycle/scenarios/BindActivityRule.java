package rx.android.lifecycle.scenarios;

import org.junit.rules.ExternalResource;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.robolectric.Robolectric;
import org.robolectric.util.ActivityController;

public class BindActivityRule extends ExternalResource {
    private Class<? extends BaseLifecycleActivity> activityClass;

    public ActivityController controller;
    public BaseLifecycleActivity activity;

    @Override
    protected void before() throws Throwable {
        if (activityClass != null) {
            controller = Robolectric.buildActivity(activityClass);
            activity = (BaseLifecycleActivity) controller.create().get();
        }
    }

    @Override
    protected void after() {
        controller = null;
        activity = null;
    }

    @Override
    public Statement apply(final Statement base, Description description) {
        UseActivity annotation = description.getAnnotation(UseActivity.class);
        if (annotation != null) {
            activityClass = annotation.value();
        }

        return super.apply(base, description);
    }
}
