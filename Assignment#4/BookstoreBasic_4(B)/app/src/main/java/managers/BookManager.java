package managers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import contracts.BookContract;
import edu.stevens.cs522.bookstore.entities.Book;


public class BookManager extends Manager<Book>{

    public BookManager(Context context, IEntityCreator<Book> creator,
                       int loaderID) {
        super(context, creator, loaderID);
        // TODO Auto-generated constructor stub
    }

    public void persistAsync(Book book, IContinue<Uri> callback) {
        ContentValues values = new ContentValues();
        book.writeToProvider(values);
        asyncResolver.insertAsync(BookContract.CONTENT_URI, values, callback);
    }

    public void getAllAsync(Uri uri,
                            String[] projection,
                            String selection,
                            String[] selectionArgs,
                            String sortOrder,
                            IContinue<Cursor> callback) {
        super.getAllAsync(uri, projection, selection, selectionArgs, sortOrder, callback);
    }

    public void deleteAsync (Uri uri, String selection, String[] selectionArgs) {
        super.deleteAsync(uri, selection, selectionArgs);
    }
}
