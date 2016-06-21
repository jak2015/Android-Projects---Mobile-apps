package edu.stevens.cs522.displaylocation;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MainActivity extends AppCompatActivity {

    GoogleMap map;
    Double lat,lon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Cursor cursor = getContentResolver().query(Uri.parse("content://chatappcloud/message"), null, null, null, null);
        int i = 0;
        if(cursor.getCount() == 0){
            Toast.makeText(getApplicationContext(),"No user to display", Toast.LENGTH_LONG).show();
        }else{
            cursor.moveToFirst();

            while(cursor.moveToNext()){
                String user = cursor.getString(cursor.getColumnIndex("sender"));
                String Latitude = cursor.getString(cursor.getColumnIndex("latitude"));
                String Longitude = cursor.getString(cursor.getColumnIndex("longitude"));
                String message = cursor.getString(cursor.getColumnIndex("text"));
                addMarker(Latitude,Longitude, user,message);
                Toast.makeText(getApplicationContext(), String.valueOf(cursor.getCount()), Toast.LENGTH_LONG).show();
                i++;
            }
        }
    }

    public void addMarker(String Lat, String Longi, String user, String message){

        lat = Double.parseDouble(Lat);
        lon = Double.parseDouble(Longi);



        try {

            LatLng ll = new LatLng(lat, lon);
            String userAddress = message.substring(0,message.indexOf("|"));
            map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(new LatLng(lat, lon))
                    .title("Received From:"+user+", "+userAddress)
                    .draggable(true);

            map.addMarker(markerOptions);
            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
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
            map.setMyLocationEnabled(true);
            map.getUiSettings().setZoomControlsEnabled(true);
            map.getUiSettings().setZoomGesturesEnabled(true);
            map.getUiSettings().setCompassEnabled(true);

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(ll)
                    .tilt(30)
                    .build();

            map.animateCamera
                    (CameraUpdateFactory
                            .newCameraPosition(cameraPosition));


        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
