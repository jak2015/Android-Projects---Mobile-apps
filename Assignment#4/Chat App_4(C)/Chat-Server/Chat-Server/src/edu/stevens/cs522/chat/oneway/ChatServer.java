/*********************************************************************

    Chat server: accept chat messages from clients.
    
    Sender name and GPS coordinates are encoded
    in the messages, and stripped off upon receipt.

    Copyright (c) 2012 Stevens Institute of Technology

**********************************************************************/
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
		
	/*
	 * Socket used both for sending and receiving
	 */
	private DatagramSocket serverSocket; 

	/*
	 * True as long as we don't get socket errors
	 */
	private boolean socketOK = true; 

	Button next;
	
	private PeerAdapter peerAdapter;
	
	private ListView lv;
	
	private PeerManager pm;
	
	private String[] projection = new String[] { 
			PeerContract.PEER_NAME,
			MessageContract.MESSAGE_MESSAGE
		};

	/*
	 * Called when the activity is first created. 
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		/**
		 * Let's be clear, this is a HACK to allow you to do network communication on the main thread.
		 * This WILL cause an ANR, and is only provided to simplify the pedagogy.  We will see how to do
		 * this right in a future assignment (using a Service managing background threads).
		 */
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build(); 
		StrictMode.setThreadPolicy(policy);

		try {
			/*
			 * Get port information from the resources.
			 */
			int port = Integer.parseInt(this.getString(R.string.app_port));
			serverSocket = new DatagramSocket(port);
		} catch (Exception e) {
			Log.e(TAG, "Cannot open socket" + e.getMessage());
			return;
		}

		/*
		 * TODO: Initialize the UI.
		 */
	
		pm = new PeerManager(this, new IEntityCreator<Peer>() {
					public Peer create(Cursor cursor) {
						return new Peer(cursor);
					}
				}, PEERS_LOADER_ID);			
		fillData(null);
		
		pm.getAllAsync(PeerContract.ALL_URI, projection, null, null, null,
		new IContinue<Cursor>() {
			public void kontinue(Cursor value) {
				peerAdapter.swapCursor(value);
			}
		});

		next = (Button)findViewById(R.id.next);
		next.setOnClickListener(this);

	}
	
	
	private void fillData(Cursor c) {
		
		// Which layout object to bind to which data object
		
        this.peerAdapter = new PeerAdapter(this, R.layout.peer_list_row, c, 0);
        
        lv = (ListView)findViewById(android.R.id.list);
        lv.setAdapter(peerAdapter);
        lv.setEmptyView(findViewById(android.R.id.empty));
        lv.setSelection(0);
	}

	public void onClick(View v) {
		
		byte[] receiveData = new byte[1024];

		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

		try {
			
			serverSocket.receive(receivePacket);
			Log.i(TAG, "Received a packet");

			InetAddress sourceIPAddress = receivePacket.getAddress();
			Log.i(TAG, "Source IP Address: " + sourceIPAddress);
			
			/*
			 * TODO: Extract sender and receiver from message and display.
			 */
			String msgContents = new String(receivePacket.getData(), 0, receivePacket.getLength());
			String sender = msgContents.substring(0, msgContents.indexOf(":"));
			String message = msgContents.substring(msgContents.indexOf(":")+1);
			
			final Peer peer = new Peer(sender, sourceIPAddress, receivePacket.getPort());
			Message msg = new Message(0, message, sender);
			pm.persistAsync(peer, msg, new IContinue<Uri> () {
				public void	kontinue(Uri value) {
					peer.setId(PeerContract.getId(value));
				}
			});
			
			
			pm.getAllAsync(PeerContract.ALL_URI, projection, null, null, null,
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

	/*
	 * Close the socket before exiting application
	 */
	public void closeSocket() {
		serverSocket.close();
	}

	/*
	 * If the socket is OK, then it's running
	 */
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