package edu.stevens.cs522.activities;


import edu.stevens.cs522.chatappcloud.R;
import edu.stevens.cs522.contracts.Contract;
import edu.stevens.cs522.databases.DbAdapter;
import edu.stevens.cs522.providers.CloudProvider;

import android.app.ListActivity;
import android.content.ContentResolver;
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



        ContentResolver cr = getContentResolver();
        Cursor c = cr.query(CloudProvider.CONTENT_URI_PEER_PASSWORD, null, null, null, null);
        startManagingCursor(c);


        //fillData();
		//Cursor c = chatDbAdapter.getAllPeer();
		//startManagingCursor(c);

		String[] from = new String[] { DbAdapter.SENDERNAME, DbAdapter.ADDRESS};
		int[] to = new int[] { android.R.id.text1, android.R.id.text2 };

		dbAdapter = new SimpleCursorAdapter(this, // Context.
				android.R.layout.simple_list_item_2, c, from, to);
		setListAdapter(dbAdapter);
		setSelection(0);

		myListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				Log.i(TAG, "item id = " + id);
				String name = "Default";

                ContentResolver cr = getContentResolver();

                Cursor c1 =  cr.query(CloudProvider.CONTENT_URI_PEER_PASSWORD, null, "peer._id = ?", new String[] { String.valueOf(id) },
                        null);

                if(c1.moveToFirst()){
                    name = c1.getString(c1.getColumnIndex(Contract.NAME));
                }
				//name = chatDbAdapter.getNameById(id);
				Log.i(TAG, "search peer: " + name);


				//Cursor c = chatDbAdapter.getMessgeByPeer(name);
                Cursor c = cr.query(CloudProvider.CONTENT_URI_PASSWORD, null, CloudProvider.SENDER+" = ?", new String[]{name}, null);


                startManagingCursor(c);
				String[] from = new String[]{"text", "sender"};
				int[] to = new int[]{android.R.id.text1, android.R.id.text2};
				dbAdapter = new SimpleCursorAdapter(getBaseContext(), android.R.layout.simple_list_item_2, c, from, to);
				setListAdapter(dbAdapter);
				setSelection(0);
			}
		});
	}


	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.peers, menu);
		return true;
	}

}
