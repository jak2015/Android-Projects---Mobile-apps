package edu.stevens.cs522.chat.oneway;
import provider.ChatProvider;
import android.Manifest.permission;
import android.content.ContentValues;
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
		// Context of the application using the database. 
	private Context context;
	private DatabaseHelper dbHelper;
	private static class DatabaseHelper extends SQLiteOpenHelper {
		   public DatabaseHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		public void onUpgrade(SQLiteDatabase _db, int _oldVersion, int _newVersion) {
		      Log.w("TaskDBAdapter", "Upgrading from version " +  _oldVersion + " to " +  _newVersion);
		      _db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
		      _db.execSQL("DROP TABLE IF EXISTS peer");
		      onCreate(_db);
		   }
		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub
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
	public Cursor getAllEntries () {return db.query(DATABASE_TABLE,
            new String[] {ID, TEXT,SENDER},
            null, null, null, null, null);
	}	
	public boolean deleteAll(){
		return db.delete(DATABASE_TABLE, null, null)>0;
	}
	public boolean addMessage(ChatMessage b) {
		ContentValues contentValues = new ContentValues();
		b.writeToProvider(contentValues);
		return db.insert(DATABASE_TABLE, null, contentValues)>0;
		 
	}
	public boolean addPeer(Peer b) {
		ContentValues contentValues = new ContentValues();
		b.writeToProvider(contentValues);
		db.delete(DATABASE_TABLE_PEER, "name ='" + b.name +"' and "+"address ='"+b.address.getHostAddress()+"'", null);
		return db.insert(DATABASE_TABLE_PEER, null, contentValues)>0;
		 
	}
/*	public boolean deletebytitle(String t,String a) throws SQLException{
		//boolean re=db.delete(DATABASE_TABLE, TITLE + "='" + t +"' and "+ AUTHOR+"='"+a+"'", null)>0;
		
		return true;
	}
	public boolean selectbytitle(String t,String a) throws SQLException{
		//boolean re=db.delete(DATABASE_TABLE, TITLE + "='" + t +"' and "+ AUTHOR+"='"+a+"'", null)>0;
		
		return true;
	}*/
	public boolean deletePeer(Peer b) {
		ContentValues contentValues = new ContentValues();
		b.writeToProvider(contentValues);
		return db.delete(DATABASE_TABLE_PEER, "name ='" + b.name +"' and "+"address ='"+b.address.getHostAddress()+"'", null)>0;
		 
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
