package edu.stevens.cs522.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import edu.stevens.cs522.constants.constant;

public class LocationService extends IntentService{

    public static final String TAG = LocationService.class.getCanonicalName();

    String lat, lon;

    public LocationService(String name) {
        super(name);
    }

    public LocationService() {
        super("LocationSERVICE");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.i(TAG, "onHandleIntent: ");

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        String errorMessage = "";

        // Get the location passed to this service through an extra.
        Location location = intent.getParcelableExtra(constant.LOCATION_DATA_EXTRA);
        mReceiver = intent.getParcelableExtra(constant.RECEIVER);

        List<Address> addresses = null;

        try {

            if(location != null){
                addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                Log.i(TAG, "onHandleIntent: addressx");
            }else{

                //"39.7512520", "-74.0534790"
                SharedPreferences prfs = this.getSharedPreferences(constant.SHARED_PREF, Context.MODE_PRIVATE);
                lat = prfs.getString(constant.LATITUDE, "39.7512520");
                lon = prfs.getString(constant.LONGITUDE, "-74.0534790");
                addresses = geocoder.getFromLocation(Double.parseDouble(lat), Double.parseDouble(lon), 1);
                Log.i(TAG, "onHandleIntent: addressx, if location is null, lat="+lat+" ,lon="+lon);
            }

        } catch (IOException ioException) {
            Log.e(TAG, errorMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            Log.e(TAG, errorMessage + ". " +
                    "Latitude = " + location.getLatitude() +
                    ", Longitude = " +
                    location.getLongitude(), illegalArgumentException);
        }

        // Handle case where no address was found.
        if (addresses == null || addresses.size()  == 0) {
            if (errorMessage.isEmpty()) {
                Log.i(TAG, "No address found");
            }
        } else {
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<String>();

            // Fetch the address lines using getAddressLine,
            // join them, and send them to the thread.
            for(int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));
            }
            Log.i(TAG, "Address Found");
            deliverResultToReceiver(constant.SUCCESS_RESULT,
                    TextUtils.join(System.getProperty("line.separator"),
                            addressFragments));
        }
    }

    protected ResultReceiver mReceiver;
    private void deliverResultToReceiver(int resultCode, String message) {
        Bundle bundle = new Bundle();
        bundle.putString(constant.RESULT_DATA_KEY, message);
        mReceiver.send(resultCode, bundle);
    }
}
