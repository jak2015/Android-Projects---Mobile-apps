package database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.sql.SQLException;
import contracts.PeerContract;
import entities.Message;
import entities.Peer;

public class DbAdapter
{
    private static final String TAG = "DbAdapter";
    private static final String DATABASE_NAME = "ChatDb";
    private static final int DATABASE_VERSION = 1;
    private static final String Peer_Table= "Peers";
    private static final String Message_Table = "Messages";
    private final Context context;

    private DatabaseHelper DbHelper;
    private SQLiteDatabase PeerDb;
    private static String for_key = "PRAGMA foreign_keys=ON;";

    private static final String CREATE_PEERS_TABLE ="create table "+Peer_Table+" (_id INTEGER PRIMARY KEY," +
                                                    "Name TEXT NOT NULL," +
                                                    "Address TEXT NOT NULL," +
                                                    "Port INTEGER)";
    private static final String CREATE_MESSAGES_TABLE ="create table "+Message_Table+" " +
                                                        "(_id INTEGER PRIMARY KEY," +
                                                        "Message TEXT NOT NULL," +
                                                        "Sender TEXT NOT NULL," +
                                                        "Peer_fk INTEGER," +
                                                        "FOREIGN KEY (Peer_fk) REFERENCES Peers(_id))";
    private static final String CREATE_INDEX ="CREATE INDEX MessagesIndex ON Messages(Peer_fk);";

    public static class DatabaseHelper extends SQLiteOpenHelper
    {
        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            Log.i(TAG, "Creating DataBase: " + CREATE_PEERS_TABLE);
            db.execSQL(CREATE_MESSAGES_TABLE);
            db.execSQL(CREATE_PEERS_TABLE);
            db.execSQL(for_key);
            db.execSQL(CREATE_INDEX);
            Log.i(TAG, "Creating DataBase: " + CREATE_MESSAGES_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion);
        }
    }

    public DbAdapter(Context ctx) {
        this.context = ctx;
    }

    public DbAdapter open() {
        DbHelper = new DatabaseHelper(context);
        PeerDb = DbHelper.getWritableDatabase();
        return this;
    }

    public long createPeer(Peer p) {
        ContentValues initialValues = new ContentValues();
        p.writeToProvider(initialValues);
        return PeerDb.insert(Peer_Table, null, initialValues);
    }
    public long createMessage(Message m)
    {
        ContentValues initialValues = new ContentValues();
        m.writeToProvider(initialValues);
        return PeerDb.insert(Message_Table,null,initialValues);
    }
    public boolean deletePeer(long rowId) {
        return PeerDb.delete(Peer_Table, "_id" + "=" + rowId, null) > 0;
    }
    public Cursor fetchAllPeers()
    {
        Cursor cursor = PeerDb.query(Peer_Table,new String[]{"_id","name"},null,null,null,null,null);
        return cursor;
    }
    public void close() {
        DbHelper.close();
    }

    public Cursor fetchText(long id)throws android.database.SQLException
    {
        String TEXT = "SELECT *"+
                " FROM Messages LEFT JOIN Peers"+
                " ON Peers._id =Messages.peer_fk WHERE peer_fk="+id;
        try{
            Cursor c=PeerDb.rawQuery(TEXT,null);
            return c;
        }
        catch(Exception e)
        {
            e.printStackTrace();}
        return null;
    }

    public int fetchId(String sender) throws SQLException {
        Cursor mCursor = null;
        String where = "Name="+"'"+sender+"'";
        try {
            mCursor =PeerDb.query(Peer_Table,new String[]{"_id"},where,null, null, null, null);
        }catch (Exception e){
            e.printStackTrace();
        }
        mCursor.moveToFirst();
        int ID = PeerContract.getId(mCursor);
        return ID;
    }

    public boolean updatePeer(String name,String address,int port)
    {
        ContentValues args = new ContentValues();
        String where = "Name="+"'"+name+"'";
        args.put("Address", address);
        args.put("Port", port);
        return PeerDb.update(Peer_Table, args, where, null) > 0;
    }

    public boolean isExits(String sender)
    {
        boolean signal = false;
        String where = "Name="+"'"+sender+"'";
        Cursor cursor = PeerDb.query(Peer_Table,new String[]{"Name"},where,null, null, null, null);
        if(cursor.getCount()>0)
        {
            signal = true;
        }
        else {
            signal = false;
        }
        return signal;
    }
}

