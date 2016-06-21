package managers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Message;

import edu.stevens.cs522.chat.oneway.Contract;
import edu.stevens.cs522.chat.oneway.Peer;

public class PeerManager extends managers.Manager<Peer> {

    public PeerManager(Context context, IEntityCreator<Peer> creator,
                       int loaderID) {
        super(context, creator, loaderID);
    }

    public void persistAsync(Peer peer, Message message, IContinue<Uri> callback) {
        ContentValues peerValues = new ContentValues();
        peer.writeToProvider(peerValues);
        asyncResolver.insertAsync(Contract.CONTENT_URI, peerValues, callback);
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
