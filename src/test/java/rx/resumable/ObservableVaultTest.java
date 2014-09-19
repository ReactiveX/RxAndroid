package rx.resumable;

import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import rx.Observable;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class ObservableVaultTest {

    private ResumableReference ref1 = new ResumableReference() {
        @Override
        public int getResumableId() {
            return 1;
        }
    };

    private ResumableReference ref2 = new ResumableReference() {
        @Override
        public int getResumableId() {
            return 2;
        }
    };

    private ObservableVault observableVault;

    @Before
    public void setUp() {
        observableVault = new ObservableVault();
    }

    @Test
    public void itStoresValuesForAGivenKey() {
        Observable mockObservable = mock(Observable.class);
        Observable mockObservable2 = mock(Observable.class);
        observableVault.put(ref1, 42, mockObservable);
        observableVault.put(ref2, 42, mockObservable2);
        Map<Integer, Observable> observableMap = observableVault.getImmutableObservablesFor(ref1);

        assertTrue(observableMap.containsKey(42));
        assertSame(observableMap.get(42), mockObservable);
    }

    @Test
    public void itRemovesValuesForAGivenKey() {
        Observable mockObservable = mock(Observable.class);
        Observable mockObservable2 = mock(Observable.class);

        observableVault.put(ref1, 42, mockObservable);
        observableVault.put(ref2, 42, mockObservable2);
        observableVault.remove(ref1, 42);
        Map<Integer, Observable> observableMap = observableVault.getImmutableObservablesFor(ref1);
        Map<Integer, Observable> observableMap2 = observableVault.getImmutableObservablesFor(ref2);

        assertFalse(observableMap.containsKey(42));
        assertTrue(observableMap2.containsKey(42));
        assertSame(observableMap2.get(42), mockObservable2);
    }
}