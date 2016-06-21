package edu.stevens.cs522.multipane.database;

import edu.stevens.cs522.multipane.entity.ChatMessage;
import edu.stevens.cs522.multipane.entity.Peer;
import edu.stevens.cs522.multipane.provider.MessageProviderCloud;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

public class DbAdapter {
	private static final String DATABASE_NAME = "MultiPaneChatApp.s3db";
	private static final String DATABASE_CHATROOM_TABLE = "chatrooms";
	private static final String DATABASE_MESSAGE_TABLE = "messages";
	private static final String DATABASE_PEER_TABLE = "peers";
	public static final String TEXT = "text";
	public static final String CHATROOMNAME = "name";
	public static final String ID = "_id";
	public static final String SENDER = "sender";
	public static final String CHATROOM_FK = "chatroom_fk";
	public static final String DATE = "date";
	public static final String MESSAGEID = "messageid";
	public static final String SENDERID_FK = "sender_fk";
	public static final String SENDERNAME = "name";
	
	public static final String DATABASE_CREATE_CHATROOMS = "CREATE TABLE " 
			+DATABASE_CHATROOM_TABLE + " (" + ID + " INTEGER PRIMARY KEY,"
			+ CHATROOMNAME + " TEXT NOT NULL);";
			
			
			
	public static final String DATABASE_CREATE_MESSAGES = "CREATE TABLE "
			+ DATABASE_MESSAGE_TABLE + " (" + ID + " INTEGER PRIMARY KEY, "
			+ TEXT + " TEXT NOT NULL, " 
			+ SENDER + " TEXT NOT NULL," 
			+ DATE	+ " TEXT NOT NULL," 
			+ MESSAGEID + " INTEGER NOT NULL,"
			+ SENDERID_FK + " INTEGER NOT NULL,"
			+ CHATROOM_FK + " INTEGER NOT NULL," 
			+ "FOREIGN KEY (" + SENDERID_FK + ") REFERENCES " + DATABASE_PEER_TABLE +"("+ ID +") ON DELETE CASCADE,"
			+ "FOREIGN KEY (" + CHATROOM_FK + ") REFERENCES " + DATABASE_CHATROOM_TABLE +"("+ ID +") "
			+ ");" ;
	
	public static final String DATABASE_CREATE_PEERS = "CREATE TABLE "
			+ DATABASE_PEER_TABLE + " (" + ID + " INTEGER PRIMARY KEY, "
			+ SENDERNAME + " TEXT NOT NULL );";
	
	public static final String DATABASE_CREATE_MESSAGE_INDEX =
	 "CREATE INDEX MessageSenderIndex ON "+ DATABASE_MESSAGE_TABLE + "("+SENDERID_FK+");";
	
	public static final String DATABASE_CREATE_CHATROOM_INDEX =
			 "CREATE INDEX MessageChatroomIndex ON "+ DATABASE_MESSAGE_TABLE + "("+CHATROOM_FK+");";
	
	public SQLiteDatabase db;
	// Context of the application using the database.
	private Context context;
	private DatabaseHelper dbHelper;

	private static class DatabaseHelper extends SQLiteOpenHelper {
		public DatabaseHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		public void onUpgrade(SQLiteDatabase _db, int _oldVersion,
				int _newVersion) {
			// Log the version upgrade.
			Log.w("TaskDBAdapter", "Upgrading from version " + _oldVersion
					+ " to " + _newVersion);
			// Upgrade: drop the old table and create a new one.

			_db.execSQL("DROP TABLE IF EXISTS " + DATABASE_MESSAGE_TABLE);
			_db.execSQL("DROP TABLE IF EXISTS " + DATABASE_PEER_TABLE);
			onCreate(_db);
		}


		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub
			
			db.execSQL(DbAdapter.DATABASE_CREATE_PEERS);
			db.execSQL(DbAdapter.DATABASE_CREATE_CHATROOMS);
			db.execSQL(DbAdapter.DATABASE_CREATE_MESSAGES);
			db.execSQL(DbAdapter.DATABASE_CREATE_CHATROOM_INDEX);
	
		}
	
		
		@Override
		public void onOpen(SQLiteDatabase db){
			super.onOpen(db);
			if(!db.isReadOnly()){
				db.setForeignKeyConstraintsEnabled(true);
			}
		}
	}
	

	public DbAdapter(Context ctx) {
		this.context = ctx;

	}

	public DbAdapter open() throws SQLException {
		dbHelper = new DatabaseHelper(context, DATABASE_NAME, null,
				MessageProviderCloud.DATABASE_VERSION);
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
				+ DATABASE_PEER_TABLE+"._id ='" + peer.id + "'", null);
		return db.insert(DATABASE_PEER_TABLE, null, contentValues) > 0;

	}
	public boolean deletePeer(Peer peer) {
		ContentValues contentValues = new ContentValues();
		peer.writeToProvider(contentValues);
		return db.delete(DATABASE_PEER_TABLE, "name ='" + peer.name + "' and "
				+ DATABASE_PEER_TABLE+"._id ='" + peer.id + "'", null) > 0;

	}

	public Cursor getAllPeer() {
		return db.query(DATABASE_PEER_TABLE, new String[] { ID, "name" }, null,
				null, null, null, null);
	}

	public Cursor getMessgeByPeer(String name) {
		String whereClause = "sender = ?";
		String[] whereArgs = new String[] { name };
		return db.query(DATABASE_MESSAGE_TABLE,
				new String[] { ID, TEXT, SENDER }, whereClause, whereArgs,
				null, null, null);
	}

	public String getNameById(long id) {
		String whereClause = DATABASE_PEER_TABLE+"._id = ?";
		String[] whereArgs = new String[] { String.valueOf(id) };
		Cursor c = db.query(DATABASE_PEER_TABLE, new String[] { ID, "name" },
				whereClause, whereArgs, null, null, null);
		String name = null;
		if (c.moveToFirst()) {
			name = c.getString(c.getColumnIndex("name"));
		}
		return name;
	}
}
