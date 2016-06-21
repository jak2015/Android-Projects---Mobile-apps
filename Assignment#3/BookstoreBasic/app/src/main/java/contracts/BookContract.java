package contracts;

import android.content.ContentValues;
import android.database.Cursor;

public class BookContract
{
    public static final String TITLE ="Title";
    public static final String Book_ID = "_id";
    public static final String ISBN = "ISBN";
    public static final String PRICE = "Price";
    public static final String BOOK_TABLE = "Booktable";

    public static final String Author_ID = "_id";
    public static final String NAME = "Name";
    public static final String bookFk = "BookId";
    public static final String Authors = "Authors";

    public static String getTitle(Cursor cursor){
        cursor.moveToFirst();
        int book_title = cursor.getColumnIndexOrThrow(TITLE);
        return cursor.getString(book_title);
    }

    public static int getID(Cursor cursor){
        cursor.moveToFirst();
        return cursor.getInt(cursor.getColumnIndexOrThrow(Book_ID));
    }

    public static String getIsbn(Cursor cursor){
        cursor.moveToFirst();
        return cursor.getString(cursor.getColumnIndexOrThrow(ISBN));
    }

    public static String getPrice(Cursor cursor) {
        cursor.moveToFirst();
        return cursor.getString(cursor.getColumnIndexOrThrow(PRICE));
    }

    public static String getAuthor(Cursor cursor) {
        cursor.moveToFirst();
        return cursor.getString(cursor.getColumnIndexOrThrow(Authors));
    }

    public static void putTitle(ContentValues values, String title){
        values.put(TITLE, title);
    }

    public static void putIsbn(ContentValues values, String Isbn){
        values.put(ISBN, Isbn);
    }

    public static void putPrice(ContentValues values, String Price){
        values.put(PRICE, Price);
    }

    public static void putAuthor(ContentValues values, String Name){
        values.put(NAME,Name);
    }

    public static void putbookfk(ContentValues values, int bookfk){
        values.put(bookFk,bookfk);
    }
}
