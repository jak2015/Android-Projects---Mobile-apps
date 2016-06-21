package edu.stevens.cs522.chat.oneway;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class PeerList extends ListActivity{
	DbAdapter dbadapter;
	public SimpleCursorAdapter scadapter;
	String[] from = new String[] {"name"};
	int[] to = new int[] { R.id.cart_row_peer};
	@SuppressWarnings("deprecation")

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_peer_list);
		dbadapter = new DbAdapter(this);
		dbadapter.open();
		scadapter = new SimpleCursorAdapter(this, R.layout.peerlist, dbadapter.getAllPeer(), from, to);
		ListView listView = getListView();
		listView.setAdapter(scadapter);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.peer_list, menu);
		return true;
	}
	 protected void onListItemClick(ListView l, View v, int position, long id) {
         super.onListItemClick(l, v, position, id);
         Context context = getApplicationContext();
			int duration = Toast.LENGTH_SHORT;
			TextView tview = (TextView) v.findViewById(R.id.cart_row_peer);
			String name = tview.getText().toString();
			Cursor cursor = scadapter.getCursor();
		 	cursor.moveToPosition(position);
			String ip = Contract.getAddress(cursor);
			String port = Contract.getPort(cursor);
			Intent intent = new Intent(this, MessageList.class);
			intent.putExtra("name",name);
			intent.putExtra("ip",ip);
			intent.putExtra("port",port);
			startActivityForResult(intent, 1);
			Toast toast = Toast.makeText(context,"Peer "+name+" is selected.", duration);
			toast.show();
	 }

}
