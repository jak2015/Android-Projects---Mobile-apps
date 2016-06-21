package edu.stevens.cs522.providers;

import java.util.List;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;

import net.sqlcipher.Cursor;
import net.sqlcipher.DatabaseUtils;
import net.sqlcipher.DefaultDatabaseErrorHandler;
import net.sqlcipher.SQLException;
import net.sqlcipher.database.SQLiteDatabase;
import net.sqlcipher.database.SQLiteOpenHelper;


import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import edu.stevens.cs522.constants.constant;
import edu.stevens.cs522.databases.DbAdapter;

public class CloudProvider extends ContentProvider {

    public static final String TAG = CloudProvider.class.getCanonicalName();

    private static final String DATABASE_NAME = DbAdapter.DATABASE_NAME;
    public static final int DATABASE_VERSION = 47;

    private static final int ALL_ROWS_MESSAGE = 1;
    private static final int SINGLE_ROW_MESSAGE = 2;
    private static final int ALL_ROWS_PEER = 3;
    private static final int SINGLE_ROW_PEER = 4;

    private static final String AUTHORITY = "chatappcloud";
    private static final String DATABASE_TABLE_MESSAGE = DbAdapter.DATABASE_MESSAGE_TABLE;
    private static final String DATABASE_TABLE_PEER = DbAdapter.DATABASE_PEER_TABLE;
    ;
    public static final String TEXT = DbAdapter.TEXT;
    public static final String ID = DbAdapter.ID;
    public static final String SENDER = DbAdapter.SENDER;
    public static final String SENDERNAME = DbAdapter.SENDERNAME;
    public static final String LATITUDE = DbAdapter.LATITUDE;
    public static final String LONGITUDE = DbAdapter.LONGITUDE;

    public static final String DATE = DbAdapter.DATE;
    public static final String MESSAGEID = DbAdapter.MESSAGEID;
    public static final String SENDERID = DbAdapter.SENDERID;
    public static final String ADDRESS = DbAdapter.ADDRESS;

    public static final String REGISTERID = DbAdapter.REGISTERID;

    private DatabaseHelper database;
    private Context context;
    private SQLiteDatabase db;
    public static char[] databaseKey;

