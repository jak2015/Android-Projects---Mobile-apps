package edu.stevens.cs522.bookstore.activities;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;
import java.util.Random;

import contracts.BookContract;
import edu.stevens.cs522.bookstore.R;
import edu.stevens.cs522.bookstore.entities.Author;
import edu.stevens.cs522.bookstore.entities.Book;
import managers.BookManager;
import managers.IContinue;
import provider.BookProvider;


public class AddBookActivity extends Activity {
    private EditText Title, Author, isbn;

    @Override
	public void onCreate(Bundle savedInstanceState)
    {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_book);
        Title = (EditText)findViewById(R.id.search_title);
        Author = (EditText)findViewById(R.id.search_author);
        isbn = (EditText)findViewById(R.id.search_isbn);
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		// TODO provide SEARCH and CANCEL options
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_book_menu,menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		  switch(item.getItemId())
            {   case R.id.search:
                String Title1 = Title.getText().toString();
                String Author1 = Author.getText().toString();
                String isbn1 = isbn.getText().toString();
                if(Title1.isEmpty() || Author1.isEmpty() || isbn1.isEmpty())
                {
                    Toast.makeText(getApplicationContext(),"Some text fields are empty ", Toast.LENGTH_SHORT).show();
                    return true;
                }
                else{
                    int auth = searchBook();
                    getAuthor(auth);
                    Intent intent = new Intent();
                    setResult(1, intent);
                    finish();
                    return true;
                }
            case R.id.cancel:
                Toast.makeText(this, "No book added to the cart!", Toast.LENGTH_LONG).show();
                setResult(RESULT_CANCELED);
                finish();
                return super.onOptionsItemSelected(item);
        }
		// SEARCH: return the book details to the BookStore activity
		return true;
		// CANCEL: cancel the search request
	}

    ContentValues cv = new ContentValues();
    public void getAuthor(int fk) {
        EditText editText = (EditText) findViewById(R.id.search_author);
        String msg = editText.getText().toString();
        if (msg.contains(",")) {
            String[] auth = msg.split(",");
            ContentResolver cr = getContentResolver();
            ContentValues cv2= new ContentValues();
            Author a=new Author(auth[0],fk);
            Author aa=new Author(auth[1],fk);
            a.writeToProvider(cv);
            aa.writeToProvider(cv2);
            cr.insert(BookContract.CONTENT_URI_A,cv);
            cr.insert(BookContract.CONTENT_URI_A,cv2);
        }
        else{
            Author a = new Author(msg,fk);
            ContentResolver cr = getContentResolver();
            a.writeToProvider(cv);
            Uri u =cr.insert(BookContract.CONTENT_URI_A,cv);
            String idd = u.getPathSegments().get(1);
        }
    }

    public int searchBook(){

        int max_price = 700;
        int min_price = 10;
        EditText title = (EditText) findViewById(R.id.search_title);
        String title_str = title.getText().toString();
        EditText isbn = (EditText) findViewById(R.id.search_isbn);
        String isbn_str = isbn.getText().toString();
        Random rndm = new Random();
        int price = rndm.nextInt((max_price - min_price)+1)+min_price;
        String prc = "";
        prc = prc + price;
        ContentValues cvb=new ContentValues();
        Book b = new Book(title_str,isbn_str,prc);
        b.writeToProvider(cvb);
        ContentResolver cr = getContentResolver();
        Uri bookrow=cr.insert(BookContract.CONTENT_URI, cvb);
        int rowId = Integer.parseInt(bookrow.getLastPathSegment());
        return rowId;
        // TODO Just build a Book object with the search criteria and return that
	}

}