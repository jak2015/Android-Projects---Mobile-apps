package edu.stevens.cs522.requests;

import java.util.ArrayList;


import edu.stevens.cs522.activities.ChatApp;
import edu.stevens.cs522.constants.constant;
import edu.stevens.cs522.entities.Peer;
import edu.stevens.cs522.entities.ChatMessage;
import edu.stevens.cs522.providers.CloudProvider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

public class RequestProcessor {
    RestMethod restMethod = new RestMethod();
    ContentResolver cr;

    public RequestProcessor(ContentResolver cr) {
        super();
        this.cr = cr;
    }

    public static final String TAG = RequestProcessor.class.getCanonicalName();

    public void perform(Register request, ResultReceiver resultReceiver) {

        Response response = restMethod.perform(request);

        if (response != null) {
            Bundle bundle = new Bundle();

            ContentValues newValues = new ContentValues();
            newValues.put("name", request.username);
            newValues.put("_id", response.body);
            Log.i(TAG, "performing client registration, body= " + response.body);

            Uri uri = cr.insert(CloudProvider.CONTENT_URI_PEER, newValues);
            if (uri != null) {
                Log.i(TAG, "performing client registration, insert success: uri= " + uri.toString());
            }

            bundle.putString(constant.CLIENT_ID, response.body);
            resultReceiver.send(1, bundle);
        } else {
            resultReceiver.send(2, null);
        }
    };

    public void perform(Synchronize request, ResultReceiver receiver) {
        Cursor cursor = cr.query(CloudProvider.CONTENT_URI, null, "messageid=?", new String[]{"0"}, null);
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
        cr.delete(CloudProvider.CONTENT_URI_PEER, null, null);
        if (response.usersList != null) {
            for (String name : response.usersList) {
                ContentValues values = new ContentValues();
                new Peer(name, 1).writeToProvider(values);
                Log.i(TAG, "inserted peer: " + cr.insert(CloudProvider.CONTENT_URI_PEER, values).toString());
            }

            int i = cr.delete(CloudProvider.CONTENT_URI, "messageid=?", new String[]{"0"});
            Log.i(TAG, "Delete TEMP messages# " + String.valueOf(i));

            for (String[] msg : response.msgList) {
                ChatMessage cm;
                long seqnum = Long.parseLong(msg[4]);
                long date = Long.parseLong(msg[1]);

                //chatMessage(id, text, sender, mid, date, lat, long
                cm = new ChatMessage(0, msg[6], msg[5], seqnum, date, msg[2], msg[3]);
                ContentValues values = new ContentValues();
                cm.writeToProvider(values);
                cr.insert(CloudProvider.CONTENT_URI, values);
            }
        }
    }

    ;

    public void perform(Unregister request) {
    }

    ;
}