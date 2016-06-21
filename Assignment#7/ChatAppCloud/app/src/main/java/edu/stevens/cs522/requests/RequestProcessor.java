package edu.stevens.cs522.requests;

import java.net.URI;
import java.util.ArrayList;
import edu.stevens.cs522.chat.entity.Peer;
import edu.stevens.cs522.chat.entity.ChatMessage;
import edu.stevens.cs522.providers.MessageProviderCloud;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
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
		//ContentResolver cr = ctx.getContentResolver();
		
		ContentValues newValues = new ContentValues();
		newValues.put("name", request.username);
		newValues.put("_id", response.body);
		Uri uri = cr.insert(MessageProviderCloud.CONTENT_URI_PEER, newValues);
		if(uri!=null){
			Log.i("RequestProcessor:insert susscess",uri.toString());
		}
		
		bundle.putString("clientId", response.body);
		resultReceiver.send(1, bundle);
	};

	public void perform(Synchronize request) {
		Cursor cursor = cr.query(MessageProviderCloud.CONTENT_URI,null, "messageid=?",new String[]{ "0"},null);
		ArrayList<String> temp = new ArrayList<String>();
		ChatMessage cm1;
		if(cursor.moveToFirst()){
			do{
				
				cm1 = new ChatMessage(cursor);
				Log.d("will to upload",cm1.messageText);
				temp.add(cm1.messageText);
			}while(cursor.moveToNext());
		}
		
		request.message= temp;
		StreamingResponse response = restMethod.perform(request);
		//update list of peers
		cr.delete(MessageProviderCloud.CONTENT_URI_PEER, null, null);
		if(response.usersList!=null){
		for(String name : response.usersList){
			 ContentValues values=new ContentValues();
			  new Peer(name,1).writeToProvider(values);
			Log.d("inserted peer", cr.insert(MessageProviderCloud.CONTENT_URI_PEER,values).toString());
		}

		int i = cr.delete(MessageProviderCloud.CONTENT_URI, "messageid=?",new String[]{ "0"});
		Log.d("Temp Msg been delete", String.valueOf(i));
		for(String[] msg:response.msgList)
		{
			  ChatMessage chatmsg;
              long seqnum = Long.parseLong(msg[4]);
              long date = Long.parseLong(msg[1]);
			  chatmsg = new ChatMessage(0, msg[6], msg[5],seqnum,date);
			  ContentValues values=new ContentValues();
			  chatmsg.writeToProvider(values);
			  cr.insert(MessageProviderCloud.CONTENT_URI, values);
		}
		}
	};

	public void perform(Unregister request) {
	};
}