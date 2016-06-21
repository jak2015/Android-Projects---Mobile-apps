package edu.stevens.cs522.requests;

import java.net.URI;
import java.util.ArrayList;
import edu.stevens.cs522.chat.entity.Peer;
import edu.stevens.cs522.chat.entity.ChatMessage;
import edu.stevens.cs522.providers.MessageProviderCloud;
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

	public void perform(Register request, ResultReceiver resultReceiver) {
		Response response = restMethod.perform(request);
		Bundle bundle = new Bundle();
		ContentValues newValues = new ContentValues();
		newValues.put("name", request.username);
		newValues.put("_id", response.body);
		Uri uri = cr.insert(MessageProviderCloud.CONTENT_URI_PEER, newValues);
		if(uri!=null){
		}
		
		bundle.putString("clientId", response.body);
		resultReceiver.send(1, bundle);
	};

	public void perform(Synchronize request) {
		Cursor cursor = cr.query(MessageProviderCloud.CONTENT_URI,null, "messageid=?",new String[]{ "0"},null);
		String temp ="";
		ChatMessage cm1;
		if(cursor.moveToFirst()){
			do{
				
				cm1 = new ChatMessage(cursor);
				temp = cm1.messageText;
			}while(cursor.moveToNext());
		}

		request.message = temp;
		StreamingResponse response = restMethod.perform(request);
		cr.delete(MessageProviderCloud.CONTENT_URI_PEER, null, null);
		if(response.usersList!=null){
		for(String name : response.usersList){
			 ContentValues values=new ContentValues();
			  new Peer(name,1).writeToProvider(values);
			Log.d("inserted peer", cr.insert(MessageProviderCloud.CONTENT_URI_PEER,values).toString());
		}

		int i = cr.delete(MessageProviderCloud.CONTENT_URI, "messageid=?",new String[]{ "0"});
		Log.d("Temp Msg been delete", String.valueOf(i));
		for(String[] msg:response.msgList){
			  ChatMessage chatmsg;
				chatmsg = new ChatMessage(0, msg[4], msg[3],Long.parseLong(msg[2]),Long.parseLong(msg[1]));
				ContentValues values=new ContentValues();
				chatmsg.writeToProvider(values);
				cr.insert(MessageProviderCloud.CONTENT_URI, values);
			  }
		}
	};

	public void perform(Unregister request) {
	};
}
