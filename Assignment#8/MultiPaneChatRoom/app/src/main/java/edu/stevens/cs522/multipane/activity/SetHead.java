package edu.stevens.cs522.multipane.activity;

import java.util.UUID;

import edu.stevens.cs522.multipane.R;
import edu.stevens.cs522.multipane.helper.ServiceHelper;
import edu.stevens.cs522.multipane.provider.MessageProviderCloud;
import edu.stevens.cs522.multipane.request.Register;
import edu.stevens.cs522.multipane.request.RequestProcessor;
import edu.stevens.cs522.multipane.service.AlarmReceiver;
import edu.stevens.cs522.multipane.service.RequestService;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

@SuppressLint("NewApi")
public class SetHead extends Activity {
	public static final String NAME = "name";
	public static final String PORT = "port";
	public static final String HOST = "host";
	public static final String PREFS_NAME = "multipanechatapp";
	public static boolean networkOn = false;
	AckReceiver receiver;
	String clientName, portNo, hostStr, uuid;
	int ipt;
	Context mContext;
	AlarmManager alarmManager;
	final static public String TAG = SetHead.class.getCanonicalName();
ContentResolver cr;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try{
		cr.delete(MessageProviderCloud.CONTENT_URI, null, null);
		cr.delete(MessageProviderCloud.CONTENT_URI_PEER, null, null);
		cr.delete(MessageProviderCloud.CONTENT_URI_CHATROOM, null, null);
		}catch(Exception e){
			e.printStackTrace();
		}
		setContentView(R.layout.login);
		receiver = new AckReceiver(new Handler());
		mContext = this.getApplicationContext();
		SharedPreferences prfs = getSharedPreferences(PREFS_NAME, 0);
		uuid = UUID.randomUUID().toString();
		SharedPreferences.Editor editor = prfs.edit();
		editor.putString("clientUUID", uuid);
		editor.commit();Intent intentAlarm = new Intent(this, AlarmReceiver.class);
		alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		PendingIntent sender = PendingIntent.getBroadcast(this, 12,
				intentAlarm, 0);
		try {
			alarmManager.cancel(sender);
		} catch (Exception e) {
			Log.e(TAG, "AlarmManager update was not canceled. " + e.toString());
		}
		// NAME = res.getString(R.string.user_name) ;
		// name_field = (EditText) findViewById(R.id.name_field) ;
	}

	public class AckReceiver extends ResultReceiver {
		public AckReceiver(Handler handler) {
			super(handler);
			// TODO Auto-generated constructor stub
		}

		protected void onReceiveResult(int resultCode, Bundle result) {
			switch (resultCode) {
			case 1:
				String client = result.getString("clientId");
				if (client != null) {
					Log.d("OnReceiveResult id:", client);
				}

				// Toast.makeText(context, "success register " +
				// result.getString("clientId"),Toast.LENGTH_SHORT).show();
				Intent i = new Intent(mContext, FragmentLayout.class);
				 i.putExtra(NAME, clientName);
				 i.putExtra(PORT, portNo);
				 i.putExtra(HOST, hostStr);
				 i.putExtra("clientId", client);
				 startActivity(i);
				break;
			case 0:
				Toast.makeText(mContext, "Login Fail, Can't find server.",
						Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}

		}
	};

	// the function will be called when the start chat button is clicked
	public void start_chat(View view) {
		EditText username = (EditText) findViewById(R.id.name_field);
		EditText port = (EditText) findViewById(R.id.port_text);
		EditText host = (EditText) findViewById(R.id.dest_text);
		clientName = username.getText().toString();
		portNo = port.getText().toString();
		hostStr = host.getText().toString();

		if (username.getText().toString().matches("")
				|| port.getText().toString().matches("")
				|| host.getText().toString().matches("")) {
			Toast.makeText(this, "Please fill in every field",
					Toast.LENGTH_SHORT).show();
		} else {
			
			Register register = new Register(0, UUID.fromString(uuid),
					clientName, "http://" + hostStr + ":" + portNo);
			SharedPreferences prefs = this.getSharedPreferences("multipanechatapp", Context.MODE_PRIVATE);
			SharedPreferences.Editor editor = prefs.edit();
			editor.putString("clientName", register.username);
			editor.putString("portNo", portNo);
			editor.putString("hostStr", hostStr);
			editor.commit();
			ServiceHelper.getInstance(this).register(register, receiver);
		}

	}
}
