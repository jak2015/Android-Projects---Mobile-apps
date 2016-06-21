package edu.stevens.cs522.chat.oneway;


import edu.stevens.cs522.chat.contracts.PeerContract;
import edu.stevens.cs522.chat.entities.Peer;
import edu.stevens.cs522.chat.managers.IEntityCreator;
import edu.stevens.cs522.chat.managers.PeerManager;
import edu.stevens.cs522.chat.managers.IContinue;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class ListPeers extends ListActivity {
	
	public final static String EXTRA_PEER_ID = "edu.stevens.cs522.chat.oneway.EXTRA_PEER_ID";
	
	static final private int PEER_DETAIL = 1;
	
	private SimpleCursorAdapter simpleAdapter;	
	
	private long selected_item_pos = -1;
	
	private PeerManager peerManager;
	
	private static final int PEERS_LOADER_ID = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.peer_list);
		
		fillData(null);
		
		peerManager = new PeerManager(this, new IEntityCreator<Peer>() {

			public Peer create(Cursor cursor) {
				return new Peer(cursor);
			}
		}, PEERS_LOADER_ID);

		peerManager.getAllAsync(PeerContract.CONTENT_URI, 
								new String[]{PeerContract.PEER_ID, PeerContract.PEER_NAME}, 
								null, null, null, 
								new IContinue<Cursor>() {
			
			public void kontinue(Cursor value) {
				simpleAdapter.swapCursor(value);			
			}
		});
		
		
	}
	
	private void fillData(Cursor cr){
		String[] from = new String[]{PeerContract.PEER_NAME};
        int[] to = new int[]{R.id.peer_list_row_name};
        
		simpleAdapter = new SimpleCursorAdapter(this, R.layout.peer_list_row, cr, from, to, 0);
		setListAdapter(simpleAdapter);
		this.setSelection(0);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		MenuInflater inflater = getMenuInflater(); 
	    inflater.inflate(R.menu.chat_peer_list_menu, menu);
	   	    
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		switch (item.getItemId()) {
		case (R.id.peer_detail):
			if(this.selected_item_pos == -1) {
				Toast.makeText(this, "Please select a peer!", Toast.LENGTH_SHORT).show();
				return true;
			}
			Intent detailIntent = new Intent(this, PeerDetail.class);
			detailIntent.putExtra(ListPeers.EXTRA_PEER_ID, this.selected_item_pos);
			startActivityForResult(detailIntent, PEER_DETAIL);
			return true;
		case (R.id.cancel):
			setResult(RESULT_CANCELED, getIntent());
			finish();
			return true;
		}
		return false;
	}
	
	@Override
	protected void onListItemClick(ListView list, View view, int position, long id) {
	    super.onListItemClick(list, view, position, id);

	    this.selected_item_pos = id;
	    Toast.makeText(this, "Peer " + id + " selected!", Toast.LENGTH_SHORT).show();
	    
	}
}
