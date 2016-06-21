package edu.stevens.cs522.chat.oneway;

import edu.stevens.cs522.chat.contracts.MessageContract;
import edu.stevens.cs522.chat.contracts.PeerContract;
import edu.stevens.cs522.chat.entities.Peer;
import edu.stevens.cs522.chat.managers.IContinue;
import edu.stevens.cs522.chat.managers.IEntityCreator;
import edu.stevens.cs522.chat.managers.PeerManager;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class PeerDetail extends ListActivity {
	
	private SimpleCursorAdapter simpleAdapter;	
	
	private PeerManager peerManager;
	
	private static final int MESSAGES_LOADER_ID = 2;
	
	private TextView ip_address;
	private TextView port_number;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.detail_list);
		
		fillData(null);
        
        Intent intent = getIntent();
        long id = intent.getLongExtra(ListPeers.EXTRA_PEER_ID,-1);
        
        peerManager = new PeerManager(this, new IEntityCreator<Peer>() {

			public Peer create(Cursor cursor) {
				return new Peer(cursor);
			}
		}, MESSAGES_LOADER_ID);

		peerManager.getAllAsync(PeerContract.MESSAGE_CONTENT_URI, 
								new String[]{PeerContract.PEER_ID,MessageContract.MESSAGE_MESSAGE}, 
								MessageContract.MESSAGE_PEER_FK + "=?", 
								new String[]{String.valueOf(id)}, 
								null, 
								new IContinue<Cursor>() {
			
					public void kontinue(Cursor value) {
						simpleAdapter.swapCursor(value);
					}
		});
		
		Uri peerUri = PeerContract.withExtendedPath(PeerContract.CONTENT_URI, String.valueOf(id));
		
		peerManager.getAllAsync(peerUri, 
								new String[]{PeerContract.PEER_ADDRESS, PeerContract.PEER_ADDRESS_PORT}, 
								null, 
								null, 
								null, 
								new IContinue<Cursor>() {

					public void kontinue(Cursor value) {

						value.moveToFirst();
						
						byte[] blob = value.getBlob(value.getColumnIndex(PeerContract.PEER_ADDRESS));
						String address = new String(blob);

						int port = value.getInt(value.getColumnIndexOrThrow(PeerContract.PEER_ADDRESS_PORT));
						ip_address.setText(address);
						port_number.setText(String.valueOf(port));			
					}
		});
		
	}

	private void fillData(Cursor cr){
		String[] from = new String[]{MessageContract.MESSAGE_MESSAGE};
        int[] to = new int[]{R.id.detail_list_row_msg};
        
        this.ip_address = (TextView)findViewById(R.id.ip_address);
        this.port_number = (TextView)findViewById(R.id.port_number);
        
		simpleAdapter = new SimpleCursorAdapter(this, R.layout.detail_list_row, cr, from, to, 0);
		setListAdapter(simpleAdapter);
		this.setSelection(0);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		MenuInflater inflater = getMenuInflater(); 
	    inflater.inflate(R.menu.chat_peer_detail_menu, menu);
	   	    
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		switch (item.getItemId()) {
		case (R.id.cancel):
			setResult(RESULT_CANCELED, getIntent());
			finish();
			return true;
		}
		return false;
	}

}
