package edu.stevens.cs522.requests;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


import edu.stevens.cs522.activities.ChatApp;
import edu.stevens.cs522.constants.constant;
import edu.stevens.cs522.entities.Peer;
import edu.stevens.cs522.entities.ChatMessage;
import edu.stevens.cs522.providers.CloudProvider;
import edu.stevens.cs522.services.LocationService;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

public class RequestProcessor {
    RestMethod restMethod = new RestMethod();
    ContentResolver cr;
    Context ctx;

    public RequestProcessor(ContentResolver cr) {
        super();
        this.cr = cr;
    }
    public RequestProcessor(ContentResolver cr, Context ctx) {
        super();
        this.cr = cr;
        this.ctx = ctx;
    }

    public static final String TAG = RequestProcessor.class.getCanonicalName();
    public String addr, lat, lon;


    public void perform(Register request, ResultReceiver resultReceiver) {

        Response response = restMethod.perform(request);

        if (response != null) {
            Bundle bundle = new Bundle();

            ContentValues newValues = new ContentValues();
            newValues.put("name", request.username);
            newValues.put("_id", response.body);
            Log.i(TAG, "performing client registration, body= " + response.body);

            Uri uri = cr.insert(CloudProvider.CONTENT_URI_PEER_PASSWORD, newValues);
            if (uri != null) {
                Log.i(TAG, "performing client registration, insert success: uri= " + uri.toString());
            }

            bundle.putString(constant.CLIENT_ID, response.body);
            resultReceiver.send(1, bundle);
        } else {
            resultReceiver.send(2, null);
        }
    }

    public void perform(Synchronize request, ResultReceiver receiver) {
        Cursor cursor = cr.query(CloudProvider.CONTENT_URI_PASSWORD, null, "messageid=?", new String[]{"0"}, null);
        ArrayList<String> temp = new ArrayList<String>();
        ChatMessage cm1;
        if (cursor.moveToFirst()) {
            do {

                cm1 = new ChatMessage(cursor);
                Log.i(TAG, "messages to upload: " + cm1.messageText);
                temp.add(cm1.messageText);
            } while (cursor.moveToNext());
        }

        request.message = temp;
        StreamingResponse response = restMethod.perform(request);
        //update list of peers
        cr.delete(CloudProvider.CONTENT_URI_PEER_PASSWORD, null, null);
        if (response.usersList != null) {

            for (String user : response.usersList) {

                //Resolve Peer Address
                /*Location location = new Location("addressProvider");
                location.setLatitude(Double.parseDouble(user[1]));
                location.setLongitude(Double.parseDouble(user[2]));

                Intent intent = new Intent(ctx, LocationService.class);
                intent.putExtra(constant.RECEIVER, mResultReceiver);
                intent.putExtra(constant.LOCATION_DATA_EXTRA, location);
                ctx.startService(intent);
                */

                SharedPreferences prfs = ctx.getSharedPreferences(constant.SHARED_PREF, Context.MODE_PRIVATE);
                addr = prfs.getString(constant.PEER_ADDRESS, "NJ");

                ContentValues values = new ContentValues();
                new Peer(user, 1, addr).writeToProvider(values);
                Uri ip = cr.insert(CloudProvider.CONTENT_URI_PEER_PASSWORD, values);
                Log.i(TAG, "inserted peer: " +ip);
            }

            int i = cr.delete(CloudProvider.CONTENT_URI_PASSWORD, "messageid=?", new String[]{"0"});
            Log.i(TAG, "Delete TEMP messages# " + String.valueOf(i));

            for (String[] msg : response.msgList) {
                ChatMessage cm;

                Log.i(TAG, "perform: msgList msg[0]="+msg[0]+" ,msg[1]="+msg[1]+" ,msg[2]="+msg[2]+" ,msg[3]="+msg[3]+" ,msg[4]="+msg[4]+" ,msg[5]="+msg[5]+" ,msg[6]="+msg[6]);

                long seqnum = Long.parseLong(msg[4]);
                long date = Long.parseLong(msg[1]);

                Log.i(TAG, "perform: lon-msg[3]="+msg[3]);
                //cm = new ChatMessage(0, msg[6], msg[5], seqnum, date, msg[2], msg[3]);
                if(msg[2].equalsIgnoreCase("0.0")){
                    cm = new ChatMessage(0, msg[6], msg[5], seqnum, date, "39.668891", " -74.281346");
                }else{
                    cm = new ChatMessage(0, msg[6], msg[5], seqnum, date, msg[2], msg[3]);
                }




                ContentValues values = new ContentValues();
                cm.writeToProvider(values);
                cr.insert(CloudProvider.CONTENT_URI_PASSWORD, values);
            }
            Log.i(TAG, "perform: Messages and Peers synched");
        }
    }

    public void perform(Unregister request) {
    }

}