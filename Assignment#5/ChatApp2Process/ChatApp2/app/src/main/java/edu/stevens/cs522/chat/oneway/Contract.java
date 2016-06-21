package edu.stevens.cs522.chat.oneway;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
public class Contract {
	public static final int ALL_ROWS = 1; 
	public static final int SINGLE_ROW = 2;
	public static final int ALL_ROWS_PEER = 3;
	public static final int SINGLE_ROW_PEER = 4;

	public static final String AUTHORITY = "chat";
	public static final String DATABASE_NAME = "Chat.db";
	public static final String DATABASE_TABLE= "message";
	public static final String DATABASE_TABLE_PEER= "peer";

	public static final int DATABASE_VERSION = 5;
	

	public static final Uri CONTENT_URI= Uri.parse("content://" + AUTHORITY
	      + "/" + DATABASE_TABLE);
	public static final Uri CONTENT_URI_PEER= Uri.parse("content://" + AUTHORITY
		      + "/" + DATABASE_TABLE_PEER);
	public static final String CONTENT_PATH = "content://chat/message";
	public static final String CONTENT_PATH_ITEM =  "content://chat/message#";
	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
	      + "/message";
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
	      + "/message";
	  
	public static final String TEXT = "text";
	public static final String ID = "_id";
	public static final String SENDER = "sender";
	public static final String NAME = "name";
	public static final String ADDRESS = "address";
	public static final String PORT = "port";
	public static String getText(Cursor cursor) {
		return cursor.getString(cursor.getColumnIndexOrThrow(TEXT));
	}
	public static void putText(ContentValues values, String id) {
		values.put(TEXT, id);
	}
	public static int getId(Cursor cursor) {
		return cursor.getInt(cursor.getColumnIndexOrThrow(ID));
	}
	public static void putId(ContentValues values, long id2) { values.putNull(ID);
	}
	public static String getSender(Cursor cursor) {
		return cursor.getString(cursor.getColumnIndexOrThrow(SENDER));
	}
	public static void putSender(ContentValues values, String title) { values.put(SENDER, title);
	}
	public static String getName(Cursor cursor) {
		return cursor.getString(cursor.getColumnIndexOrThrow(NAME));
	}
	public static void putName(ContentValues values, String title) { values.put(NAME, title);
	}
	public static String getAddress(Cursor cursor) {
		return cursor.getString(cursor.getColumnIndexOrThrow(ADDRESS));
	}
	public static void putAddress(ContentValues values, String title) { values.put(ADDRESS, title);
	}
	public static String getPort(Cursor cursor) {
		return cursor.getString(cursor.getColumnIndexOrThrow(PORT));
	}
	public static void putPort(ContentValues values, int title) { values.put(PORT, title);
	}
}
