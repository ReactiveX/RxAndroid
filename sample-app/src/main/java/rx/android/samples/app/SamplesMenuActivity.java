package rx.android.samples.app;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import rx.android.samples.R;

public class SamplesMenuActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.samples_menu_activity);

        ArrayAdapter<String> menuAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        menuAdapter.addAll(MenuItems.titles);

        ListView listView = (ListView) findViewById(android.R.id.list);
        listView.setAdapter(menuAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(MenuItems.intentForSample(SamplesMenuActivity.this, position));
            }
        });
    }
}
