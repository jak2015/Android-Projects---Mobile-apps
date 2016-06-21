package databases;
//Jayshree Kanse
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import contracts.BookContract;

public class CartDbAdapter {

    private static final String TAG = "BookStoreDbAdapter";

    public static final String DATABASE_NAME = "Bookstore.db";
    public static final int DATABASE_VERSION = 1;
    public static final String BOOK_TABLE = "Booktable";
    public static final String AUTHOR_TABLE = "Authortable";
    public static final String AUTHOR_TABLE_INDEX = "Authortableindex";

    public DatabaseHelper cartDbHelper;
    public SQLiteDatabase cartDb;
    public final Context cartContext;

    public static final String BOOK_AUTHOR_JOIN_CART = "SELECT " +
            "Booktable._id, Title,Price,ISBN,GROUP_CONCAT(Name,' | ') as "+ BookContract.Authors +
            " FROM Booktable LEFT OUTER JOIN Authortable " +
            "ON Booktable._id = Authortable.BookId GROUP BY Booktable._id";

    public static final String FORIEGN_KEY = "PRAGMA foreign_keys=ON;";

    private static final String BOOK = "CREATE TABLE "+BOOK_TABLE+" ("+
            BookContract.Book_ID+" INTEGER PRIMARY KEY, " +
            BookContract.TITLE + " TEXT" +", "+
            BookContract.ISBN + " TEXT" +", "+
            BookContract.PRICE + " TEXT"+")";

    private static final String AUTHOR = "CREATE TABLE " +
            AUTHOR_TABLE + "(" +
            BookContract.Author_ID + " INTEGER PRIMARY KEY, " +
            BookContract.NAME + " TEXT NOT NULL" +","+
            BookContract.bookFk + " INTEGER" +","+
            "FOREIGN KEY " +"("+ BookContract.bookFk +")"+ " REFERENCES " + BOOK_TABLE + "(" + BookContract.Book_ID + ")" + ");";

    public CartDbAdapter(Context cartContext) {
        this.cartContext = cartContext;
    }

    public CartDbAdapter open() throws SQLException {
        cartDbHelper = new DatabaseHelper(cartContext,DATABASE_NAME,null,DATABASE_VERSION);
        cartDb = cartDbHelper.getWritableDatabase();
        cartDb.execSQL(FORIEGN_KEY);
        return this;
    }

    public Cursor fetchAllBooks(){
        cartDb = cartDbHelper.getWritableDatabase();
        Cursor cursor = cartDb.rawQuery(BOOK_AUTHOR_JOIN_CART, null);
        return cursor;
    }

    public Cursor fetchbook(long rowId) throws SQLException {
        cartDb = cartDbHelper.getWritableDatabase();

        String BOOK_AUTHOR_JOIN_DETAIL = "SELECT " +
                "Booktable._id, Title,Price,ISBN,GROUP_CONCAT(Name,' | ') as " + BookContract.Authors +
                " FROM Booktable LEFT OUTER JOIN Authortable " +
                "ON Booktable._id=Authortable.BookId WHERE Booktable._id=" + rowId +
                " GROUP BY Booktable._id";

        Cursor cursor = cartDb.rawQuery(BOOK_AUTHOR_JOIN_DETAIL, null);
        return cursor;
    }

    public long createbook(String title, String isbn, String price) {
        ContentValues contentvalues = new ContentValues();
        BookContract.putTitle(contentvalues,title);
        BookContract.putIsbn(contentvalues,isbn);
        BookContract.putPrice(contentvalues,price);

        return cartDb.insert(BOOK_TABLE, null, contentvalues);
    }

    public long createauthor(String author, int bookFk) {
        ContentValues contentvalues = new ContentValues();
        BookContract.putAuthor(contentvalues,author);
        BookContract.putbookfk(contentvalues,bookFk);

        return cartDb.insert(AUTHOR_TABLE, null, contentvalues);
    }

    public void deleteAll() {
        cartDb.delete(AUTHOR_TABLE,null,null);
        cartDb.delete(BOOK_TABLE, null, null);
    }

    public boolean deleteItem(int rowId) {
        String where = BookContract.bookFk+"="+rowId;
        String whereArgs= BookContract.Book_ID+"="+rowId;
        try {
            cartDb.delete(AUTHOR_TABLE, where,null);
        }
        catch(Exception e){e.printStackTrace();}
        cartDb.delete(BOOK_TABLE,whereArgs,null);

        return true;
    }

    public void close() {
        cartDbHelper.close();
    }

    public class DatabaseHelper extends SQLiteOpenHelper {
        private static final String INDEX = "CREATE INDEX " +
                AUTHOR_TABLE_INDEX + " ON " + AUTHOR_TABLE + "(" + BookContract.bookFk + ");";

        public DatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            try{
                db.execSQL(FORIEGN_KEY);
                db.execSQL(AUTHOR);
                db.execSQL(BOOK);
                db.execSQL(INDEX);
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            // Log the version upgrade.
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF IT EXISTS" + BOOK_TABLE);
            db.execSQL("DROP TABLE IF IT EXISTS" + AUTHOR_TABLE);
            // Create a new one.
            onCreate(db);
        }
    }
}

