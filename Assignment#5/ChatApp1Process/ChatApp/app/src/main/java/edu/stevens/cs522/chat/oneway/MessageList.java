package edu.stevens.cs522.chat.oneway;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class MessageList extends Activity {
    DbAdapter dbadapter;
    public SimpleCursorAdapter scuradapter;

    String[] from = new String[]{DbAdapter.SENDER, DbAdapter.TEXT};
    int[] to = new int[]{R.id.cart_row_sender, R.id.cart_row_message};

    @SuppressWarnings("deprecation")

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_list);
        Bundle b = getIntent().getExtras();
        String name = b.getString("name");
        String ip = b.getString("ip");
        String port = b.getString("port");
        TextView t1 = (TextView) findViewById(R.id.textView1);
        TextView t2 = (TextView) findViewById(R.id.textView2);
        TextView t3 = (TextView) findViewById(R.id.textView3);
        t1.setText(name);
        t2.setText(ip + ":");
        t3.setText(port);
        dbadapter = new DbAdapter(this);
        dbadapter.open();
        try {
            scuradapter = new SimpleCursorAdapter(this, R.layout.message, dbadapter.getMessgeByPeer(name), from, to);
            ListView listView = (ListView) findViewById(R.id.listView1);

            listView.setAdapter(scuradapter);
        } catch (Exception e) {
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.message_list, menu);
        return true;
    }
}
