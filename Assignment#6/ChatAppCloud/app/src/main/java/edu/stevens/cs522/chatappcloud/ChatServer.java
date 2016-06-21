
package edu.stevens.cs522.chatappcloud;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;


import android.app.Activity;
import android.content.ContentValues;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class ChatServer extends Activity implements OnClickListener {
	public static final char SEPARATOR_CHAR = '|';
	private static final Pattern SEPARATOR = Pattern.compile(
			Character.toString(SEPARATOR_CHAR), Pattern.LITERAL);

	public static String[] readStringArray(String in) {
		return SEPARATOR.split(in);
	}

	final static public String TAG = ChatServer.class.getCanonicalName();
	final static public int LOADER_ID = 1;
	private DatagramSocket serverSocket;
	private boolean socketOK = true;
	ArrayAdapter<String> messagesAdapter;
	ArrayList<String> message;
	ArrayList<String> messageAll;
	ListView listview;
	InetAddress sourceIPAddress;
	String[] msgContents;
	String msgStr;
	Button next;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
				.permitAll().build();
		StrictMode.setThreadPolicy(policy);

		try {
			int port = Integer.parseInt(this.getString(R.string.app_port));
			serverSocket = new DatagramSocket(port);
		} catch (Exception e) {
			Log.e(TAG, "Cannot open socket" + e.getMessage());
			return;
		}

		listview = (ListView) findViewById(R.id.msgList);
		message = new ArrayList<String>();
		messagesAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.message, message);
		listview.setAdapter(messagesAdapter);
		next.setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_activity_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
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

	public void onClick(View v) {

		byte[] receiveData = new byte[1024];

		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

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

		if (msgStr != null) {

			msgContents = readStringArray(msgStr);
			Log.i(TAG, "Received (name msgbody port) " + msgContents[0] + ": "
					+ msgContents[1] + ": " + msgContents[2]);
	//		String name = msgContents[0];
	//		message.add(msgContents[0] + ": " + msgContents[1]);
	//		messagesAdapter.notifyDataSetChanged();
	//		Log.d("Query the peer ", name);
		}
	}

	public void insertMsg(long id) {
		Log.d("insert Msg", String.valueOf(id));
	}

	public void closeSocket() {
		serverSocket.close();
	}

	boolean socketIsOK() {
		return socketOK;
	}
	public void insertPeer() {}

}