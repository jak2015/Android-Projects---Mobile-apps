package edu.stevens.cs522.multipane.service;

import java.util.ArrayList;
import java.util.UUID;

import edu.stevens.cs522.multipane.contract.Contract;
import edu.stevens.cs522.multipane.entity.Peer;
import edu.stevens.cs522.multipane.R;
import edu.stevens.cs522.multipane.provider.MessageProviderCloud;
import edu.stevens.cs522.multipane.request.Register;
import edu.stevens.cs522.multipane.request.RequestProcessor;
import edu.stevens.cs522.multipane.request.Synchronize;
import edu.stevens.cs522.multipane.request.Unregister;
import android.annotation.SuppressLint;
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
	public static final String REGISTER_ACTION = "edu.stevens.cs522.multipane.register";
	public static final String SYNCHRONIZE_ACTION = "edu.stevens.cs522.multipane.synchronize";
	public static final String UNREGISTER_ACTION = "edu.stevens.cs522.multipane.unregister";
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
		// Bundle b=intent.getExtras();

		switch (type) {
		case 0:

			Register register = intent.getExtras().getParcelable("register");
			// ContentResolver cr = getContentResolver();
			// ContentValues newValues = new ContentValues();
			// newValues.put("name", register.username);
			// newValues.put("senderid", register.id);
			// cr.insert(MessageProviderCloud.CONTENT_URI_PEER, newValues);
			resultReceiver = (ResultReceiver) intent.getExtras().getParcelable(
					"receiver");
			rp.perform(register, resultReceiver);
			sendNotify(1, register.username);
			break;
		case 1:
			//SharedPreferences prefs =this.getSharedPreferences("multipanechatapp", 0);
			
			
			Synchronize sync = new Synchronize();
			SharedPreferences prefs2 = this.getSharedPreferences(
					"multipanechatapp", 0);
			String uuid = prefs2.getString("clientUUID", UUID.randomUUID()
					.toString());
			String chatroom = prefs2.getString("currchatroom", "_default");
		//	sync.chatroom = chatroom;
		//	Log.d("RequestService triggered", "sync Room: " + chatroom);
			sync.regid = UUID.fromString(uuid);
			ContentResolver cr = getContentResolver();
			Cursor cursor = cr.query(MessageProviderCloud.CONTENT_URI, null,
					null, null, "messages.messageid");
			int messageid = 0;
			if (cursor.moveToFirst()) {

				messageid = Contract.getMessageId(cursor);

			}
			Log.d("getting seq NUM", String.valueOf(messageid));
			sync.id = messageid;
			String name = prefs2.getString("clientName", "missingName");
			Cursor c = cr.query(MessageProviderCloud.CONTENT_URI_PEER, null,
					"peers.name=?", new String[] { name }, null);
			Peer peer = new Peer();
			if (c.moveToFirst()) {
				do{
					peer = new Peer(c);
				}while(c.moveToNext());
			}
			c.close();
			Log.d("===========",chatroom);
//			Cursor c1 = cr.query(MessageProviderCloud.CONTENT_URI_CHATROOM, null, "name=?", new String[]{chatroom}, null);
//			Log.d("Search name for chatroom",String.valueOf(c1.getCount()));
//			if(c1!=null&&c1.getCount()>1){
//				if(c1.moveToFirst()){
					//sync.chatroom= String.valueOf(Contract.getChatroom(c1));
//				}
//			}
//			
			sync.chatroom=chatroom;
				sync.userId = String.valueOf(peer.id);
			
		
					
			String addr = "http://"+ prefs2.getString("hostStr", "localhost")+ ":"+prefs2.getString("portNo", "8080");
			sync.addr = addr;
			Log.d("testSync service", sync.addr+"==" + sync.id +"=="+ sync.userId);
			rp.perform(sync);
			break;
		case 2:
			Log.d("unregister service started", "--");
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
