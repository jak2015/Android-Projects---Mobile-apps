package edu.stevens.cs522.databases;

import edu.stevens.cs522.contracts.Contract;
import edu.stevens.cs522.entities.ChatMessage;
import edu.stevens.cs522.entities.Peer;
import edu.stevens.cs522.providers.CloudProvider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

public class DbAdapter {
	private static final String DATABASE_NAME = "ChatCloud.s3db";
	private static final String DATABASE_MESSAGE_TABLE = "message";
	private static final String DATABASE_PEER_TABLE = "peer";

	private static final String DATABASE_MESSAGE_REGISTRATION_TABLE="register";

	public static final String TEXT = "text";
	public static final String ID = "_id";
	public static final String SENDER = "sender";
	public static final String DATE = "date";
	public static final String MESSAGEID = "messageid";
	public static final String SENDERID = "senderid";
	public static final String SENDERNAME = "name";

	public static final String LATITUDE = "latitude";
	public static final String LONGITUDE = "longitude";
	public static final String REGISTERID ="regid";


	public static final String DATABASE_CREATE = "create table "
			+ DATABASE_MESSAGE_TABLE + " (" + ID + " integer primary key, "
			+ TEXT + " text not null, " + SENDER + " text not null," + DATE
			+ " integer not null," + MESSAGEID + " integer not null,"
			+ SENDERID + " integer not null, " +LATITUDE+" text, "+LONGITUDE+" text" + ")";
	public static final String DATABASE_CREATE_PEER = "create table " +DATABASE_PEER_TABLE +" ("
			+ ID
			+ " integer primary key, "
			+ SENDERNAME
			+ " text not null )";

	public static final String DATABASE_CREATE_REGISTER = "create table "+DATABASE_MESSAGE_REGISTRATION_TABLE+" ("+ID+" integer primary key, "
			+ REGISTERID +" integer not null, "+MESSAGEID+" integer not null )";

	// private static final String DATABASE_CREATE_FK =
	// "CREATE INDEX PeerMessageIndex ON peer(peer_fk);";
	public SQLiteDatabase db;
	// Context of the application using the database.
	private Context context;
	private DatabaseHelper dbHelper;

	private static class DatabaseHelper extends SQLiteOpenHelper {
		public DatabaseHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		// Database version mismatch: version on disk
		// needs to be upgraded to the current version.
		public void onUpgrade(SQLiteDatabase _db, int _oldVersion,
				int _newVersion) {
			// Log the version upgrade.
			Log.w("TaskDBAdapter", "Upgrading from version " + _oldVersion
					+ " to " + _newVersion);
			// Upgrade: drop the old table and create a new one.

			_db.execSQL("DROP TABLE IF EXISTS " + DATABASE_MESSAGE_TABLE);
			_db.execSQL("DROP TABLE IF EXISTS peer");
			_db.execSQL("DROP TABLE IF EXIST "+DATABASE_MESSAGE_REGISTRATION_TABLE);

			onCreate(_db);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub
			db.execSQL(DbAdapter.DATABASE_CREATE);
			db.execSQL(DbAdapter.DATABASE_CREATE_PEER);
			db.execSQL(DbAdapter.DATABASE_CREATE_REGISTER);

			// db.execSQL(DbAdapter.DATABASE_CREATE_FK);
			// db.execSQL("PRAGMA foreign_keys=ON;");

		}
	}

	public DbAdapter(Context ctx) {
		this.context = ctx;

	}

	public DbAdapter open() throws SQLException {
		dbHelper = new DatabaseHelper(context, DATABASE_NAME, null,
				CloudProvider.DATABASE_VERSION);
		db = dbHelper.getWritableDatabase();
		return this;
	}

	public Cursor getAllEntries() {
		return db
				.query(DATABASE_MESSAGE_TABLE,
						new String[] { ID, TEXT, SENDER }, null, null, null,
						null, null);
	}

	public boolean deleteAll() {
		return db.delete(DATABASE_MESSAGE_TABLE, null, null) > 0;
	}

	public boolean addMessage(ChatMessage chatMessage) {
		ContentValues contentValues = new ContentValues();
		chatMessage.writeToProvider(contentValues);
		return db.insert(DATABASE_MESSAGE_TABLE, null, contentValues) > 0;

	}

	public boolean addPeer(Peer peer) {
		ContentValues contentValues = new ContentValues();
		peer.writeToProvider(contentValues);
		db.delete(DATABASE_PEER_TABLE, "name ='" + peer.name + "' and "
				+ "peer._id ='" + peer.id + "'", null);
		return db.insert(DATABASE_PEER_TABLE, null, contentValues) > 0;

	}
	
	public boolean deletebytitle(String t, String a) throws SQLException {
		// boolean re=db.delete(DATABASE_TABLE, TITLE + "='" + t +"' and "+
		// AUTHOR+"='"+a+"'", null)>0;
		return true;
	}

	public boolean selectbytitle(String t, String a) throws SQLException {
		// boolean re=db.delete(DATABASE_TABLE, TITLE + "='" + t +"' and "+
		// AUTHOR+"='"+a+"'", null)>0;
		return true;
	}

	public boolean deletePeer(Peer peer) {
		ContentValues contentValues = new ContentValues();
		peer.writeToProvider(contentValues);
		return db.delete(DATABASE_PEER_TABLE, "name ='" + peer.name + "' and "
				+ "peer._id ='" + peer.id + "'", null) > 0;

	}

	public Cursor getAllPeer() {
		return db.query(DATABASE_PEER_TABLE, new String[] { ID, "name" }, null, null, null, null, null);
	}


	public static final String TAG = DbAdapter.class.getCanonicalName();

	public Cursor getMessgeByPeer(String name) {
		Log.i(TAG, "getMessageByPeer");
		String whereClause = "sender = ?";
		String[] whereArgs = new String[] { name };
		return db.query(DATABASE_MESSAGE_TABLE,
				new String[] { ID, TEXT, SENDER }, whereClause, whereArgs,
				null, null, null);
	}
	public String getNameById(long id){
		Log.i(TAG, "getNameById(long) id = "+id);
		String whereClause = "peer._id = ?";
		String[] whereArgs = new String[] { String.valueOf(id) };
		Cursor c =  db.query(DATABASE_PEER_TABLE,
				new String[] { ID, SENDERNAME }, whereClause, whereArgs,
				null, null, null);
		String name = null;
		if(c.moveToFirst()){
			Log.i(TAG, "getNameById(long) = movetoFirst");
			name = c.getString(c.getColumnIndex(Contract.NAME));
			Log.i(TAG, "getNameById(long), name = "+name);
		}
		 return name;
	}
}
