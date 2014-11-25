package rx.android.samples;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import rx.Observable;
import rx.Subscriber;
import rx.android.content.ContentObservable;
import rx.android.widget.OnListViewScrollEvent;
import rx.android.widget.WidgetObservable;
import rx.functions.Action1;

import static rx.android.schedulers.AndroidSchedulers.mainThread;

/**
 * Problem:
 * You have an asynchronous sequence that emits items to be displayed in a list. You want the data
 * to survive rotation changes.
 * <p/>
 * Solution:
 * Combine {@link android.app.Fragment#setRetainInstance(boolean)} in a ListFragment with
 * {@link rx.android.schedulers.AndroidSchedulers#mainThread()} and an {@link rx.Observable.Operator}
 * that binds to the list adapter.
 */
public class ListFragmentActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("Lists");
        setContentView(R.layout.list_fragment_activity);
    }

    @SuppressWarnings("ConstantConditions")
    public static class RetainedListFragment extends Fragment {

        private ArrayAdapter<String> adapter;

        public RetainedListFragment() {
            setRetainInstance(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.list_fragment, container, false);

            adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1);
            ListView listView = (ListView) view.findViewById(android.R.id.list);
            listView.setAdapter(adapter);

            ContentObservable.bindFragment(this, SampleObservables.numberStrings(1, 500, 100))
                .observeOn(mainThread())
                .lift(new BindAdapter())
                .subscribe();

            final ProgressBar progressBar = (ProgressBar) view.findViewById(android.R.id.progress);
            ContentObservable.bindFragment(this, WidgetObservable.listScrollEvents(listView))
                .subscribe(new Action1<OnListViewScrollEvent>() {
                    @Override
                    public void call(OnListViewScrollEvent event) {
                        if (event.totalItemCount() == 0) {
                            return;
                        }

                        int progress =
                            (int) ((100.0 * (event.firstVisibleItem() + event.visibleItemCount())) / event.totalItemCount());
                        progressBar.setProgress(progress);
                    }
                });

            return view;
        }

        private final class BindAdapter implements Observable.Operator<String, String> {

            @Override
            public Subscriber<? super String> call(Subscriber<? super String> subscriber) {
                return new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onError(Throwable throwable) {

                    }

                    @Override
                    public void onNext(String strings) {
                        adapter.add(strings);
                    }
                };
            }
        }
    }
}
