package edu.stevens.cs522.chat.databases;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import edu.stevens.cs522.chat.contracts.MessageContract;
import edu.stevens.cs522.chat.contracts.PeerContract;
import edu.stevens.cs522.chat.entities.Peer;

public class ChatDbAdapter {
	
	private DatabaseHelper dbHelper;	
	private SQLiteDatabase db;	
	private final Context chatContext;

	private static final String DATABASE_NAME = "chat.db";
	private static final String MESSAGE_TABLE = "Messages";
	private static final String PEER_TABLE = "Peers";
	private static final String MESSAGES_PEER_INDEX = "MessagePeerIndex";
	private static final int DATABASE_VERSION = 8;

	private static final String SELECT_MSG_FROM_PEER = "SELECT "
			+ MessageContract.MESSAGE_ID + ","
			+ MessageContract.MESSAGE_SENDER + ","
			+ MessageContract.MESSAGE_MESSAGE
			+ " FROM " + MESSAGE_TABLE + " WHERE "
			+ MessageContract.MESSAGE_PEER_FK + "=?";

	private static final String CREATE_PEER_TABLE = "CREATE TABLE if not exists "
			+ PEER_TABLE + " ( " 
			+ PeerContract.PEER_ID + " INTEGER PRIMARY KEY, " 
			+ PeerContract.PEER_NAME + " TEXT NOT NULL, " 
			+ PeerContract.PEER_ADDRESS + " BLOB NOT NULL, "
			+ PeerContract.PEER_ADDRESS_PORT + " INTEGER NOT NULL)";
	
	private static final String CREATE_MESSAGE_TABLE = "CREATE TABLE if not exists "
			+ MESSAGE_TABLE + " ( " 
			+ MessageContract.MESSAGE_ID + " INTEGER PRIMARY KEY, " 
			+ MessageContract.MESSAGE_MESSAGE + " TEXT NOT NULL, " 
			+ MessageContract.MESSAGE_SENDER + " TEXT NOT NULL, " 
			+ MessageContract.MESSAGE_PEER_FK + " INTEGER NOT NULL, " 
			+ "FOREIGN KEY (" + MessageContract.MESSAGE_PEER_FK + ") REFERENCES " 
			+ PEER_TABLE + "(" + PeerContract.PEER_ID + ") ON DELETE CASCADE)";
	
	private static final String CREATE_INDEX = "CREATE INDEX " + MESSAGES_PEER_INDEX 
			+ " ON " + MESSAGE_TABLE + "(" + MessageContract.MESSAGE_PEER_FK + ")";
			
	private static final String SELECT_IF_PEER_EXIST = "SELECT " + PeerContract.PEER_ID 
			+ " FROM " + PEER_TABLE+ " where " + PeerContract.PEER_NAME + " = ?";
	
	private static final String SELECT_ALL = "SELECT " 
			+ PEER_TABLE + "." + PeerContract.PEER_ID + ", " 
			+ PEER_TABLE + "." + PeerContract.PEER_NAME + ", " 
			+ PEER_TABLE + "." + PeerContract.PEER_ADDRESS + ", " 
			+ MESSAGE_TABLE + "." + MessageContract.MESSAGE_MESSAGE
			+ " FROM " 
			+ PEER_TABLE + "," + MESSAGE_TABLE 
			+ " WHERE " + PEER_TABLE + "." + PeerContract.PEER_ID + "="
			+ MESSAGE_TABLE + "." + MessageContract.MESSAGE_PEER_FK;

	public ChatDbAdapter(Context ctx) 
	{
		chatContext = ctx;
		dbHelper = new DatabaseHelper(chatContext);
    }
	
	private static class DatabaseHelper extends SQLiteOpenHelper 
	{

