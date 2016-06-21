package edu.stevens.cs522.multipane.provider;

import java.util.List;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.TextUtils;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Log;
import edu.stevens.cs522.multipane.entity.ChatMessage;
import edu.stevens.cs522.multipane.entity.Peer;
import edu.stevens.cs522.multipane.database.DbAdapter;

public class MessageProviderCloud extends ContentProvider {
	private static final int ALL_ROWS_MESSAGE = 1;
	private static final int SINGLE_ROW_MESSAGE = 2;
	private static final int ALL_ROWS_PEER = 3;
	private static final int SINGLE_ROW_PEER = 4;
	private static final int ALL_ROWS_CHATROOM = 5;
	private static final int SINGLE_ROW_CHATROOM = 6;
	
	private static final String AUTHORITY = "multipanechatapp";
	private static final String DATABASE_NAME = "MultiPaneChatApp.s3db";
	private static final String DATABASE_TABLE_MESSAGE = "messages";
	private static final String DATABASE_TABLE_MESSAGE_JOIN = "messages JOIN chatrooms ON messages.chatroom_fk =chatrooms._id ";
	private static final String DATABASE_TABLE_PEER = "peers";
	private static final String DATABASE_TABLE_CHATROOM = "chatrooms";
	public static final int DATABASE_VERSION = 20;
	public static final String TEXT = "text";
	public static final String ID = "_id";
	public static final String SENDER = "sender";
	private DatabaseHelper database;
	private Context context;
	private SQLiteDatabase db;

	
	private String charGroup = "_default";
	List<String> usersList = null;
	List<String[]> msgList = null;
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY+ "/" + DATABASE_TABLE_MESSAGE);
	public static final Uri CONTENT_URI_PEER = Uri.parse("content://"+ AUTHORITY + "/" + DATABASE_TABLE_PEER);
	public static final Uri CONTENT_URI_CHATROOM = Uri.parse("content://"+ AUTHORITY + "/" + DATABASE_TABLE_CHATROOM);
	
	public static final Uri CONTENT_URI_PEER_ITEM = Uri.parse("content://"+ AUTHORITY + "/" + DATABASE_TABLE_PEER+"#");
	public static final Uri CONTENT_URI_CHATROOM_ITEM = Uri.parse("content://"+ AUTHORITY + "/" + DATABASE_TABLE_CHATROOM+"#");
	public static final Uri CONTENT_URI_MESSAGE_ITEM = Uri.parse("content://"+ AUTHORITY + "/" + DATABASE_TABLE_MESSAGE+"#");
	public static final String CONTENT_PATH = "content://multipanechatapp/message";
	public static final String CONTENT_PATH_ITEM = "content://multipanechatapp/message#";
	public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
			+ "/message";
	public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
			+ "/message";
	public static String[] messageProjection = new String[] { ID, TEXT, SENDER,
			"date", "messageid", "sender_fk","chatroom_fk"};
	public static String[] peerProjection = new String[] { ID, "name"};
	public static String[] chatroomProjection = new String[] { ID, "name"};
	public static String[] messageJoinProjection = new String[] { 
		DATABASE_TABLE_MESSAGE+"."+ID, 
		DATABASE_TABLE_MESSAGE+"."+TEXT, 
		DATABASE_TABLE_MESSAGE+"."+SENDER,
		DATABASE_TABLE_MESSAGE+"."+"date", 
		DATABASE_TABLE_MESSAGE+"."+"messageid",
		DATABASE_TABLE_MESSAGE+"."+"sender_fk",
		DATABASE_TABLE_CHATROOM+".name"};

	// db helper###########
	private static class DatabaseHelper extends SQLiteOpenHelper {
		public DatabaseHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		public void onUpgrade(SQLiteDatabase _db, int _oldVersion,
				int _newVersion) {

			_db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_MESSAGE);
			_db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_PEER);
			_db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_CHATROOM);
			onCreate(_db);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub
			db.execSQL("PRAGMA foreign_keys=ON;");
			db.execSQL(DbAdapter.DATABASE_CREATE_PEERS);
			db.execSQL(DbAdapter.DATABASE_CREATE_CHATROOMS);
			db.execSQL(DbAdapter.DATABASE_CREATE_MESSAGES);
			db.execSQL(DbAdapter.DATABASE_CREATE_CHATROOM_INDEX);
			

		}
	}

	public MessageProviderCloud() {
		// TODO Auto-generated constructor stub
	}

	public MessageProviderCloud(Context c) {
		context = c;
		database = new DatabaseHelper(c, DATABASE_NAME, null, DATABASE_VERSION);
		db = database.getWritableDatabase();
		// TODO Auto-generated constructor stub
	}

	private static final UriMatcher uriMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);
	static {
		uriMatcher.addURI(AUTHORITY, DATABASE_TABLE_MESSAGE, ALL_ROWS_MESSAGE);
		uriMatcher.addURI(AUTHORITY, DATABASE_TABLE_MESSAGE + "/#", SINGLE_ROW_MESSAGE);
		
		uriMatcher.addURI(AUTHORITY, DATABASE_TABLE_PEER, ALL_ROWS_PEER);
		uriMatcher.addURI(AUTHORITY, DATABASE_TABLE_PEER + "/#",SINGLE_ROW_PEER);
		
		uriMatcher.addURI(AUTHORITY, DATABASE_TABLE_CHATROOM ,ALL_ROWS_CHATROOM);
		uriMatcher.addURI(AUTHORITY, DATABASE_TABLE_CHATROOM + "/#",SINGLE_ROW_CHATROOM);
		
	}

	@Override
	public boolean onCreate() {
		Context context = getContext();
		database = new DatabaseHelper(context, DATABASE_NAME, null,DATABASE_VERSION);
		db = database.getWritableDatabase();
		return false;
	}

	@Override
	public String getType(Uri arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		Cursor cursor = null;
		switch (uriMatcher.match(uri)) {
		case ALL_ROWS_MESSAGE:
			cursor = db.query(DATABASE_TABLE_MESSAGE_JOIN, null,
					selection, selectionArgs, null, null, sortOrder + " DESC");
			break;
		// query the database
		case SINGLE_ROW_MESSAGE:
			selection = ID + "=?";
			selectionArgs[0] = uri.getLastPathSegment();
			break;
			
		case ALL_ROWS_PEER:
			Log.d(selection,selectionArgs[0]);
			cursor = db.query(DATABASE_TABLE_PEER, peerProjection, selection,
					selectionArgs, null, null, null);
			break;
			
			
		case ALL_ROWS_CHATROOM:
			Log.d("All rows chatroom query","---------");
			cursor = db.query(DATABASE_TABLE_CHATROOM, chatroomProjection, selection,
					selectionArgs, null, null, null);
			break;
		case SINGLE_ROW_CHATROOM:
			Log.d("one rows chatroom query","---------"+ selectionArgs[0]);
			cursor = db.query(DATABASE_TABLE_MESSAGE_JOIN, new String[] {"messages._id", "(sender||' Says: '||text) AS 'cols'","datetime(date/1000, 'unixepoch') AS 'timestamp'" }, selection,
					selectionArgs, null, null, null);
			break;
			
		default:
			break;
		}
		// SQLiteDatabase db = database.getWritableDatabase();

		return cursor;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		int uriType = uriMatcher.match(uri);
		
		long id = 0;
		

		try {
			// sqlDB = database.getWritableDatabase();
			switch (uriType) {
			//return message uri with id 0
			case ALL_ROWS_MESSAGE:
				// sendFromMsg(values);

				 id = db.insert(DATABASE_TABLE_MESSAGE,null,values);
				return Uri.parse("content://" + AUTHORITY + "/"
						+ DATABASE_TABLE_MESSAGE + "/" + id);
				//insesrt peer and return url
			
			case ALL_ROWS_PEER:
				id = db.insert(DATABASE_TABLE_PEER, null, values);
				return Uri.parse("content://" + AUTHORITY + "/"
						+ DATABASE_TABLE_PEER + "/" + id);
				
			case ALL_ROWS_CHATROOM:
				id = db.insert(DATABASE_TABLE_CHATROOM, null, values);
				return Uri.parse("content://" + AUTHORITY + "/"
						+ DATABASE_TABLE_CHATROOM + "/" + id);
			default:
				throw new IllegalArgumentException("Unknown URI: " + uri);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		getContext().getContentResolver().notifyChange(uri, null);
		// getContext().getContentResolver().notifyChange(uri, null);
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int uriType = uriMatcher.match(uri);
		// SQLiteDatabase sqlDB = database.getWritableDatabase();
		int rowsDeleted = 0;
		switch (uriType) {
		case ALL_ROWS_MESSAGE:
			rowsDeleted = db.delete(DATABASE_TABLE_MESSAGE, selection, selectionArgs);
			break;
		case ALL_ROWS_PEER:
			rowsDeleted = db.delete(DATABASE_TABLE_PEER, selection,
					selectionArgs);
			break;
		case ALL_ROWS_CHATROOM:
			rowsDeleted = db.delete(DATABASE_TABLE_CHATROOM, selection,
					selectionArgs);
			break;
		case SINGLE_ROW_MESSAGE:
			String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				rowsDeleted = db.delete(DATABASE_TABLE_MESSAGE, ID + "=" + id, null);
			} else {
				rowsDeleted = db.delete(DATABASE_TABLE_MESSAGE, ID + "=" + id + " and "
						+ selection, selectionArgs);
			}
			break;
		default:
			break;
		}
		// context.getContentResolver().notifyChange(uri, null);
		return rowsDeleted;
	}


	@Override
	public int update(Uri uri, ContentValues values, String selection,String[] selectionArgs) {

		int uriType = uriMatcher.match(uri);
		// SQLiteDatabase sqlDB = database.getWritableDatabase();

		switch (uriType) {
		case ALL_ROWS_MESSAGE:
			return db.update(DATABASE_TABLE_MESSAGE, values, selection, selectionArgs);

		case SINGLE_ROW_MESSAGE:
			String id = uri.getLastPathSegment();
			if (TextUtils.isEmpty(selection)) {
				return db.update(DATABASE_TABLE_MESSAGE, values, ID + "=" + id, null);
			} else {
				return db.update(DATABASE_TABLE_MESSAGE, values, ID + "=" + id
						+ " and " + selection, selectionArgs);
			}
			
		case SINGLE_ROW_PEER:
			
			String peerid = uri.getLastPathSegment();
			Log.d("update peer", peerid);
			if (TextUtils.isEmpty(selection)) {
				return db.update(DATABASE_TABLE_PEER, values, ID + "=" + peerid, null);
			} else {
				return db.update(DATABASE_TABLE_PEER, values, ID + "=" + peerid
						+ " and " + selection, selectionArgs);
			}
		case SINGLE_ROW_CHATROOM:
			
			String chatroomid = uri.getLastPathSegment();
			Log.d("update chatroom", chatroomid);
			if (TextUtils.isEmpty(selection)) {
				return db.update(DATABASE_TABLE_CHATROOM, values, ID + "=" + chatroomid, null);
			} else {
				return db.update(DATABASE_TABLE_CHATROOM, values, ID + "=" + chatroomid
						+ " and " + selection, selectionArgs);
			}
		default:
			return 0;
		}
	}
}
