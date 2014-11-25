package rx.android.view;

import android.widget.TextView;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import rx.subjects.PublishSubject;

import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
public class ViewActionSetTextTest {

    private TextView textView;

    @Before
    public void setUp() {
        textView = Mockito.mock(TextView.class);
    }

    @Test
    public void testSetsTextViewCharSequence() {
        final PublishSubject<String> subject = PublishSubject.create();
        subject.subscribe(ViewActions.setText(textView));

        subject.onNext("Hello");
        verify(textView).setText("Hello");

        subject.onNext("World");
        verify(textView).setText("World");
    }

    @Test
    public void testSetsTextViewTextResource() {
        final PublishSubject<Integer> subject = PublishSubject.create();
        subject.subscribe(ViewActions.setTextResource(textView));

        subject.onNext(1);
        verify(textView).setText(1);

        subject.onNext(3);
        verify(textView).setText(3);
    }
}
