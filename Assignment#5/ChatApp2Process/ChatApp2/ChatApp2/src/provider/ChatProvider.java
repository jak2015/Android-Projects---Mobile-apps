package provider;

import java.util.Arrays;
import java.util.HashSet;

import edu.stevens.cs522.chat.oneway.Contract;
import edu.stevens.cs522.chat.oneway.DbAdapter;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;


public class ChatProvider extends ContentProvider {
	
	  private DatabaseHelper database;
	  private Context context;
	  private SQLiteDatabase db;
	  
	  public static String[] projection = new String[] {
	          Contract.ID,Contract.TEXT,Contract.SENDER
	        };
	  public static String[] projection2 = new String[] {
		  Contract.ID,"name","address","port"
	        };
	//private static UriMatcher uriMatcher;
	  public static class DatabaseHelper extends SQLiteOpenHelper {
		   public DatabaseHelper(Context context, String name,
				CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		// Database version mismatch: version on disk
		// needs to be upgraded to the current version.
		public void onUpgrade(SQLiteDatabase _db, int _oldVersion,
		                                             int _newVersion) {
		      // Log the version upgrade.
		      Log.w("TaskDBAdapter",
		            "Upgrading from version " +  _oldVersion
		                             + " to " +  _newVersion);
		      // Upgrade: drop the old table and create a new one.
		      
		      _db.execSQL("DROP TABLE IF EXISTS " + Contract.DATABASE_TABLE);
		      _db.execSQL("DROP TABLE IF EXISTS author");
		      onCreate(_db);
		   }
		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub
			db.execSQL(DbAdapter.DATABASE_CREATE);
			db.execSQL(DbAdapter.DATABASE_CREATE_PEER);
			db.execSQL("PRAGMA foreign_keys=ON;");

		}
	}
	private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
	  static {
	    uriMatcher.addURI(Contract.AUTHORITY, Contract.DATABASE_TABLE, Contract.ALL_ROWS);
	    uriMatcher.addURI(Contract.AUTHORITY, Contract.DATABASE_TABLE + "/#", Contract.SINGLE_ROW);
	    uriMatcher.addURI(Contract.AUTHORITY, Contract.DATABASE_TABLE_PEER, Contract.ALL_ROWS_PEER);
	    uriMatcher.addURI(Contract.AUTHORITY, Contract.DATABASE_TABLE_PEER + "/#", Contract.SINGLE_ROW_PEER);
	  }
	 

	@Override
	public String getType(Uri uri) {
		// TODO: Implement this to handle requests for the MIME type of the data
		// at the given URI.
		throw new UnsupportedOperationException("Not yet implemented");
	}


	@Override
	public boolean onCreate() {
		Context context = getContext();
		database = new DatabaseHelper(context, Contract.DATABASE_NAME, null,Contract.DATABASE_VERSION);
		db = database.getWritableDatabase();
		return false;
	}
	public ChatProvider() {
		// TODO Auto-generated constructor stub
	}
public ChatProvider(Context c) {
	context=c;
		database=new DatabaseHelper(c, Contract.DATABASE_NAME, null, Contract.DATABASE_VERSION);
		db=database.getWritableDatabase();
		// TODO Auto-generated constructor stub
	}
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		 Cursor cursor=null;
		 switch (uriMatcher.match(uri)) {
	      case Contract.ALL_ROWS :
	    	  cursor = db.query(Contract.DATABASE_TABLE,
			            new String[] {Contract.ID, Contract.TEXT,Contract.SENDER},
			            selection, selectionArgs, null, null, null);
	    	  break;
	         // query the database
	      case Contract.SINGLE_ROW :
	         selection = Contract.ID + "=?";
	         selectionArgs[0] = uri.getLastPathSegment();
	         break;
	      case Contract.ALL_ROWS_PEER:
	    	  cursor = db.query(Contract.DATABASE_TABLE_PEER,
			            new String[] {Contract.ID, "name","address","port"},
			            selection, selectionArgs, null, null, null);
	    	  break;
	         // query the database
	      default: 
	    	  throw new UnsupportedOperationException("Not yet implemented");
	} 
		 //SQLiteDatabase db = database.getWritableDatabase();
		 
