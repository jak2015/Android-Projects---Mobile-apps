package provider;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;


import java.sql.SQLException;

import contracts.BookContract;
import edu.stevens.cs522.bookstore.entities.Book;

public class BookProvider extends ContentProvider {

    private static final int ALL_ROWS = 1;
    private static final int SINGLE_ROW = 2;

    public DatabaseHelper cartDbHelper;
    public SQLiteDatabase cartDb;

    public static final String BOOK_TABLE = "Booktable";
    public static final String AUTHOR_TABLE = "Authortable";
    public static final String AUTHOR_TABLE_INDEX = "Authortableindex";

    public static final String DATABASE_NAME = "Bookstore.db";
    public static final int version = 1;

    private static final UriMatcher umatch;
    static{
        umatch=new UriMatcher(UriMatcher.NO_MATCH);
        umatch.addURI(BookContract.Authority,BookContract.Path+"/#",SINGLE_ROW);
        umatch.addURI(BookContract.Authority,BookContract.Path,ALL_ROWS);
        umatch.addURI(BookContract.Authority,BookContract.PathA+"/#",SINGLE_ROW);
        umatch.addURI(BookContract.Authority,BookContract.PathA,ALL_ROWS);
    }

    @Override
    public boolean onCreate() {
        cartDbHelper = new DatabaseHelper(
                getContext(),
                DATABASE_NAME,
                null,
                1
        );
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb=new SQLiteQueryBuilder();
        switch(umatch.match(uri))
        {
            case ALL_ROWS:
                cartDb = cartDbHelper.getWritableDatabase();
                Cursor cursor = cartDb.rawQuery(BOOK_AUTHOR_JOIN_CART, null);
                return cursor;
            case SINGLE_ROW:
                cartDb = cartDbHelper.getWritableDatabase();
                String rowId=uri.getPathSegments().get(1);
                qb.appendWhere(BookContract.Book_ID+"="+rowId);

                String BOOK_AUTHOR_JOIN_DETAIL = "SELECT " +
                        "Booktable._id, Title,Price,ISBN,GROUP_CONCAT(Name,'|') as " + BookContract.Authors +
                        " FROM Booktable LEFT OUTER JOIN Authortable " +
                        "ON Booktable._id=Authortable.BookId WHERE Booktable._id=" + rowId +
                        " GROUP BY Booktable._id";
                Cursor cu = cartDb.rawQuery(BOOK_AUTHOR_JOIN_DETAIL, null);
                return cu;
        }
        return null;
    }

    @Override
    public String getType(Uri uri) {
        switch(umatch.match(uri)){
            case ALL_ROWS:
                return BookContract.contentType;
            case SINGLE_ROW:
                return BookContract.contentItemType;
        }
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        cartDb = cartDbHelper.getWritableDatabase();
        if(uri.toString().contains("authors")) {
            long k = cartDb.insert(AUTHOR_TABLE, null, contentValues);
            if (k > 0) {
                Uri instaUri = BookContract.CONTENT_URI_A(k);
                ContentResolver cr = getContext().getContentResolver();
                cr.notifyChange(instaUri, null);
                return instaUri;}
        }
        else {
            long l = cartDb.insert(BOOK_TABLE, null, contentValues);
            if (l > 0) {
                Uri instaUri = BookContract.CONTENT_URI(l);
                ContentResolver cr = getContext().getContentResolver();
                cr.notifyChange(instaUri, null);
                return instaUri;
            }
        }
        return uri;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        switch(umatch.match(uri))
        {
            case ALL_ROWS:
                // TODO: Delete all records in the shopping cart.
                cartDb.delete(AUTHOR_TABLE,null,null);
                cartDb.delete(BOOK_TABLE, null, null);
                getContext().getContentResolver().notifyChange(uri, null);
                return 0;
            case SINGLE_ROW:
                long rowId = BookContract.getUriId(uri);
                String where = BookContract.bookFk+"="+rowId;
                String whereArgs = BookContract.Book_ID+"="+rowId;
                cartDb.delete(AUTHOR_TABLE, where,null);
                cartDb.delete(BOOK_TABLE,whereArgs,null);
                getContext().getContentResolver().notifyChange(uri, null);
                return 1;
        }
        return -1;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }

    public static final String DATABASE_BOOK = "CREATE TABLE "+BOOK_TABLE+" ("+
            BookContract.Book_ID+" INTEGER PRIMARY KEY, " +
            BookContract.TITLE + " TEXT" +", "+
            BookContract.ISBN + " TEXT" +", "+
            BookContract.PRICE + " TEXT"+")";

    private static final String DATABASE_AUTHOR = "CREATE TABLE " +
            AUTHOR_TABLE + "(" +
            BookContract.Author_ID + " INTEGER PRIMARY KEY, " +
            BookContract.NAME + " TEXT NOT NULL" +","+
            BookContract.bookFk + " INTEGER" +","+
            "FOREIGN KEY " +"("+ BookContract.bookFk +")"+ " REFERENCES " + BOOK_TABLE + "(" + BookContract.Book_ID + ")" + ");";

    public static final String BOOK_AUTHOR_JOIN_CART = "SELECT " +
            "Booktable._id, Title,Price,ISBN,Name as "+BookContract.Authors +
            " FROM Booktable LEFT OUTER JOIN Authortable " +
            "ON Booktable._id=Authortable.BookId GROUP BY Booktable._id";

    public static final String FORIEGN_KEY = "PRAGMA foreign_keys=ON;";
    private static final String INDEX_CREATE = "CREATE INDEX " +
            AUTHOR_TABLE_INDEX + " ON " + AUTHOR_TABLE + "(" + BookContract.bookFk + ");";

    public class DatabaseHelper extends SQLiteOpenHelper {
        public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version)
        {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try
            {
                db.execSQL(FORIEGN_KEY);
                db.execSQL(DATABASE_AUTHOR);
                db.execSQL(DATABASE_BOOK);
                db.execSQL(INDEX_CREATE);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w("TaskDBAdapter", "Upgrading from version" +
                    oldVersion + "to" + newVersion + " which will destroy all old data");
            // Upgrade the existing database to conform to the new version.
            // Multiple previous versions can be handled by
            // comparing oldVersion and newVersion values.
            // The simplest case is to drop the old table and create a new one.
            db.execSQL("DROP TABLE IF IT EXISTS" + BOOK_TABLE);
            db.execSQL("DROP TABLE IF IT EXISTS" + AUTHOR_TABLE);
            // Create a new one.
            onCreate(db);
        }
    }
}
