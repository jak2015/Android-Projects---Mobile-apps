package edu.stevens.cs522.services;

import java.util.UUID;

import edu.stevens.cs522.constants.constant;
import edu.stevens.cs522.contracts.Contract;
import edu.stevens.cs522.entities.Peer;
import edu.stevens.cs522.chatappcloud.R;
import edu.stevens.cs522.providers.CloudProvider;
import edu.stevens.cs522.requests.Register;
import edu.stevens.cs522.requests.RequestProcessor;
import edu.stevens.cs522.requests.Synchronize;
import edu.stevens.cs522.requests.Unregister;
import android.annotation.SuppressLint;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.ResultReceiver;
import android.util.Log;

public class RequestService extends IntentService {

	public static final String TAG = RequestService.class.getCanonicalName();
	ResultReceiver resultReceiver;
	public Context mContext;

	RequestProcessor requestProcessor;

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
		requestProcessor = new RequestProcessor(this.getContentResolver(), this);
		Log.i(TAG, "Starting Services");

		int type = intent.getExtras().getInt(constant.TYPE);
		switch (type) {
		case 0:
			Register register = intent.getExtras().getParcelable(constant.REGISTER);
			resultReceiver = (ResultReceiver) intent.getExtras().getParcelable(constant.RECEIVER);
			requestProcessor.perform(register, resultReceiver);
			sendNotify(1, register.username);
			break;

		case 1:
			Log.d(TAG, "RequestService triggered, sync");
            resultReceiver = (ResultReceiver) intent.getExtras().getParcelable(constant.RECEIVER);
			Synchronize sync = new Synchronize();
			SharedPreferences sharedPreferences = this.getSharedPreferences(constant.SHARED_PREF, 0);
			String uuid = sharedPreferences.getString(constant.CLIENT_UUID, UUID.randomUUID().toString());
			sync.regid = UUID.fromString(uuid);

			ContentResolver cr = getContentResolver();
			Cursor cursor = cr.query(CloudProvider.CONTENT_URI_PASSWORD, null,null, null, "message.messageid");
			int messageid = 0;
			if (cursor.moveToFirst()) {
				messageid = Contract.getMessageId(cursor);
			}
			Log.i(TAG, "Message Sequence #:"+String.valueOf(messageid));
			sync.id = messageid;

			String name = sharedPreferences.getString(constant.NAME, "DefaultClient");
			Cursor c = cr.query(CloudProvider.CONTENT_URI_PEER_PASSWORD, null,"peer.name=?", new String[] { name }, null);
			Peer peer = new Peer();
			if (c.moveToFirst()) {
				do{
					peer = new Peer(c);
				}while(c.moveToNext());
			}
			c.close();
            sync.userId = String.valueOf(peer.id);
			String addr = "http://"+ sharedPreferences.getString(constant.HOST, "localhost")+ ":"+sharedPreferences.getString(constant.PORT, "8080");
			sync.addr = addr;

            SharedPreferences prefs = this.getSharedPreferences(constant.SHARED_PREF,Context.MODE_PRIVATE);
            if(!sync.userId.equals("0")){
                SharedPreferences.Editor edit = prefs.edit();
                edit.putString("userId",sync.userId);
                edit.commit();
            }

            if(sync.userId.equals("0")){
                sync.userId = prefs.getString("userId","1");
            }

			Log.i(TAG, "test Sync service: "+sync.addr+"/" + sync.id +"/"+ sync.userId);
			requestProcessor.perform(sync, resultReceiver);

            //resultReceiver.send(3, null);
            //Log.i(TAG, "After sending resultReceiver notification for case 3");

            break;
		case 2:
			Unregister unreg = intent.getExtras().getParcelable(constant.UNREGISTER);
			requestProcessor.perform(unreg);
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
				.setContentTitle("Client Registered!").setContentText(text)
				.setSmallIcon(R.drawable.ic_launcher).setContentIntent(pIntent)
				.build();
		NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		noti.flags |= Notification.FLAG_AUTO_CANCEL;

		notificationManager.notify(0, noti);
	}

}