    private String charGroup = "_default";
    List<String> usersList = null;
    List<String[]> msgList = null;
    public static Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + DATABASE_TABLE_MESSAGE);
    public static Uri.Builder builder;
    public static Uri CONTENT_URI_PASSWORD;

    public static Uri CONTENT_URI_PEER = Uri.parse("content://" + AUTHORITY + "/" + DATABASE_TABLE_PEER);
    public static Uri.Builder builder_peer;
    public static Uri CONTENT_URI_PEER_PASSWORD;

    public static final Uri CONTENT_URI_PEER_ITEM = Uri.parse("content://" + AUTHORITY + "/" + DATABASE_TABLE_PEER + "#");

    public static final String CONTENT_PATH = "content://chatappcloud/message";
    public static final String CONTENT_PATH_ITEM = "content://chatappcloud/message#";
    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE + "/message";
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/message";

    public static String[] MessageProjection = new String[]{ID, TEXT, SENDER, DATE, MESSAGEID, SENDERID};
    public static String[] peerProjection = new String[]{ID, SENDERNAME, ADDRESS};

    private static class DatabaseHelper extends net.sqlcipher.database.SQLiteOpenHelper {

        public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        public void onUpgrade(net.sqlcipher.database.SQLiteDatabase _db, int _oldVersion, int _newVersion) {
            _db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_MESSAGE);
            _db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE_PEER);
            onCreate(_db);
        }


        public void onCreate(net.sqlcipher.database.SQLiteDatabase db) {
            // TODO Auto-generated method stub
            db.execSQL(DbAdapter.DATABASE_CREATE);
            db.execSQL(DbAdapter.DATABASE_CREATE_PEER);
            db.execSQL("PRAGMA foreign_keys=ON;");
        }


    }

    public CloudProvider() {
        // TODO Auto-generated constructor stub
    }


    public CloudProvider(Context c, char[] dbPassword) {
        context = c;
        databaseKey = dbPassword;
        // TODO Auto-generated constructor stub
    }

    private static final UriMatcher uriMatcher = new UriMatcher(
            UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(AUTHORITY, DATABASE_TABLE_MESSAGE, ALL_ROWS_MESSAGE);
        uriMatcher.addURI(AUTHORITY, DATABASE_TABLE_MESSAGE + "/#", SINGLE_ROW_MESSAGE);
        uriMatcher.addURI(AUTHORITY, DATABASE_TABLE_PEER, ALL_ROWS_PEER);
        uriMatcher.addURI(AUTHORITY, DATABASE_TABLE_PEER + "/#", SINGLE_ROW_PEER);
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        SQLiteDatabase.loadLibs(context);
        String dbPath = context.getDatabasePath(DATABASE_NAME).getPath();
        database = new DatabaseHelper(context, DATABASE_NAME, null, DATABASE_VERSION);
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

        String k = uri.getQueryParameter(constant.SECURITY_PARAMETER_KEY);
        if (k == null) {
            throw new IllegalArgumentException("ERROR !!!!!!!");
        } else {
            databaseKey = k.toCharArray();
            Log.i(TAG, "query: dbkey = "+databaseKey);

            builder = CloudProvider.CONTENT_URI.buildUpon();
            CONTENT_URI_PASSWORD = builder.appendQueryParameter(constant.SECURITY_PARAMETER_KEY, new String(databaseKey)).build();
            Log.i(TAG, "query: ="+CONTENT_URI_PASSWORD);

            builder_peer = CloudProvider.CONTENT_URI_PEER.buildUpon();
            CONTENT_URI_PEER_PASSWORD = builder_peer.appendQueryParameter(constant.SECURITY_PARAMETER_KEY, new String(databaseKey)).build();
            Log.i(TAG, "query: peer= "+CONTENT_URI_PEER_PASSWORD);

        }
        Cursor cursor = null;
        try {
            db = database.getWritableDatabase(databaseKey);

            switch (uriMatcher.match(uri)) {
                case ALL_ROWS_MESSAGE:
                    cursor = db.query(DATABASE_TABLE_MESSAGE, new String[]{ID, TEXT, SENDER,
                                    DbAdapter.DATE, DbAdapter.MESSAGEID, DbAdapter.SENDERID, DbAdapter.LATITUDE, DbAdapter.LONGITUDE},
                            selection, selectionArgs, null, null, sortOrder + " DESC");
                    break;
                case SINGLE_ROW_MESSAGE:
                    cursor = db.query(DATABASE_TABLE_MESSAGE, new String[]{ID, TEXT, SENDER,
                                    DbAdapter.DATE, DbAdapter.MESSAGEID, DbAdapter.SENDERID, DbAdapter.LATITUDE, DbAdapter.LONGITUDE},
                            selection, selectionArgs, null, null, sortOrder + " DESC");
                    break;

                case ALL_ROWS_PEER:
                    cursor = db.query(DATABASE_TABLE_PEER, peerProjection, selection, selectionArgs, null, null, null);
                    break;

                case SINGLE_ROW_PEER:
                    Log.i(TAG, "query: SINGLE_ROW_PEER");
                    cursor = db.query(DATABASE_TABLE_PEER, peerProjection, selection, selectionArgs, null, null, null);

                default:
                    break;
            }
            return cursor;
        }finally {
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        try {
            String k = uri.getQueryParameter(constant.SECURITY_PARAMETER_KEY);
            if (k == null) {
                throw new IllegalArgumentException("ERROR !!!!!!!");
            } else {
                databaseKey = k.toCharArray();
                Log.i(TAG, "insert: databasekey = "+databaseKey.toString());
            }

            db = database.getWritableDatabase(databaseKey);

            int uriType = uriMatcher.match(uri);
            long id = 0;

            try {
                switch (uriType) {
                    case ALL_ROWS_MESSAGE:
                        id = db.insert(DATABASE_TABLE_MESSAGE, null, values);
                        return Uri.parse("content://" + AUTHORITY + "/" + DATABASE_TABLE_MESSAGE + "/" + id);

                    case ALL_ROWS_PEER:
                        id = db.insert(DATABASE_TABLE_PEER, null, values);
                        return Uri.parse("content://" + AUTHORITY + "/" + DATABASE_TABLE_PEER + "/" + id);

                    default:
                        throw new IllegalArgumentException("Unknown URI: " + uri);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            getContext().getContentResolver().notifyChange(uri, null);
            return null;

        } catch (Exception e) {
            e.printStackTrace();

        }
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        try {

            String k = uri.getQueryParameter(constant.SECURITY_PARAMETER_KEY);
            if (k == null) {
                throw new IllegalArgumentException("ERROR !!!!!!!");
            } else {
                databaseKey = k.toCharArray();
            }
            db = database.getWritableDatabase(databaseKey);
            int uriType = uriMatcher.match(uri);
            int rowsDeleted = 0;
            switch (uriType) {
                case ALL_ROWS_MESSAGE:
                    rowsDeleted = db.delete(DATABASE_TABLE_MESSAGE, selection, selectionArgs);
                    break;
                case ALL_ROWS_PEER:
                    rowsDeleted = db.delete(DATABASE_TABLE_PEER, selection, selectionArgs);
                    break;
                case SINGLE_ROW_MESSAGE:
                    String id = uri.getLastPathSegment();
                    if (TextUtils.isEmpty(selection)) {
                        rowsDeleted = db.delete(DATABASE_TABLE_MESSAGE, ID + "=" + id, null);
                    } else {
                        rowsDeleted = db.delete(DATABASE_TABLE_MESSAGE, ID + "=" + id + " and " + selection, selectionArgs);
                    }
                    break;
                default:
                    break;
            }
            return rowsDeleted;

        } catch (Exception e) {
            e.printStackTrace();

        }
        return -1;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        try {

            String k = uri.getQueryParameter(constant.SECURITY_PARAMETER_KEY);
            if (k == null) {
                throw new IllegalArgumentException("ERROR !!!!!!!");
            } else {
                databaseKey = k.toCharArray();
            }
            db = database.getWritableDatabase(databaseKey);
            int uriType = uriMatcher.match(uri);
            switch (uriType) {
                case ALL_ROWS_MESSAGE:
                    return db.update(DATABASE_TABLE_MESSAGE, values, selection, selectionArgs);

                case SINGLE_ROW_MESSAGE:
                    String id = uri.getLastPathSegment();
                    if (TextUtils.isEmpty(selection)) {
                        return db.update(DATABASE_TABLE_MESSAGE, values, ID + "=" + id, null);
                    } else {
                        return db.update(DATABASE_TABLE_MESSAGE, values, ID + "=" + id + " and " + selection, selectionArgs);
                    }
                case SINGLE_ROW_PEER:

                    String peerid = uri.getLastPathSegment();
                    Log.i(TAG, "update peer" + peerid);
                    if (TextUtils.isEmpty(selection)) {
                        return db.update(DATABASE_TABLE_PEER, values, ID + "=" + peerid, null);
                    } else {
                        return db.update(DATABASE_TABLE_PEER, values, ID + "=" + peerid + " and " + selection, selectionArgs);
                    }
                default:
                    return 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
}
