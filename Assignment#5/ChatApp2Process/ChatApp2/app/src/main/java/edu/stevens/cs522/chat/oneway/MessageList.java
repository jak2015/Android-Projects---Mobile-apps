package edu.stevens.cs522.chat.oneway;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class MessageList extends Activity {
	DbAdapter dbadapter;
	public SimpleCursorAdapter scadapter;

	String[] from = new String[] {DbAdapter.SENDER, DbAdapter.TEXT };
	int[] to = new int[] { R.id.cart_row_sender, R.id.cart_row_message };
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_message_list);
		Bundle b=getIntent().getExtras();
		String name=b.getString("name");
		String ip=b.getString("ip");
		String port=b.getString("port");
		TextView tview1=(TextView)findViewById(R.id.textView1);
		TextView tview2=(TextView)findViewById(R.id.textView2);
		TextView tview3=(TextView)findViewById(R.id.textView3);
		tview1.setText(name);
		tview2.setText(ip+":");
		tview3.setText(port);
		dbadapter = new DbAdapter(this);
		dbadapter.open();
		try{
			scadapter = new SimpleCursorAdapter(this, R.layout.message, dbadapter.getMessgeByPeer(name), from, to);
			ListView listView = (ListView) findViewById(R.id.listView1);
			
			listView.setAdapter(scadapter);
			}catch(Exception e){}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.message_list, menu);
		return true;
	}

}
