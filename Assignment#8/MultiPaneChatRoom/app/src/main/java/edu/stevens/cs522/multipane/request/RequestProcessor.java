package edu.stevens.cs522.multipane.request;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.JsonReader;
import android.util.Log;
import edu.stevens.cs522.multipane.contract.Contract;
import edu.stevens.cs522.multipane.entity.ChatMessage;
import edu.stevens.cs522.multipane.entity.Peer;
import edu.stevens.cs522.multipane.provider.MessageProviderCloud;

public class RequestProcessor {
	RestMethod restMethod = new RestMethod();

	// Get the Content Resolver.
	ContentResolver cr;

	public RequestProcessor(ContentResolver cr) {
		super();
		this.cr = cr;
	}

	public void perform(Register request, ResultReceiver resultReceiver) {
		Response response = restMethod.perform(request);
		Bundle bundle = new Bundle();
		ContentValues newValues = new ContentValues();
		newValues.put("name", request.username);
		newValues.put("_id", response.body);
		
		Uri uri = cr.insert(MessageProviderCloud.CONTENT_URI_PEER, newValues);
		if (uri != null) {
			Log.i("RequestProcessor:insert susscess", uri.toString());
		}

		bundle.putString("clientId", response.body);
		resultReceiver.send(1, bundle);
	};

	public void perform(Synchronize sync) {
		Cursor cursor = cr.query(MessageProviderCloud.CONTENT_URI, null,
				"messageid=?", new String[] { "0" }, null);
		ArrayList<String> temp = new ArrayList<String>();
		ChatMessage cm1;
		if (cursor.moveToFirst()) {
			do {

				cm1 = new ChatMessage(cursor);
				Log.d("will to upload", cm1.messageText);
				temp.add(cm1.messageText);
			} while (cursor.moveToNext());
		}

		sync.message = temp;
		StreamingResponse response = restMethod.perform(sync);
		// update list of peers
		cr.delete(MessageProviderCloud.CONTENT_URI_PEER, null, null);
		if (response.usersList != null) {
			for (String name : response.usersList) {
				ContentValues values = new ContentValues();
				new Peer(name, 1).writeToProvider(values);
				Log.d("inserted peer",
						cr.insert(MessageProviderCloud.CONTENT_URI_PEER, values)
								.toString());
			}

			int i = cr.delete(MessageProviderCloud.CONTENT_URI, "messageid=?",
					new String[] { "0" });
			Log.d("Temp Msg been delete", String.valueOf(i));
			for (String[] msg : response.msgList) {
				Log.d("JSON back", msg[0]+"--"+msg[1]+"--"+msg[2]+"--"+msg[3]+"--"+msg[4]+"--"+msg[5]+"--"+msg[6]);
				String chatroomId=null;
				String senderId = "0";
				ChatMessage cm;
				Cursor c1 = cr.query(MessageProviderCloud.CONTENT_URI_CHATROOM, null, "name=?", new String[]{msg[0]}, null);
				if(c1!=null && c1.getCount()>0){
					if(c1.moveToFirst()){
						chatroomId = String.valueOf(Contract.getId(c1));
						Log.d("Found that Chatroom ", chatroomId +":"+ msg[0]);
					}
				}else{
					ContentValues values = new ContentValues();
					values.put("name", msg[0]);
					Uri uri = cr.insert(MessageProviderCloud.CONTENT_URI_CHATROOM, values);
					chatroomId=uri.getLastPathSegment();
					Log.d("NOT Found that Chatroom ", chatroomId +":"+ msg[0]);
				}
				
				Cursor c2 = cr.query(MessageProviderCloud.CONTENT_URI_PEER, null, "name=?", new String[]{msg[5]}, null);
				if(c2!=null && c2.getCount()>0){
					if(c2.moveToFirst()){
						senderId = String.valueOf(Contract.getId(c2));
						Log.d("New Msg from sender", senderId);
					}
				}else{
					ContentValues values = new ContentValues();
					values.put("name", msg[3]);
					cr.insert(MessageProviderCloud.CONTENT_URI_PEER, values);
				}
				
				cm = new ChatMessage(0, msg[6], msg[5], Long.parseLong(msg[4]),Long.parseLong(chatroomId),
						Long.parseLong(msg[1]), Long.parseLong(senderId));
				
				ContentValues values = new ContentValues();
				cm.writeToProvider(values);
				cr.insert(MessageProviderCloud.CONTENT_URI, values);

			}
		}
	};


	public void perform(Unregister unregister) {
		Response response = restMethod.perform(unregister);
	
};
}
