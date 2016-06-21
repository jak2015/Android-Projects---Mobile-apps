package edu.stevens.cs522.bookstore.activities;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;
import java.util.Random;
import contracts.BookContract;
import databases.CartDbAdapter;
import edu.stevens.cs522.bookstore.R;


public class AddBookActivity extends Activity {

    // Use this as the key to return the book details as a Parcelable extra in the result intent.
    public static final String BOOK_RESULT_KEY = "ADD_BOOK_RESULT";
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
        MenuInflater inflater= getMenuInflater();
        inflater.inflate(R.menu.add_book_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        // TODO
        switch(item.getItemId())
        {
            case R.id.search:
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
                setResult(RESULT_CANCELED);
                finish();
                return super.onOptionsItemSelected(item);
        }
        // SEARCH: return the book details to the BookStore activity
        return true;
        // CANCEL: cancel the search request
    }

    public void getAuthor(int fornkey)
    {
        EditText editText = (EditText) findViewById(R.id.search_author);
        String msg = editText.getText().toString();
        if (msg.contains(",")) {
            String[] auth = msg.split(",");
            //Author a= new Author(msg,msg);
            CartDbAdapter cdadapter = new CartDbAdapter(this);
            cdadapter.open();
            cdadapter.createauthor(auth[0], fornkey);
            cdadapter.createauthor(auth[1], fornkey);
            cdadapter.close();
        }
        else{
            CartDbAdapter cdadapter = new CartDbAdapter(this);
            cdadapter.open();
            cdadapter.createauthor(msg, fornkey);
            cdadapter.close();
        }
    }

    public int searchBook()
    {
        int max_p = 500;
        int min_p = 20;
        EditText title = (EditText) findViewById(R.id.search_title);
        String title_str = title.getText().toString();
        EditText isbn = (EditText) findViewById(R.id.search_isbn);
        String isbn_str = isbn.getText().toString();
        Random r = new Random();
        int price = r.nextInt((max_p-min_p)+1)+min_p;
        String prc = "";
        prc = prc + price;
        CartDbAdapter cdadapter = new CartDbAdapter(this);
        cdadapter.open();
        cdadapter.createbook(title_str, isbn_str, prc);

        String where = "SELECT * FROM "+ BookContract.BOOK_TABLE+" WHERE "+BookContract.ISBN+"="+"'"+isbn_str+"'";
        cdadapter.open();
        Cursor cursor = cdadapter.cartDb.rawQuery(where,null);
        cursor.moveToFirst();
        int ID = BookContract.getID(cursor);
        return ID;
        // TODO Just build a Book object with the search criteria and return that
    }
}