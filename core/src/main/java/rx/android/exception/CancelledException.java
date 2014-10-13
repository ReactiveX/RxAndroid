package rx.android.exception;

public class CancelledException extends NavigationException {
    @Override
    public String getMessage() {
        return "User cancelled the action";
    }
}
