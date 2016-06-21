package edu.stevens.cs522.activities;


import edu.stevens.cs522.chatappcloud.R;
import edu.stevens.cs522.databases.DbAdapter;

import android.app.ListActivity;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.OnItemClickListener;


public class PeersActivity extends ListActivity {

	public static final String TAG = PeersActivity.class.getCanonicalName();
	private SimpleCursorAdapter dbAdapter;
	private  DbAdapter chatDbAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_peers);
		ListView myListView = (ListView) findViewById(android.R.id.list);
		registerForContextMenu(myListView);
		chatDbAdapter = new DbAdapter(this);
		chatDbAdapter.open();

		//fillData();
		Cursor c = chatDbAdapter.getAllPeer();
		startManagingCursor(c);

		String[] from = new String[] { "_id", "name"};
		int[] to = new int[] { android.R.id.text1, android.R.id.text2 };

		dbAdapter = new SimpleCursorAdapter(this, // Context.
				android.R.layout.simple_list_item_2, c, from, to);
		setListAdapter(dbAdapter);
		setSelection(0);

		myListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Log.i(TAG,"item id = "+id);
				String name;
				name = chatDbAdapter.getNameById(id);
				Log.i(TAG, "search peer: "+name);
				Cursor c = chatDbAdapter.getMessgeByPeer(name);
				startManagingCursor(c);
				String[] from = new String[] {"text", "sender" };
				int[] to = new int[] { android.R.id.text1, android.R.id.text2 };
				dbAdapter = new SimpleCursorAdapter(getBaseContext(), android.R.layout.simple_list_item_2, c, from, to);
				setListAdapter(dbAdapter);
				setSelection(0);
			}
		});
	}

	@SuppressWarnings("deprecation")
	private void fillData() {
		// TODO Auto-generated method stub
		Cursor c = chatDbAdapter.getAllPeer();
		startManagingCursor(c);

		String[] from = new String[] { "_id", "name"};
		int[] to = new int[] { android.R.id.text1, android.R.id.text2 };

		dbAdapter = new SimpleCursorAdapter(this, // Context.
				android.R.layout.simple_list_item_2, c, from, to); 				
		setListAdapter(dbAdapter);
		setSelection(0);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.peers, menu);
		return true;
	}

}