		 return cursor;
	}

	@Override
	 public Uri insert(Uri uri, ContentValues values) {
	    int uriType = uriMatcher.match(uri);
	    long id = 0;
	   
	    try{
	    	//sqlDB = database.getWritableDatabase();
	    switch (uriType) {
	    case Contract.ALL_ROWS:
	      id = db.insert(Contract.DATABASE_TABLE, null, values);
	      return Uri.parse("content://" + Contract.AUTHORITY
		  	      + "/" + Contract.DATABASE_TABLE + "/" + id);
	    case Contract.ALL_ROWS_PEER:
	    	id = db.insert(Contract.DATABASE_TABLE_PEER, null, values);
		      return Uri.parse("content://" + Contract.AUTHORITY
			  	      + "/" + Contract.DATABASE_TABLE_PEER + "/" + id);
	      default:
	      throw new IllegalArgumentException("Unknown URI: " + uri);
	    }
	    }catch(Exception e){
	    	Log.e("insert fail", "fail to insert");
	    }
	    
	    getContext().getContentResolver().notifyChange(uri, null);
	    //getContext().getContentResolver().notifyChange(uri, null);
	    return null;
	  }

	  @Override
	  public int delete(Uri uri, String selection, String[] selectionArgs) {
	    int uriType = uriMatcher.match(uri);
	    //SQLiteDatabase sqlDB = database.getWritableDatabase();
	    int rowsDeleted = 0;
	    switch (uriType) {
	    case Contract.ALL_ROWS:
	      rowsDeleted = db.delete(Contract.DATABASE_TABLE, selection,
	          selectionArgs);
	      break;
	    case Contract.ALL_ROWS_PEER:
		      rowsDeleted = db.delete(Contract.DATABASE_TABLE_PEER, selection,
		          selectionArgs);
		      break;
	    case Contract.SINGLE_ROW:
	      String id = uri.getLastPathSegment();
	      if (TextUtils.isEmpty(selection)) {
	        rowsDeleted = db.delete(Contract.DATABASE_TABLE,Contract.ID + "=" + id, null);
	      } else {
	        rowsDeleted = db.delete(Contract.DATABASE_TABLE,Contract.ID + "=" + id  + " and " + selection,
	            selectionArgs);
	      }
	      break;
	    default:
	      throw new IllegalArgumentException("Unknown URI: " + uri);
	    }
	    //context.getContentResolver().notifyChange(uri, null);
	    return rowsDeleted;
	  }

	  @Override
	  public int update(Uri uri, ContentValues values, String selection,
	      String[] selectionArgs) {

	    int uriType = uriMatcher.match(uri);
	   // SQLiteDatabase sqlDB = database.getWritableDatabase();
	    int rowsUpdated = 0;
	    switch (uriType) {
	    case Contract.ALL_ROWS:
	      rowsUpdated = db.update(Contract.DATABASE_TABLE, 
	          values, 
	          selection,
	          selectionArgs);
	      break;
	    case Contract.SINGLE_ROW:
	      String id = uri.getLastPathSegment();
	      if (TextUtils.isEmpty(selection)) {
	        rowsUpdated = db.update(Contract.DATABASE_TABLE, 
	            values,
	            Contract.ID + "=" + id, 
	            null);
	      } else {
	        rowsUpdated = db.update(Contract.DATABASE_TABLE, 
	            values,
	            Contract.ID + "=" + id 
	            + " and " 
	            + selection,
	            selectionArgs);
	      }
	      break;
	    default:
	      throw new IllegalArgumentException("Unsupported URI: " + uri);
	    }
	    //getContext().getContentResolver().notifyChange(uri, null);
	    return rowsUpdated;
	  }

	  @SuppressWarnings("unused")
	private void checkColumns(String[] projection) {
		    String[] available = { Contract.ID,Contract.TEXT,Contract.SENDER };
		    if (projection != null) {
		      HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
		      HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
		      // check if all columns which are requested are available
		      if (!availableColumns.containsAll(requestedColumns)) {
		        throw new IllegalArgumentException("Unknown columns in projection");
		      }
		    }
		  }


	  


}
