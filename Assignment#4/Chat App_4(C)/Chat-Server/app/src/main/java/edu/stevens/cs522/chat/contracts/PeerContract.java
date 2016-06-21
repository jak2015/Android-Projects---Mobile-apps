package edu.stevens.cs522.chat.contracts;

import java.net.InetAddress;
import java.net.UnknownHostException;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

public class PeerContract {
	
	public static final String PEER_ID = "_id";
	public static final String PEER_NAME = "name";
	public static final String PEER_ADDRESS = "address";
	public static final String PEER_ADDRESS_PORT = "port";
	
	public static final String PROVIDER_AUTHORITY = "edu.stevens.cs522.chat.oneway";
	public static final String PEER_TABLE_NAME = "Peers";
	public static final String MESSAGE_TABLE_NAME = "Messages";
	public static final Uri CONTENT_URI = CONTENT_URI(PROVIDER_AUTHORITY, PEER_TABLE_NAME);
	public static final Uri MESSAGE_CONTENT_URI = CONTENT_URI(PROVIDER_AUTHORITY, MESSAGE_TABLE_NAME);
	public static final Uri ALL_URI = CONTENT_URI(PROVIDER_AUTHORITY, "ALL");
	
	public static final String CONTENT_PATH = CONTENT_PATH(CONTENT_URI);
	public static final String CONTENT_PATH_ITEM = CONTENT_PATH(withExtendedPath(CONTENT_URI, "#"));
	public static final String MESSAGE_CONTENT_PATH = CONTENT_PATH(MESSAGE_CONTENT_URI);
	public static final String MESSAGE_CONTENT_PATH_ITEM = CONTENT_PATH(withExtendedPath(MESSAGE_CONTENT_URI, "#"));
	public static final String ALL_PATH = CONTENT_PATH(ALL_URI);
	
	public static final int ALLROWS = 1;
	public static final int SINGLE_ROW = 2;
	public static final int MESSAGE_ALLROWS = 3;
	public static final int MESSAGE_SINGLE_ROW = 4;
	public static final int ALL = 5;
	
	public static Uri withExtendedPath(Uri uri, String... path) {
		Uri.Builder builder = uri.buildUpon();
		for (String p : path) {
			builder.appendPath(p);
		}
		return builder.build();
	}
	
	public static Uri CONTENT_URI (String authority, String path) {
		return new Uri.Builder().scheme("content")
								.authority(authority)
								.path(path)
								.build();
	}
	
	public static String CONTENT_PATH (Uri uri) {
		return uri.getPath().substring(1);
	}
	
	public static String contentType (String content) {
		return "vnd.android.cursor/vnd." +
				PROVIDER_AUTHORITY + "." + content + "s";
	}
	
	public static String contentItemType (String content) {
		return "vnd.android.cursor.item/vnd." +
				PROVIDER_AUTHORITY + "." + content;
	}
	
	public static Uri CONTENT_URI (Long id) {
		return withExtendedPath(CONTENT_URI, String.valueOf(id));
	}
	
	public static Long getId(Uri uri) {
		return Long.parseLong(uri.getLastPathSegment());
	}
	
	public static String getTableName(Uri uri) {
		return uri.getLastPathSegment();
	}
	
	public static int getPeerId(Cursor cursor) {
		return cursor.getInt(cursor.getColumnIndexOrThrow(PEER_ID));
	}
	public static void putPeerId(ContentValues values, long peer_id) {
		values.put(PEER_ID, peer_id);
	}
	
	public static String getPeerName(Cursor cursor) {
		return cursor.getString(cursor.getColumnIndexOrThrow(PEER_NAME));
	}
	public static void putPeerName(ContentValues values, String peer_name) {
		values.put(PEER_NAME, peer_name);
	}

	public static InetAddress getPeerAddress(Cursor cursor) {
		byte[] blob = cursor.getBlob(cursor.getColumnIndexOrThrow(PEER_ADDRESS));
		String address = new String(blob);
		InetAddress inetAddress = null;
		try {
			inetAddress =  InetAddress.getByName(address);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return inetAddress;
	}
	public static void putPeerAddress(ContentValues values, InetAddress peer_address) {
		values.put(PEER_ADDRESS, peer_address.getHostAddress().getBytes());
	}
	public static int getPeerAddressPort(Cursor cursor) {
		return cursor.getInt(cursor.getColumnIndexOrThrow(PEER_ADDRESS_PORT));
	}
	public static void putPeerAddressPort(ContentValues values, int port) {
		values.put(PEER_ADDRESS_PORT, port);
	}
}
