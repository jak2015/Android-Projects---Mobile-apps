package edu.stevens.cs522.activities;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.regex.Pattern;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import edu.stevens.cs522.chatappcloud.R;

public class ChatServer extends Activity implements OnClickListener {
	public static final char SEPARATOR_CHAR = '|';
	private static final Pattern SEPARATOR = Pattern.compile(
			Character.toString(SEPARATOR_CHAR), Pattern.LITERAL);

	public static String[] readStringArray(String in) {
		return SEPARATOR.split(in);
	}

	final static public String TAG = ChatServer.class.getCanonicalName();
	final static public int LOADER_ID = 1;
//	MessageManager messageManager;
//	PeerManager peerManager;
	// ChatDbAdapter chatDbAdapter;
	/*
	 * Socket used both for sending and receiving
	 */
	private DatagramSocket serverSocket;

	/*
	 * True as long as we don't get socket errors
	 */
	private boolean socketOK = true;
	/*
	 * TODO: Declare UI.
	 */
	ArrayAdapter<String> messagesAdapter;
	ArrayList<String> message;
	ArrayList<String> messageAll;
	ListView listview;

	/*
	 * End Todo
	 */

	Button next;

	/*
	 * Called when the activity is first created.
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		// chatDbAdapter =new ChatDbAdapter(this);
		// chatDbAdapter.open();
//		messageManager = new MessageManager(this,
//				new IEntityCreator<Msg>() {
//
//					public Msg create(Cursor cursor) {
//						return new Msg(cursor);
//					}
//				}, LOADER_ID);
//		peerManager = new PeerManager(this, new IEntityCreator<PeerOld>() {
//
//			public PeerOld create(Cursor cursor) {
//				return new PeerOld(cursor);
//			}
//		}, 2);
		/**
		 * Let's be clear, this is a HACK to allow you to do network
		 * communication on the main thread. This WILL cause an ANR, and is only
		 * provided to simplify the pedagogy. We will see how to do this right
		 * in a future assignment (using a Service managing background threads).
		 */
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
				.permitAll().build();
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
		listview = (ListView) findViewById(R.id.msgList);
		message = new ArrayList<String>();
		messagesAdapter = new ArrayAdapter<String>(getApplicationContext(),
				R.layout.message, message);
		// messages = new
		// ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,
		// values);
		listview.setAdapter(messagesAdapter);
		// sync query------------------------------------
		// Cursor c = messageManager.query(MessageContract.CONTENT_URI, null,
		// null, null, null);
		// if (c.moveToFirst()) {
		// String name;
		// String msg;
		// do {
		// name = c.getString(c
		// .getColumnIndex("name"));
		// msg = c.getString(c
		// .getColumnIndex("message"));
		// message.add(name + ":" + msg);
		// } while (c.moveToNext());
		// messagesAdapter.notifyDataSetChanged();
		// }
		// Async query-----------------------------------------
//		messageManager.queryAsync(MessageContract.CONTENT_URI, null, null,
//				null, null, new IContinue<Cursor>() {
//					String name;
//					String msg;
//
//					public void kontinue(Cursor value) {
//						// ArrayList<String> arrStr = new ArrayList<String>();
//						if (value.moveToFirst()) {
//							do {
//								name = value.getString(value
//										.getColumnIndex("name"));
//								msg = value.getString(value
//										.getColumnIndex("message"));
//								message.add(name + ":" + msg);
//							} while (value.moveToNext());
//						}
//						messagesAdapter.notifyDataSetChanged();
//					}
//				});

		//next = (Button) findViewById(R.id.next);
		next.setOnClickListener(this);
		/*
		 * End Todo
		 */
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu items for use in the action bar
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_activity_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.action_peers:
			openPeersList();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void openPeersList() {
		// TODO Auto-generated method stub
		Intent i;
		i = new Intent(this, PeersActivity.class);
		startActivityForResult(i, 2);
	}

	InetAddress sourceIPAddress;
	String[] msgContents;
	String msgStr;

	public void onClick(View v) {

		byte[] receiveData = new byte[1024];

		DatagramPacket receivePacket = new DatagramPacket(receiveData,
				receiveData.length);

		try {

			serverSocket.receive(receivePacket);
			Log.i(TAG, "Received a packet");

			sourceIPAddress = receivePacket.getAddress();
			String ipName = sourceIPAddress.getHostName();
			msgStr = new String(receivePacket.getData());
			Log.i(TAG, "Source IP Address: " + ipName);

		} catch (Exception e) {

			Log.e(TAG, "Problems receiving packet: " + e.getMessage());
			socketOK = false;
		}
		// Peer peer = new Peer(0, msgContents[0], sourceIPAddress,
		// Integer.parseInt(msgContents[2]));
		// Message msg = new Message(0, msgContents[1], msgContents[0]);

		if (msgStr != null) {

			msgContents = readStringArray(msgStr);
			Log.i(TAG, "Received (name msgbody port) " + msgContents[0] + ": "
					+ msgContents[1] + ": " + msgContents[2]);
			// Log.i(TAG, "Received (name msgbody port) "+msgContents[2] +":");
			// Log.i(TAG, "Received from " + name + ": " + msgContents[1]);
			String name = msgContents[0];
			message.add(msgContents[0] + ": " + msgContents[1]);
			messagesAdapter.notifyDataSetChanged();
			Log.d("Query the peer ", name);
			// query sync-----------------------------------------------------
//			Cursor value = peerManager.query(PeerContract.CONTENT_URI, null,
//					"name", new String[] { name }, null);
//			if (value != null && value.moveToFirst()) {
//				for (value.moveToFirst(); !value.isAfterLast(); value
//						.moveToNext()) {
//					Log.d("peer found", "add new message");
//					Peer peer = new Peer(value);
//					insertMsg(peer.id);
//					// value.getString(value.getColumnIndex("name"));
//					// value.getString(value.getColumnIndex("message"));
//					// mess
//				}
//
//			} else {
//				Log.d("peer not found", "add new peer & msg");
//				insertPeer();
//			}
			// query Async
			// ----------------------------------------------------------------------
//			peerManager.queryAsync(PeerContract.CONTENT_URI, null, "name",
//					new String[] { name }, null, new IContinue<Cursor>() {
//						public void kontinue(Cursor value) {
//							if (value != null && value.moveToFirst()) {
//								for (value.moveToFirst(); !value.isAfterLast(); value
//										.moveToNext()) {
//									Log.d("peer found", "add new message");
//									PeerOld peer = new PeerOld(value);
//									insertMsg(peer.id);
//								}
//							} else {
//								insertPeer();
//							}
//
//						}
//
//					});
			// msgStr=null;
		}
	}

	public void insertPeer() {
//		PeerOld peer;
//		peer = new PeerOld(0, msgContents[0], sourceIPAddress,
//				Integer.parseInt(msgContents[2]));
//		Log.d("insert Peer(): ", peer.port + peer.address.toString()
//				+ peer.name);
//		peerManager.persistAsync(peer, new IContinue<Uri>() {
//
//			public void kontinue(Uri uri) {
//
//				insertMsg(MessageContract.getId(uri));
//
//			}
//		});
//
//		// Log.d("insertPeer(): ", msg.getSender() + msg.getMessageText());

		// messageManager.persistAsync(msg, peer);

	}

	public void insertMsg(long id) {
		Log.d("insert Msg", String.valueOf(id));
//		Msg msg = new Msg(id, msgContents[1], msgContents[0]);
//		messageManager.persistAsync(msg, id);

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

}