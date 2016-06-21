package edu.stevens.cs522.bookstore.activities;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import contracts.BookContract;
import edu.stevens.cs522.bookstore.R;

public class ViewBook extends Activity implements LoaderManager.LoaderCallbacks<Cursor> {
    private TextView TView = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search_results);
        LoaderManager lm = getLoaderManager();
        lm.initLoader(0,null,this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int v_book, Bundle bundle) {
        switch(v_book)
        {
            case 0:
                Intent intent = getIntent();
                long id = intent.getLongExtra("Detail",0);
                Uri ui=BookContract.CONTENT_URI(id);
                return new CursorLoader(this,ui,null,null,null,null);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        String book_details = "";
        book_details = book_details + "Title : " + BookContract.getTitle(cursor) + '\n' + '\n' +
                "Author : " + BookContract.getAuthor(cursor) + '\n' + '\n' +
                "ISBN : " + BookContract.getIsbn(cursor) + '\n' + '\n' +
                "Price : $" + BookContract.getPrice(cursor);
        TView = (TextView) findViewById(R.id.passedView);
        TView.setText(book_details);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }
}


