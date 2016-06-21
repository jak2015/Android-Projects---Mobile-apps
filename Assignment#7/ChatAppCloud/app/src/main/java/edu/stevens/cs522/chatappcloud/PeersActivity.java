package edu.stevens.cs522.chatappcloud;

import edu.stevens.cs522.database.DbAdapter;
import android.app.ListActivity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class PeersActivity extends ListActivity {

	private SimpleCursorAdapter dbAdapter;
	private  DbAdapter chatDbAdapter;
	private int position;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_peers);
		ListView myListView = (ListView) findViewById(android.R.id.list);
		registerForContextMenu(myListView);
		chatDbAdapter = new DbAdapter(this);
		chatDbAdapter.open();
		fillData();

	}
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderTitle("Peer Detail");
		menu.add(0, v.getId(), 0, "Show Messages");
		menu.add(0, v.getId(), 0, "Cancel");
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		if(item.getTitle() =="Show Messages"){
			try{
				
				fillDataMsg(info.id);
			}catch(Exception e){
				
			}
			return true;
		}else{
			 return super.onContextItemSelected(item);
		}
		
	}
	private void fillDataMsg(long id) {
		// TODO Auto-generated method stub
		String name;
		name = chatDbAdapter.getNameById(id);
		Log.d("search peer", name);
		Cursor c = chatDbAdapter.getMessgeByPeer(name);
		startManagingCursor(c);
		String[] from = new String[] {"text", "sender" };
		int[] to = new int[] { android.R.id.text1, android.R.id.text2 };

		// Now create a list adaptor that encapsulates the result of a DB query
		dbAdapter = new SimpleCursorAdapter(this,android.R.layout.simple_list_item_2, c, from, to);
		setListAdapter(dbAdapter);
		setSelection(0);
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
