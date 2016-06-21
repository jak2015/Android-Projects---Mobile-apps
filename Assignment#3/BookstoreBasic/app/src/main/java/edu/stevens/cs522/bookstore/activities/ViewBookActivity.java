package edu.stevens.cs522.bookstore.activities;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;
import contracts.BookContract;
import databases.CartDbAdapter;
import edu.stevens.cs522.bookstore.R;

public class ViewBookActivity extends Activity {
    private TextView TView = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_results);
        Intent intent = getIntent();
        long id = intent.getLongExtra(BookStoreActivity.Detail, 0);
        int a = intent.getIntExtra("POS",0);
        CartDbAdapter cdadapter = new CartDbAdapter(this);
        cdadapter.open();
        Cursor cursor = cdadapter.fetchbook(id);
        String book_details = "";
        book_details = book_details + "Title : " + BookContract.getTitle(cursor) + '\n' + '\n' +
                "Author : " + BookContract.getAuthor(cursor) + '\n' + '\n' +
                "ID : " + a + '\n' + '\n' +
                "ISBN : " + BookContract.getIsbn(cursor) + '\n' + '\n' +
                "Price : $" + BookContract.getPrice(cursor);
        TView = (TextView) findViewById(R.id.passedView);
        TView.setText(book_details);
    }
}
