package rx.android.view;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import android.app.Activity;
import android.view.View;
import android.widget.FrameLayout;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import rx.Observer;
import rx.Subscription;
import rx.android.AndroidObservable;
import rx.subjects.PublishSubject;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class BindViewTest {

    private Activity activity;
    private FrameLayout contentView;
    private View target;

    @Mock
    private Observer<String> observer;
    private PublishSubject<String> subject;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        subject = PublishSubject.create();
        activity = Robolectric.buildActivity(Activity.class).create().visible().get();
        contentView = new FrameLayout(activity);
        activity.setContentView(contentView);
        target = new View(activity);
    }

    @Test
    public void viewIsNotifiedEvenBeforeAttach() {
        AndroidObservable.bindView(target, subject).subscribe(observer);

        subject.onNext("hello");
        subject.onCompleted();

        verify(observer).onNext("hello");
        verify(observer).onCompleted();
    }

    @Test
    public void attachedViewIsNotified() {
        AndroidObservable.bindView(target, subject).subscribe(observer);
        contentView.addView(target);

        subject.onNext("hello");
        subject.onCompleted();

        verify(observer).onNext("hello");
        verify(observer).onCompleted();
    }

    @Test
    public void detachedViewIsNotNotified() {
        AndroidObservable.bindView(target, subject).subscribe(observer);
        contentView.addView(target);
        contentView.removeView(target);

        subject.onNext("hello");
        subject.onCompleted();

        // No onNext() here.
        verify(observer).onCompleted();
    }

    @Test
    public void recycledViewIsNotNotified() {
        AndroidObservable.bindView(target, subject).subscribe(observer);
        contentView.addView(target);
        contentView.removeView(target);
        contentView.addView(target);

        subject.onNext("hello");
        subject.onCompleted();

        // No onNext() here.
        verify(observer).onCompleted();
    }

    @Test
    public void unsubscribeStopsNotifications() {
        Subscription subscription = AndroidObservable.bindView(target, subject).subscribe(observer);
        contentView.addView(target);

        subscription.unsubscribe();

        subject.onNext("hello");
        subject.onCompleted();
        contentView.removeView(target);

        verifyNoMoreInteractions(observer);
    }

    @Test
    public void earlyUnsubscribeStopsNotifications() {
        Subscription subscription = AndroidObservable.bindView(target, subject).subscribe(observer);
        subscription.unsubscribe();

        contentView.addView(target);
        subject.onNext("hello");
        subject.onCompleted();
        contentView.removeView(target);

        verifyNoMoreInteractions(observer);
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullViewIsNotAllowed() {
        AndroidObservable.bindView(null, subject);
    }
}
