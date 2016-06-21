package edu.stevens.cs522.chatappcloud;

import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.LoaderManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import edu.stevens.cs522.chat.entity.ChatMessage;
import edu.stevens.cs522.providers.MessageProviderCloud;
import edu.stevens.cs522.service.AlarmReceiver;

public class ChatApp extends Activity implements
		LoaderManager.LoaderCallbacks<Cursor> {
	final static public String TAG = ChatApp.class.getCanonicalName();
	public static final char SEPARATOR_CHAR = '|';
	private static final Pattern SEPARATOR = Pattern.compile(
			Character.toString(SEPARATOR_CHAR), Pattern.LITERAL);

	public static String[] readStringArray(String in) {
		return SEPARATOR.split(in);
	}
	static CursorAdapter messageAdapter;

	private String clientName;
	private String portNo;
	private String hostStr;
	private String uuidStr;
	private String latitude;
	private String longitute;
	private Date date;

	ArrayList<String> messageList;
	ArrayAdapter<String> adapter;
	EditText msgTxt;
	Button send;

	public static final String CLIENT_PORT_KEY = "client_port";

	public static final int DEFAULT_CLIENT_PORT = 8080;

	private int clientPort;
	AlarmManager alarmManager;
	boolean mBound = false;
	ListView msgList;

	ContentResolver cr;
	SimpleCursorAdapter msgAdapter;
    String clientId;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		Intent myIntent = getIntent();
		clientId = myIntent.getStringExtra("clientId");
		msgList = (ListView) findViewById(R.id.msgList);
		msgTxt = (EditText) findViewById(R.id.message_text);
        date = new Date();
		cr = getContentResolver();
		Cursor cursor = cr.query(MessageProviderCloud.CONTENT_URI, null, null, null, null);
		msgAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2, null, new String[] {
						"sender", "text" }, new int[] { android.R.id.text1,
						android.R.id.text2 });
		msgList.setAdapter(msgAdapter);
		msgAdapter.changeCursor(cursor);
		cursor.close();
		SharedPreferences prefs = this.getSharedPreferences("chatappcloud", Context.MODE_PRIVATE);
		
		uuidStr = prefs.getString("UUID", "00000000-a3e8-11e3-a5e2-0800201c9a66");
		clientName = prefs.getString("clientName", "myself");
		portNo = prefs.getString("portNo", "8080");
		hostStr = prefs.getString("hostStr", "localhost");
		Resources res = getResources();
		send = (Button) findViewById(R.id.send_button);
		send.setOnClickListener(sendListener);

		Long time = (long) (10);
		Intent intentAlarm = new Intent(this, AlarmReceiver.class);
		PendingIntent sender = 	PendingIntent.getBroadcast(this, 12, intentAlarm,0);
		intentAlarm.putExtra("clientId", clientId);
		alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

		alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
				SystemClock.elapsedRealtime(), time, 
				sender);
	}

	private OnClickListener sendListener = new OnClickListener() {
		public void onClick(View v) {
			ContentValues contentvalues = new ContentValues();
			ChatMessage cm = new ChatMessage(0, msgTxt.getText().toString(), clientName, 0, new Date().getTime());

            cm.writeToProvider(contentvalues);
			try {
				getContentResolver().insert(MessageProviderCloud.CONTENT_URI, contentvalues);
			} catch (Exception e) {
				e.printStackTrace();
			}
			Cursor cursor = cr.query(MessageProviderCloud.CONTENT_URI, null, null, null, null);
			msgAdapter.changeCursor(cursor);
		};
		
		
	};

	@Override
	public void onStop(){
		Intent intentAlarm = new Intent(this, AlarmReceiver.class);
		alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		PendingIntent sender = 	PendingIntent.getBroadcast(this, 12, intentAlarm,0);
		 try {
		        alarmManager.cancel(sender);
		    } catch (Exception e) {
		        Log.e(TAG, "AlarmManager update was not canceled. " + e.toString());
		    }
		 super.onStop();
	}

	@Override
	public void onDestroy(){
		
		Intent intentAlarm = new Intent(this, AlarmReceiver.class);
		alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		PendingIntent sender = 	PendingIntent.getBroadcast(this, 12, intentAlarm,0);
		 try {
		        alarmManager.cancel(sender);
		    } catch (Exception e) {
		        Log.e(TAG, "AlarmManager update was not canceled. " + e.toString());
		    }
		 cr.delete(MessageProviderCloud.CONTENT_URI, null, null);
		 cr.delete(MessageProviderCloud.CONTENT_URI_PEER, null, null);
		 super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);

		this.getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case (R.id.show_peers):
			openPeersList();
			return true;
        case (R.id.show_settings):
            openSettings();
            return true;
		}
		return false;
	}

	private void openPeersList() {
		Intent intent;
		intent = new Intent(this, PeersActivity.class);
		startActivityForResult(intent, 2);
	}

    private void openSettings() {
        Intent intent;
		intent = new Intent(this, EntryActivity.class);
        startActivityForResult(intent, 2);
    }

	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {

		return new CursorLoader(this, MessageProviderCloud.CONTENT_URI,
				MessageProviderCloud.MessageProjection, null, null, null);
	}

	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		this.msgAdapter.swapCursor(cursor);

	}

	public void onLoaderReset(Loader<Cursor> loader) {
		this.msgAdapter.swapCursor(null);
	}

}
