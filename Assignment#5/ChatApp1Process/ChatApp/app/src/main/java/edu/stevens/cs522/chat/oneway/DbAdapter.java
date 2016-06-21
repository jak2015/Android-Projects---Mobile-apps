package edu.stevens.cs522.chat.oneway;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.util.Log;

public class DbAdapter {
    private static final String DATABASE_NAME = "Chat.db";
    private static final String DATABASE_TABLE= "message";
    private static final String DATABASE_TABLE_PEER= "peer";
    public static final String TEXT = "text";
    public static final String ID = "_id";
    public static final String SENDER = "sender";

    public static final String DATABASE_CREATE =
            "create table " + DATABASE_TABLE + " ("
                    + ID + " integer primary key, "
                    + TEXT + " text not null, "
                    + SENDER + " text not null"
                    + ")";
    public static final String DATABASE_CREATE_PEER =
            "create table peer ("
                    + ID + " integer primary key, "
                    + "name text not null, "
                    + "address text not null, "
                    + "port text not null"
                    + ")";
    public SQLiteDatabase db;
    private Context context;
    private DatabaseHelper dbHelper;
    private static class DatabaseHelper extends SQLiteOpenHelper {
        public DatabaseHelper(Context context, String name,
                              CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        public void onUpgrade(SQLiteDatabase _db, int _oldVersion, int _newVersion) {
            Log.w("TaskDBAdapter",
                    "Upgrading from version " +  _oldVersion
                            + " to " +  _newVersion);
            _db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
            _db.execSQL("DROP TABLE IF EXISTS peer");
            onCreate(_db);
        }
        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(DbAdapter.DATABASE_CREATE);
            db.execSQL(DbAdapter.DATABASE_CREATE_PEER);


        }
    }
    public DbAdapter(Context _context) {
        context = _context;
        dbHelper = new DatabaseHelper(context, DATABASE_NAME, null,Contract.DATABASE_VERSION);
    }
    public DbAdapter open() throws SQLException { db = dbHelper.getWritableDatabase();
        return this;
    }

    public Cursor getAllPeer () {return db.query(DATABASE_TABLE_PEER,
            new String[] {ID, "name","address","port"},
            null, null, null, null, null);
    }

    public Cursor getMessgeByPeer (String name) {
        String whereClause = "sender = ?";
        String[] whereArgs = new String[] {
                name
        };
        return db.query(DATABASE_TABLE,
                new String[] {ID, TEXT,SENDER},
                whereClause, whereArgs, null, null, null);
    }
}
