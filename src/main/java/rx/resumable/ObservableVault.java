package rx.resumable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import rx.Observable;

public class ObservableVault {

    private final Map<Integer, Map<Integer, Observable>> arraysOfObservables = new HashMap<Integer, Map<Integer, Observable>>();

    public synchronized Observable put(ResumableReference reference, int id, Observable observable) {
        return getObservablesFor(reference).put(id, observable);
    }

    public synchronized Observable remove(ResumableReference reference, int id) {
        return getObservablesFor(reference).remove(id);
    }

    public synchronized Map<Integer, Observable> getImmutableObservablesFor(ResumableReference reference) {
        return Collections.unmodifiableMap(new HashMap<Integer, Observable>(getObservablesFor(reference)));
    }

    private synchronized Map<Integer, Observable> getObservablesFor(ResumableReference reference) {
        int referenceId = reference.getResumableId();
        if (arraysOfObservables.containsKey(referenceId)) {
            return arraysOfObservables.get(referenceId);
        }
        Map<Integer, Observable> observableMap = new HashMap<Integer, Observable>();
        arraysOfObservables.put(referenceId, observableMap);
        return observableMap;
    }
}
