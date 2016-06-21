package edu.stevens.cs522.service;

import java.util.ArrayList;
import java.util.UUID;
import java.util.prefs.Preferences;
import edu.stevens.cs522.chat.contracts.Contract;
import edu.stevens.cs522.chat.entity.Peer;
import edu.stevens.cs522.chatappcloud.ChatApp;
import edu.stevens.cs522.chatappcloud.R;
import edu.stevens.cs522.providers.MessageProviderCloud;
import edu.stevens.cs522.requests.Register;
import edu.stevens.cs522.requests.RequestProcessor;
import edu.stevens.cs522.requests.Synchronize;
import edu.stevens.cs522.requests.Unregister;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

public class RequestService extends IntentService {
	public static final String REGISTER_ACTION = "edu.stevens.cs522.chatappcloud.register";
	public static final String SYNCHRONIZE_ACTION = "edu.stevens.cs522.chatappcloud.synchronize";
	public static final String UNREGISTER_ACTION = "edu.stevens.cs522.chatappcloud.unregister";
	ResultReceiver resultReceiver;
	public Context mContext;

	RequestProcessor rp;

	public RequestService(String name) {
		super(name);
		// TODO Auto-generated constructor stub
	}

	public RequestService() {
		super("RequestService");
		// TODO Auto-generated constructor stub
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		rp = new RequestProcessor(this.getContentResolver());
		Log.d("service start", "--");
		int type = intent.getExtras().getInt("type");

		switch (type) {
		case 0:

			Register register = intent.getExtras().getParcelable("register");
			resultReceiver = (ResultReceiver) intent.getExtras().getParcelable("receiver");
			rp.perform(register, resultReceiver);
			sendNotify(1, register.username);
			break;
		case 1:
			Synchronize sync = new Synchronize();
			SharedPreferences prefs2 = this.getSharedPreferences("chatappcloud", 0);
			String uuid = prefs2.getString("clientUUID", UUID.randomUUID().toString());
			sync.regid = UUID.fromString(uuid);
			ContentResolver cr = getContentResolver();
			Cursor cursor = cr.query(MessageProviderCloud.CONTENT_URI, null, null, null, "message.messageid");
			int messageid = 0;
			if (cursor.moveToFirst()) {
				messageid = Contract.getMessageId(cursor);
			}

			sync.id = messageid;
			String name = prefs2.getString("clientName", "missingName");
			Cursor c = cr.query(MessageProviderCloud.CONTENT_URI_PEER, null, "peer.name=?", new String[] { name }, null);
			Peer peer = new Peer();
			if (c.moveToFirst()) {
				do{
					peer = new Peer(c);
				}while(c.moveToNext());
			}
			c.close();

			sync.userId = String.valueOf(peer.id);
			String addr = "http://"+ prefs2.getString("hostStr", "localhost")+ ":"+prefs2.getString("portNo", "8080");
			sync.addr = addr;
			Log.d("testSync service", sync.addr+"==" + sync.id +"=="+ sync.userId);
            SharedPreferences prefs = this.getSharedPreferences("chatappcloud", Context.MODE_PRIVATE);
            if(!sync.userId.equals("0")){
                SharedPreferences.Editor edit = prefs.edit();
                edit.putString("userId",sync.userId);
                edit.commit();
            }

            if(sync.userId.equals("0"))
			{
                sync.userId = prefs.getString("userId","1");
            }
			rp.perform(sync);
			break;
		case 2:
			Unregister unreg = intent.getExtras().getParcelable("unregister");
			rp.perform(unreg);
			break;
		default:
			break;
		}
	}

	@SuppressLint("NewApi")
	private void sendNotify(int id, String text) {
		Intent intent = new Intent();
		PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);

		Notification noti = new Notification.Builder(this)
				.setContentTitle("New client registered!").setContentText(text)
				.setSmallIcon(R.drawable.ic_launcher).setContentIntent(pIntent)
				.build();
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		noti.flags |= Notification.FLAG_AUTO_CANCEL;

		notificationManager.notify(0, noti);
	}
}
