package edu.stevens.cs522.chat.managers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import edu.stevens.cs522.chat.contracts.MessageContract;
import edu.stevens.cs522.chat.contracts.PeerContract;
import edu.stevens.cs522.chat.entities.Message;
import edu.stevens.cs522.chat.entities.Peer;

public class PeerManager extends Manager<Peer>{

	public PeerManager(Context context, IEntityCreator<Peer> creator,
			int loaderID) {
		super(context, creator, loaderID);
		// TODO Auto-generated constructor stub
	}
	
	public void persistAsync(Peer peer, Message message, IContinue<Uri> callback) {
		ContentValues peerValues = new ContentValues();
		peer.writeToProvider(peerValues);
				
		peerValues.put(MessageContract.MESSAGE_MESSAGE, message.getMessageText());
		asyncResolver.insertAsync(PeerContract.CONTENT_URI, peerValues, callback);
	}
	
	public void getAllAsync(Uri uri, 
			String[] projection, 
			String selection, 
			String[] selectionArgs, 
			String sortOrder, 
			IContinue<Cursor> callback) {
		super.getAllAsync(uri, projection, selection, selectionArgs, sortOrder, callback);
	}
	
	public void deleteAsync (Uri uri, String selection, String[] selectionArgs) {
		super.deleteAsync(uri, selection, selectionArgs);
	}
}