        public DatabaseHelper(Context context) {
        	super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) 
        {
        	db.execSQL("DROP TABLE IF EXISTS " + PEER_TABLE );
			db.execSQL("DROP TABLE IF EXISTS " + MESSAGE_TABLE );
        	db.execSQL(CREATE_PEER_TABLE);
        	db.execSQL(CREATE_MESSAGE_TABLE);
        	db.execSQL(CREATE_INDEX);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
        {
            Log.w("TaskDBAdapter", "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + PEER_TABLE);
			db.execSQL("DROP TABLE IF EXISTS " + MESSAGE_TABLE);
            onCreate(db);
        }
    }
	
	public ChatDbAdapter open() throws SQLException {
        db = dbHelper.getWritableDatabase();
		db.execSQL("PRAGMA foreign_keys=ON;");
        return this;
	}

	public Cursor fetchAllPeers() {
		return db.query(PEER_TABLE, new String[]{PeerContract.PEER_ID,PeerContract.PEER_NAME,PeerContract.PEER_ADDRESS,PeerContract.PEER_ADDRESS_PORT}, null, null, null, null, null);
	}

	public Cursor fetchAllMessagesFromPeer(Peer peer) {
		return db.rawQuery(SELECT_MSG_FROM_PEER, new String[]{String.valueOf(peer.getId())});
	}

	public Cursor fetchAllMessagesFromPeerId(long peerId) {
		return db.rawQuery(SELECT_MSG_FROM_PEER, new String[]{String.valueOf(peerId)});
	}

	public void persist(Peer peer, String message) throws SQLException {
		
		// Check if the peer is already exist in database	
		long peerId = getPeerId(peer.getName());
		if (-1 == peerId) {			
			// if not, insert new peer	
			ContentValues peerValues = new ContentValues();
			PeerContract.putPeerName(peerValues, peer.getName());
			PeerContract.putPeerAddress(peerValues, peer.getAddress());
			PeerContract.putPeerAddressPort(peerValues, peer.getPort());
			peerId = db.insertOrThrow(PEER_TABLE, null, peerValues);
		}
				
		// insert message
		ContentValues messageValues = new ContentValues();
		MessageContract.putMessageMessage(messageValues, message);
		MessageContract.putMessagePeerFK(messageValues, peerId);
		MessageContract.putMessageSender(messageValues, peer.getName());
		db.insert(MESSAGE_TABLE, null, messageValues);
		
	}
	
	public long getPeerId(String name) {
		Cursor cursor = db.rawQuery(SELECT_IF_PEER_EXIST, new String[]{name});
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			return cursor.getLong(cursor.getColumnIndexOrThrow(PeerContract.PEER_ID));
		} else {
			return -1;
		}
	}
	
	public Cursor fetchAll() {
		return db.rawQuery(SELECT_ALL, null);
	}
	
	public boolean deleteAll() {
		db.execSQL(" DELETE FROM " + PEER_TABLE);
		db.execSQL(" DELETE FROM " + MESSAGE_TABLE);
		return true;
	}
	public void close() {
		db.close();
	}
	
	public String byteToString(byte[] _bytes) {
		String string = "";
	    for(int i = 0; i < _bytes.length; i++){
	        string += (char)_bytes[i];
	    }
	    return string; 
	}
	
	public Cursor getAddress() {
		return db.rawQuery("select address from Peers", null);
	}
	
	public long createItem(ContentValues values) throws SQLException {
		long peerId = getPeerId(values.getAsString(PeerContract.PEER_NAME));
		if (-1 == peerId) {			
			// if not, insert new peer	
			ContentValues peerValues = new ContentValues();
			PeerContract.putPeerName(peerValues, values.getAsString(PeerContract.PEER_NAME));			
			peerValues.put(PeerContract.PEER_ADDRESS, values.getAsByteArray(PeerContract.PEER_ADDRESS));
			PeerContract.putPeerAddressPort(peerValues, values.getAsInteger(PeerContract.PEER_ADDRESS_PORT));
			peerId = db.insertOrThrow(PEER_TABLE, null, peerValues);
		}
	
		// insert message
		ContentValues messageValues = new ContentValues();
		MessageContract.putMessageMessage(messageValues, values.getAsString(MessageContract.MESSAGE_MESSAGE));
		MessageContract.putMessagePeerFK(messageValues, peerId);
		MessageContract.putMessageSender(messageValues, values.getAsString(PeerContract.PEER_NAME));
		db.insert(MESSAGE_TABLE, null, messageValues);
		
		return peerId;
	}
	
	public long createMessageItem(ContentValues values) {
		// TODO Auto-generated method stub
		return db.insert(MESSAGE_TABLE, null, values);
	}
	
	public Cursor fetchPeerCursor(long rowId) {

		return null;
	}

	public SQLiteDatabase getDb() {
		return db;
	}

	public void setDb(SQLiteDatabase db) {
		this.db = db;
	}
}
