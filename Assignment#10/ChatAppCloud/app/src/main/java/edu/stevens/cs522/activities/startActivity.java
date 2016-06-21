package edu.stevens.cs522.activities;

import java.util.UUID;

import edu.stevens.cs522.chatappcloud.R;
import edu.stevens.cs522.constants.constant;
import edu.stevens.cs522.helpers.ServiceHelper;
import edu.stevens.cs522.requests.Register;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;

@SuppressLint("NewApi")
public class startActivity extends Activity {

	public static final String TAG = startActivity.class.getCanonicalName();
	public static boolean networkOn = false;
	AckReceiver receiver;
	String clientName, portNo, hostStr, uuid;
	int ipt;
	Context mContext;
	public GoogleApiClient googleApiClient;
	public LocationRequest locationRequest;
	public String latitude;
	public String longitude;

    public Button login;


	@Override
	public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
		switch (requestCode) {
			case 12: {
				// If request is cancelled, the result arrays are empty.
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
					// permission was granted, yay! Do the task you need to do.
					Log.i(TAG, "onRequestPermissionsResult: Permission Granted");

				} else {
					// permission denied, boo! Disable the functionality that depends on this permission.
					Log.i(TAG, "onRequestPermissionsResult: Permission denied");
				}
				return;
			}
			default:
				Log.i(TAG, "onRequestPermissionsResult: default case");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_settings);
		receiver = new AckReceiver(new Handler());
		mContext = this.getApplicationContext();

        //Check for MAPS Permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "onCreate: ");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 12);
        }


        locationRequest = new LocationRequest();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(500);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        login = (Button)findViewById(R.id.name_btn);
        login.setOnClickListener(loginListener);

	}

	public class AckReceiver extends ResultReceiver {
		public AckReceiver(Handler handler) {
			super(handler);
			// TODO Auto-generated constructor stub
		}

		protected void onReceiveResult(int resultCode, Bundle result) {
			switch (resultCode) {
				case 0:
					Log.i(TAG, "OnReceiveResult case 0: Error login Can't find server");
					Toast.makeText(mContext, "Error Login - Can't find server!!",Toast.LENGTH_SHORT).show();
					break;
				case 1:
					String clientId = result.getString(constant.CLIENT_ID);
					if (clientId != null) {
						Log.i(TAG, "OnReceiveResult case 1: client id:"+clientId);
					}
					Intent i = new Intent(mContext, ChatApp.class);
					i.putExtra(constant.PORT, portNo);
					i.putExtra(constant.NAME, clientName);
					i.putExtra(constant.HOST, hostStr);
					i.putExtra(constant.CLIENT_ID, clientId);
					startActivity(i);
					break;
				case 2:
					Log.i(TAG, "OnReceiveResult case 2: Client already exists");
					Toast.makeText(mContext, "Client Name already exists",Toast.LENGTH_SHORT).show();
					break;
			default:
				break;
			}
		}
	};


    private View.OnClickListener loginListener = new View.OnClickListener() {
        public void onClick(View v) {
            EditText username = (EditText) findViewById(R.id.name_field);
            EditText port = (EditText) findViewById(R.id.port_text);
            EditText host = (EditText) findViewById(R.id.dest_text);

            clientName = username.getText().toString();
            portNo = port.getText().toString();
            hostStr = host.getText().toString();

            if (username.getText().toString().matches("") || port.getText().toString().matches("") || host.getText().toString().matches("")) {
                Toast.makeText(mContext, "All fields are reqiured",Toast.LENGTH_SHORT).show();
            } else {

                SharedPreferences prfs = getSharedPreferences(constant.SHARED_PREF, 0);
                uuid = UUID.randomUUID().toString();
                SharedPreferences.Editor editor = prfs.edit();
                editor.putString(constant.CLIENT_UUID, uuid);

                Register register = new Register(0, UUID.fromString(uuid), clientName, "http://" + hostStr + ":" + portNo);

                editor.putString(constant.NAME, register.username);
                editor.putString(constant.PORT, portNo);
                editor.putString(constant.HOST, hostStr);
                editor.commit();

                ServiceHelper.getInstance(mContext).register(register, receiver);
            }
        };
    };


	// the function will be called when the login chat button is clicked
	/*public void login(View view) {
		EditText username = (EditText) findViewById(R.id.name_field);
		EditText port = (EditText) findViewById(R.id.port_text);
		EditText host = (EditText) findViewById(R.id.dest_text);

		clientName = username.getText().toString();
		portNo = port.getText().toString();
		hostStr = host.getText().toString();

		if (username.getText().toString().matches("") || port.getText().toString().matches("") || host.getText().toString().matches("")) {
			Toast.makeText(this, "All fields are reqiured",Toast.LENGTH_SHORT).show();
		} else {

			SharedPreferences prfs = getSharedPreferences(constant.SHARED_PREF, 0);
			uuid = UUID.randomUUID().toString();
			SharedPreferences.Editor editor = prfs.edit();
			editor.putString(constant.CLIENT_UUID, uuid);

			Register register = new Register(0, UUID.fromString(uuid), clientName, "http://" + hostStr + ":" + portNo);

			editor.putString(constant.NAME, register.username);
			editor.putString(constant.PORT, portNo);
			editor.putString(constant.HOST, hostStr);
			editor.commit();

			ServiceHelper.getInstance(this).register(register, receiver);
		}
	}*/


}
