package edu.stevens.cs522.activities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Pattern;

import android.Manifest;
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
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
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
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import edu.stevens.cs522.constants.constant;
import edu.stevens.cs522.entities.ChatMessage;
import edu.stevens.cs522.chatappcloud.R;
import edu.stevens.cs522.providers.CloudProvider;
import edu.stevens.cs522.services.AlarmReceiver;
import edu.stevens.cs522.services.LocationService;


public class ChatApp extends Activity implements
        LoaderManager.LoaderCallbacks<Cursor>,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    public final static String TAG = ChatApp.class.getCanonicalName();
    EditText msgTxt;
    Button send;

    private String clientName;
    private String portNo;
    private String hostStr;
    private String uuidStr;

    public static double latitude;
    public static double longitude;
    public GoogleApiClient googleApiClient;
    public LocationRequest locationRequest;
    public Location location;
    public Geocoder coder;
    boolean userAllowsUpdates = true;

    public static final int SYNC_TIME_IN_SEC = 1;
    public static final int LOC_INTERVAL_IN_SEC = 1;
    public static final int LOC_FAST_INTERVAL_IN_SEC = 1;
    public static final int LOC_INTERVAL_IN_MILLISEC = LOC_INTERVAL_IN_SEC * 1000;
    public static final int LOC_FAST_INTERVAL_IN_MILLISEC = LOC_FAST_INTERVAL_IN_SEC * 1000;

    AlarmManager alarmManager;
    boolean mBound = false;

    ListView msgList;
    ContentResolver cr;

    public SimpleCursorAdapter msgAdapter;
    public String[] from = new String[]{CloudProvider.SENDER, CloudProvider.TEXT};
    public int[] to = new int[]{R.id.cart_row_sender, R.id.cart_row_message};
    String clientId;
    Cursor cursor = null;
    AckReceiver receiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Check for MAPS Permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.i(TAG, "onStart: ");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 11);
        }
        Log.i(TAG, "onCreate: After Asking for permission");

        setContentView(R.layout.main);

        //Google Api Client
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();


        //Create location Request

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(LOC_INTERVAL_IN_MILLISEC);
        locationRequest.setFastestInterval(LOC_FAST_INTERVAL_IN_MILLISEC);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);


        //GeoCoder to get the location from lat&long
        coder = new Geocoder(getApplicationContext());

        //ResultReceiver for LocationService IntentService
        mResultReceiver = new AddressResultReceiver(new Handler());

        Intent myIntent = getIntent();
        clientId = myIntent.getStringExtra(constant.CLIENT_ID);
        msgList = (ListView) findViewById(R.id.msgList);
        msgTxt = (EditText) findViewById(R.id.message_text);

        cr = getContentResolver();
        cursor = cr.query(CloudProvider.CONTENT_URI, null, null, null, null);
        //msgAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2, null, new String[] {"sender", "text" }, new int[] { android.R.id.text1, android.R.id.text2 });
        msgAdapter = new SimpleCursorAdapter(this, R.layout.list_layout, null, new String[]{"sender", "latitude", "longitude", "text"}, new int[]{R.id.client_name_tv, R.id.client_lat, R.id.client_long, R.id.client_message});

        msgList.setAdapter(msgAdapter);
        msgAdapter.changeCursor(cursor);
        cursor.close();

        SharedPreferences prefs = this.getSharedPreferences(constant.SHARED_PREF, Context.MODE_PRIVATE);
        uuidStr = prefs.getString(constant.CLIENT_UUID, "00000000-8888-1111-a5e2-0800201c9a66");
        clientName = prefs.getString(constant.NAME, "DefaultClient");
        portNo = prefs.getString(constant.PORT, "8080");
        hostStr = prefs.getString(constant.HOST, "localhost");

        receiver = new AckReceiver(new Handler());

        Resources res = getResources();
        send = (Button) findViewById(R.id.send_button);
        send.setOnClickListener(sendListener);

        Long time = (long) (SYNC_TIME_IN_SEC * 1000); //(3 * 1000)
        Intent intentAlarm = new Intent(this, AlarmReceiver.class);
        intentAlarm.putExtra(constant.RECEIVER, receiver);
        PendingIntent sender = PendingIntent.getBroadcast(this, 12, intentAlarm, 0);
        intentAlarm.putExtra(constant.CLIENT_ID, clientId);
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // set the alarm for particular time
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), time, sender);
        Toast.makeText(this, "Syncing server", Toast.LENGTH_SHORT).show();
        msgAdapter.changeCursor(cursor);
    }


    private AddressResultReceiver mResultReceiver;

    class AddressResultReceiver extends ResultReceiver {
        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string
            // or an error message sent from the intent service.
            Log.i(TAG, "onReceiveResult: " + resultData.getString(constant.RESULT_DATA_KEY));

            // Show a toast message if an address was found.
            if (resultCode == constant.SUCCESS_RESULT) {
                addr = resultData.getString(constant.RESULT_DATA_KEY);
                Toast.makeText(getBaseContext(), "Address = " + addr, Toast.LENGTH_SHORT).show();
            }

        }
    }

    String addr = "NJ";

    private OnClickListener sendListener = new OnClickListener() {
        public void onClick(View v) {

            ContentValues cv = new ContentValues();
            double lat = latitude;
            double lon = longitude;
            try {
                //Address loc = coder.getFromLocation(lat, lon, 1).get(0);
                //add = loc.getPostalCode();

                //lat  40.7512520
                //long -74.0534790

                Intent intent = new Intent(getApplicationContext(), LocationService.class);
                intent.putExtra(constant.RECEIVER, mResultReceiver);
                intent.putExtra(constant.LOCATION_DATA_EXTRA, location);
                startService(intent);

            } catch (Exception e) {

            }
            String message = "Received from : " + addr + " | " + msgTxt.getText().toString();
            ChatMessage cm = new ChatMessage(0, message,
                    clientName, 0, new Date().getTime(), String.valueOf(latitude), String.valueOf(longitude));

            Toast.makeText(getApplicationContext(), String.valueOf(latitude), Toast.LENGTH_LONG).show();

            //ChatMessage cm = new ChatMessage(0, msgTxt.getText().toString(), clientName, 0, new Date().getTime());
            cm.writeToProvider(cv);
            try {
                getContentResolver().insert(CloudProvider.CONTENT_URI, cv);
            } catch (Exception e) {
                e.printStackTrace();
            }
            Cursor cursor = cr.query(CloudProvider.CONTENT_URI, null, null, null, null);
            msgAdapter.changeCursor(cursor);
        }
    };


    //Google Loaction Api Methods
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 11: {
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

    public void onConnected(Bundle bundle) {

        Log.i(TAG, "onConnected: ");
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(8000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);

        location = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        if (location != null) {
            Log.i(TAG, "onConnected: lat=" + location.getLatitude()+" .long="+ location.getLongitude());
        }else{
            Log.i(TAG, "onConnected: location is null");
        }
    }

    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Map connection has been suspend");
    }

    public void onLocationChanged(Location location) {
        this.location = location;
        longitude = location.getLongitude();
        latitude = location.getLatitude();

        Log.i(TAG, "onLocationChanged: latitude="+latitude+" ,longitude="+longitude);
    }


    public void onConnectionFailed(ConnectionResult connectionResult) {
         /* Google Play Service fails */
        if (connectionResult.hasResolution()) {
            // Start an Activity that tries to resolve the error
            //connectionResult.startResolutionForResult(this,CONNECTION_FAILURE_RESOLUTION_REQUEST);
            Log.i(TAG, "onConnectionFailed(), failed");
        }
    }

    public class AckReceiver extends ResultReceiver {
        public AckReceiver(Handler handler) {
            super(handler);
            // TODO Auto-generated constructor stub
        }

        protected void onReceiveResult(int resultCode, Bundle result) {
            switch (resultCode) {
                case 3:
                    //msgAdapter.notifyDataSetChanged();
                    cursor.requery();
                    msgAdapter.changeCursor(cursor);
                    Log.i(TAG, "OnReceiveResult case 3: Refresh Loader Managers");
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onStart() {
        googleApiClient.connect();
        if(googleApiClient.isConnected() || googleApiClient.isConnecting()){
            Log.i(TAG, "onStart: googleApiClient Connected");
        }else {
            Log.i(TAG, "onStart: googleApiClient Not Connected");
        }

        super.onStart();
    }

    @Override
    public void onResume() {
        if (googleApiClient.isConnected()){
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                Log.i(TAG, "onResume() permission not granted");
                return;
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        }

        super.onResume();

    }


    @Override
    public void onStop() {
        Intent intentAlarm = new Intent(this, AlarmReceiver.class);
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        PendingIntent sender = PendingIntent.getBroadcast(this, 12, intentAlarm, 0);
        try {
            alarmManager.cancel(sender);
        } catch (Exception e) {
            Log.e(TAG, "AlarmManager update was not cancelled. " + e.toString());
        }
        LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
        googleApiClient.disconnect();

        super.onStop();
    }

    @Override
    public void onDestroy() {
        Intent intentAlarm = new Intent(this, AlarmReceiver.class);
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        PendingIntent sender = PendingIntent.getBroadcast(this, 12, intentAlarm, 0);
        try {
            alarmManager.cancel(sender);
        } catch (Exception e) {
            Log.e(TAG, "AlarmManager update was not canceled. " + e.toString());
        }
        cr.delete(CloudProvider.CONTENT_URI, null, null);
        cr.delete(CloudProvider.CONTENT_URI_PEER, null, null);

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
                Intent peersIntent = new Intent(this, PeersActivity.class);
                startActivityForResult(peersIntent, 2);
                return true;
            case (R.id.show_settings):
                Intent settingsIntent = new Intent(this, startActivity.class);
                startActivityForResult(settingsIntent, 2);
                return true;
        }
        return false;
    }

    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {

        return new CursorLoader(this, CloudProvider.CONTENT_URI,
                CloudProvider.MessageProjection, null, null, null);
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        this.msgAdapter.swapCursor(cursor);

    }

    public void onLoaderReset(Loader<Cursor> loader) {
        this.msgAdapter.swapCursor(null);
    }


}
