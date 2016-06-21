package edu.stevens.cs522.contracts;

import edu.stevens.cs522.constants.constant;
import edu.stevens.cs522.databases.DbAdapter;
import android.content.ContentValues;
import android.database.Cursor;
public class Contract {
	public static final String TEXT = DbAdapter.TEXT;
	public static final String ID = DbAdapter.ID;
	public static final String SENDER = DbAdapter.SENDER;
	public static final String NAME = DbAdapter.SENDERNAME;
	public static final String ADDRESS = DbAdapter.ADDRESS;
	public static final String PORT = constant.PORT;

    public static final String LATITUDE = DbAdapter.LATITUDE;
    public static final String LONGITUDE = DbAdapter.LONGITUDE;


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
	
	public static int getDate(Cursor cursor) {
		return cursor.getInt(cursor.getColumnIndexOrThrow(DbAdapter.DATE));
	}
	public static void putDate(ContentValues values, long id2) { values.put(DbAdapter.DATE,id2);
	}
	
	public static int getMessageId(Cursor cursor) {
		return cursor.getInt(cursor.getColumnIndexOrThrow(DbAdapter.MESSAGEID));
	}
	public static void putMessageID(ContentValues values, long id2) { values.put(DbAdapter.MESSAGEID,id2);
	}
	
	public static int getSenderId(Cursor cursor) {
		return cursor.getInt(cursor.getColumnIndexOrThrow(DbAdapter.SENDERID));
	}
	public static void putSenderId(ContentValues values, long id2) { values.put(DbAdapter.SENDERID,id2);
	values.put(DbAdapter.SENDERID,id2);
	}


    public static void putLatitude(ContentValues values, String title) { values.put(LATITUDE, title);
    }
    public static void putLongitude(ContentValues values, String title) { values.put(LONGITUDE, title);
    }
    public static String getLatitude(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndexOrThrow(DbAdapter.LATITUDE));
    }
    public static String getLongitude(Cursor cursor) {
        return cursor.getString(cursor.getColumnIndexOrThrow(DbAdapter.LONGITUDE));
    }
}
