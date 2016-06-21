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
	DbAdapter da;
	public SimpleCursorAdapter sca;
	String[] from = new String[] {"name"};
	int[] to = new int[] { R.id.cart_row_peer};
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_peer_list);
		da=new DbAdapter(this);
		da.open();
		sca= new SimpleCursorAdapter(this, R.layout.peerlist, da.getAllPeer(), from, to);
		//adapter=new BookAdapter(this,shoppingCart);
		//ListView listView = (ListView) findViewById(R.id.listView1);
		ListView listView = getListView();
		listView.setAdapter(sca);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.peer_list, menu);
		return true;
	}
	 protected void onListItemClick(ListView l, View v, int position, long id) {
         // TODO Auto-generated method stub
         super.onListItemClick(l, v, position, id);
         Context context = getApplicationContext();
			int duration = Toast.LENGTH_SHORT;
			//int a=position+1;
			TextView t=(TextView) v.findViewById(R.id.cart_row_peer);
			String name=t.getText().toString();
			Cursor c=sca.getCursor();
			c.moveToPosition(position);
			String ip=Contract.getAddress(c);
			String port=Contract.getPort(c);
			Intent intent=new Intent(this, MessageList.class);
			intent.putExtra("name",name);
			intent.putExtra("ip",ip);
			intent.putExtra("port",port);
			startActivityForResult(intent, 1);
	Toast toast = Toast.makeText(context,"Peer "+name+" is selected.", duration);
	toast.show();
	 }

}
