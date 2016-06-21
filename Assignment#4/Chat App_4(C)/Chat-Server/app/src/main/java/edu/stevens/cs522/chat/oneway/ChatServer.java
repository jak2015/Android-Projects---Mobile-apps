package edu.stevens.cs522.chat.oneway;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import edu.stevens.cs522.chat.adapters.PeerAdapter;
import edu.stevens.cs522.chat.contracts.MessageContract;
import edu.stevens.cs522.chat.contracts.PeerContract;
import edu.stevens.cs522.chat.entities.Message;
import edu.stevens.cs522.chat.entities.Peer;
import edu.stevens.cs522.chat.managers.PeerManager;
import edu.stevens.cs522.chat.managers.IContinue;
import edu.stevens.cs522.chat.managers.IEntityCreator;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;

public class ChatServer extends Activity implements OnClickListener {
	
	final static public String TAG = ChatServer.class.getCanonicalName();
	static final private int LIST_PEERS = 1;
	private static final int PEERS_LOADER_ID = 1;
	private DatagramSocket serverSocket;
	private boolean socketOK = true;
	Button next;
	private PeerAdapter peerAdapter;
	private ListView lstvw;
	
	private PeerManager peermanager;
	
	private String[] projection = new String[] { 
			PeerContract.PEER_NAME,
			MessageContract.MESSAGE_MESSAGE
		};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build(); 
		StrictMode.setThreadPolicy(policy);

		try {
			int port = Integer.parseInt(this.getString(R.string.app_port));
			serverSocket = new DatagramSocket(port);
		} catch (Exception e) {
			Log.e(TAG, "Cannot open socket" + e.getMessage());
			return;
		}

		peermanager = new PeerManager(this, new IEntityCreator<Peer>() {
					public Peer create(Cursor cursor) {
						return new Peer(cursor);
					}
				}, PEERS_LOADER_ID);			
		fillData(null);

		peermanager.getAllAsync(PeerContract.ALL_URI, projection, null, null, null,
		new IContinue<Cursor>() {
			public void kontinue(Cursor value) {
				peerAdapter.swapCursor(value);
			}
		});

		next = (Button)findViewById(R.id.next);
		next.setOnClickListener(this);

	}
	
	
	private void fillData(Cursor c) {

        this.peerAdapter = new PeerAdapter(this, R.layout.peer_list_row, c, 0);

		lstvw = (ListView)findViewById(android.R.id.list);
		lstvw.setAdapter(peerAdapter);
		lstvw.setEmptyView(findViewById(android.R.id.empty));
		lstvw.setSelection(0);
	}

	public void onClick(View v) {
		
		byte[] receiveData = new byte[1024];

		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

		try {
			
			serverSocket.receive(receivePacket);
			Log.i(TAG, "Received a packet");

			InetAddress sourceIPAddress = receivePacket.getAddress();
			Log.i(TAG, "Source IP Address: " + sourceIPAddress);

			String msgContents = new String(receivePacket.getData(), 0, receivePacket.getLength());
			String sender = msgContents.substring(0, msgContents.indexOf(":"));
			String message = msgContents.substring(msgContents.indexOf(":")+1);
			
			final Peer peer = new Peer(sender, sourceIPAddress, receivePacket.getPort());
			Message msg = new Message(0, message, sender);
			peermanager.persistAsync(peer, msg, new IContinue<Uri> () {
				public void	kontinue(Uri value) {
					peer.setId(PeerContract.getId(value));
				}
			});


			peermanager.getAllAsync(PeerContract.ALL_URI, projection, null, null, null,
					new IContinue<Cursor>() {
						public void kontinue(Cursor value) {
							peerAdapter.swapCursor(value);
						}
					});
			

		} catch (Exception e) {
			
			Log.e(TAG, "Problems receiving packet: " + e.getMessage());
			socketOK = false;
		} 

	}

	public void closeSocket() {
		serverSocket.close();
	}

	boolean socketIsOK() {
		return socketOK;
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		
		MenuInflater inflater = getMenuInflater(); 
	    inflater.inflate(R.menu.chat_server_menu, menu);
	   	    
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);

		switch (item.getItemId()) {
		case (R.id.show_peers):
			Intent addIntent = new Intent(this, ListPeers.class);
			startActivityForResult(addIntent, LIST_PEERS);
			return true;
		}
		return false;
	}
}