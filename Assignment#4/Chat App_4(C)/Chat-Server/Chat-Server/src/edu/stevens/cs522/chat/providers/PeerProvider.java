package edu.stevens.cs522.chat.providers;

import edu.stevens.cs522.chat.contracts.PeerContract;
import edu.stevens.cs522.chat.databases.ChatDbAdapter;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;

public class PeerProvider extends ContentProvider {
	
	
	// Creates a UriMatcher object.
    private static final UriMatcher uriMatcher;
    
    private static ChatDbAdapter cdbAdapter;
    
    static {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(PeerContract.PROVIDER_AUTHORITY, PeerContract.ALL_PATH, PeerContract.ALL);
		uriMatcher.addURI(PeerContract.PROVIDER_AUTHORITY, PeerContract.CONTENT_PATH, PeerContract.ALLROWS);
		uriMatcher.addURI(PeerContract.PROVIDER_AUTHORITY, PeerContract.CONTENT_PATH_ITEM, PeerContract.SINGLE_ROW);
		uriMatcher.addURI(PeerContract.PROVIDER_AUTHORITY, PeerContract.MESSAGE_CONTENT_PATH, PeerContract.MESSAGE_ALLROWS);
		uriMatcher.addURI(PeerContract.PROVIDER_AUTHORITY, PeerContract.MESSAGE_CONTENT_PATH_ITEM, PeerContract.MESSAGE_SINGLE_ROW);
	}
    
	@Override
	public int delete(Uri uri, String where, String[] whereArgs) {
		// TODO Auto-generated method stub
		switch (uriMatcher.match(uri)) {
		case PeerContract.ALLROWS:
//			cdbAdapter.deleteAll();
			break;
		case PeerContract.SINGLE_ROW:
//			number = cdbAdapter.delete(PeerContract.getId(uri)) ? 1 : 0;
			break;
		default: throw new IllegalArgumentException ("Unsupported URI:" + uri);
		}
//		ContentResolver contentResolver = getContext().getContentResolver();
//		contentResolver.notifyChange(uri, null);
		return 0;
	}	
	
	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		switch (uriMatcher.match(uri)) {
		case PeerContract.ALLROWS:
			return PeerContract.contentType(PeerContract.PEER_TABLE_NAME);
		case PeerContract.SINGLE_ROW:
			return PeerContract.contentItemType(PeerContract.PEER_TABLE_NAME);
		case PeerContract.MESSAGE_ALLROWS:
			return PeerContract.contentType(PeerContract.MESSAGE_TABLE_NAME);
		case PeerContract.MESSAGE_SINGLE_ROW:
			return PeerContract.contentItemType(PeerContract.MESSAGE_TABLE_NAME);
		case PeerContract.ALL:
			return PeerContract.contentType(PeerContract.PEER_TABLE_NAME);
		default: throw new IllegalArgumentException ("Unsupported URI:" + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
	
		switch (uriMatcher.match(uri)) {
		case PeerContract.ALL:
			
		case PeerContract.ALLROWS:
			long row = cdbAdapter.createItem(values);
			if (row > 0) {
				Uri instanceUri = ContentUris.withAppendedId(uri, row);
				ContentResolver cr = getContext().getContentResolver();
				cr.notifyChange(instanceUri, null);
				return instanceUri;
			} else {
				throw new SQLException("Insertion failed");
			}
		case PeerContract.SINGLE_ROW:
			break;
		case PeerContract.MESSAGE_ALLROWS:
			long authorRow = cdbAdapter.createMessageItem(values);
			if (authorRow > 0) {
				Uri instanceUri = ContentUris.withAppendedId(uri, authorRow);
				ContentResolver cr = getContext().getContentResolver();
				cr.notifyChange(instanceUri, null);
				return instanceUri;
			} else {
				throw new SQLException("Insertion failed");
			}
		case PeerContract.MESSAGE_SINGLE_ROW:
			break;
		default: throw new IllegalArgumentException ("Unsupported URI:" + uri);
		}
		
		return null;
	}

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		cdbAdapter = new ChatDbAdapter(getContext());
		cdbAdapter.open();
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sort) {
		// TODO Auto-generated method stub
		Cursor c ;
		switch (uriMatcher.match(uri)) {
		case PeerContract.ALL:
			c = cdbAdapter.fetchAll();
			c.setNotificationUri(getContext().getContentResolver(), uri);
			return c;
		case PeerContract.ALLROWS:
			// query the database
			c = cdbAdapter.getDb().query(PeerContract.PEER_TABLE_NAME, projection, selection, selectionArgs, null, null, sort);		
			c.setNotificationUri(getContext().getContentResolver(), uri);
			return c;
		case PeerContract.SINGLE_ROW:			
			c = cdbAdapter.getDb().query(PeerContract.PEER_TABLE_NAME, projection, selection, selectionArgs, null, null, sort);
			c.setNotificationUri(getContext().getContentResolver(), uri);
			return c;
			// query the database
		case PeerContract.MESSAGE_ALLROWS:
			c = cdbAdapter.getDb().query(PeerContract.getTableName(uri), projection, selection, selectionArgs, null, null, sort);
			c.setNotificationUri(getContext().getContentResolver(), uri);
			return c;
		case PeerContract.MESSAGE_SINGLE_ROW:
			c = cdbAdapter.getDb().query(PeerContract.getTableName(uri), projection, selection, selectionArgs, null, null, sort);
			c.setNotificationUri(getContext().getContentResolver(), uri);
			return c;
		}
		return null;
	}

	@Override
	public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
		// TODO Auto-generated method stub
		switch (uriMatcher.match(uri)) {
		case PeerContract.ALLROWS:
			break;
		case PeerContract.SINGLE_ROW:
			break;
		default: throw new IllegalArgumentException ("Unsupported URI:" + uri);
		}
		return 0;
	}

}
