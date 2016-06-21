package edu.stevens.cs522.bookstore.activities;

import java.util.ArrayList;
import java.util.Collections;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import contracts.BookContract;
import databases.CartDbAdapter;
import edu.stevens.cs522.bookstore.R;

public class BookStoreActivity extends ListActivity
        implements LoaderManager.LoaderCallbacks<Cursor>{
	
	// Use this when logging errors and warnings.
	@SuppressWarnings("unused")
	private static final String TAG = BookStoreActivity.class.getCanonicalName();
	
	// These are request codes for subactivity request calls
	static final private int ADD_REQUEST = 1;
    private static final int BOOK_LOADER_ID = 1;
    public static final int AUTH_LOADER_ID = 2;
	@SuppressWarnings("unused")
	static final private int CHECKOUT_REQUEST = ADD_REQUEST + 1;

	// There is a reason this must be an ArrayList instead of a List.
	@SuppressWarnings("unused")
	private ArrayList<Integer> del = new ArrayList<Integer>();
    private static ArrayList<Integer> ref = new ArrayList<Integer>();
    private ListView lstvw;
    public final static String EXTRA = "E";
    int count;

    LoaderManager lm = getLoaderManager();

    @Override
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO check if there is saved UI state, and if so, restore it (i.e. the cart contents)
        // TODO Set the layout (use cart.xml layout)
        setContentView(R.layout.cart);
        lstvw = (ListView) findViewById(android.R.id.list);
        lm.initLoader(BOOK_LOADER_ID,null,this);
        lstvw.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        lstvw.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {
                if(lstvw.isItemChecked(i)) {
                    count = count + 1;
                    actionMode.setTitle(count + " book(s) selected");
                    del.add(i+1);
                }
                else
                {
                    count--;
                    actionMode.setTitle(count + " book(s) selected");
                    del.remove(i);
                }
            }

            @Override
            public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
                MenuInflater inflater = actionMode.getMenuInflater();
                inflater.inflate(R.menu.menu_del, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.delete:
                        calldelete();
                        del.clear();
                        count = 0;
                        actionMode.finish(); // Action picked, so close the CAB
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode actionMode) {
                count = 0;
                del.clear();
            }
        });
        lstvw.setOnItemClickListener(  new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id)
            {
                Intent i = new Intent(BookStoreActivity.this, ViewBook.class);
                i.putExtra("POS",position+1);
                i.putExtra("Detail",id);
                startActivity(i);
            }
        });

    }

    public void calldelete()
    {
        Collections.sort(del);
        for(int i=del.size()-1;i>=0;i--)
        {
            int j=del.get(i);
            int index=ref.get(j-1);
            ref.remove(j-1);
            Uri ui=BookContract.CONTENT_URI(index);
            ContentResolver cr = getContentResolver();
            cr.delete(ui,null,null);
        }
        lm.restartLoader(BOOK_LOADER_ID,null,this);
    }

    // TODO use an array adapter to display the cart contents.
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// TODO provide ADD, DELETE and CHECKOUT options
        MenuInflater inflater= getMenuInflater();
        inflater.inflate(R.menu.bookstore_menu,menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		// TODO
		
		// ADD provide the UI for adding a book
        switch(item.getItemId()) {
            case R.id.add:
                Intent addIntent = new Intent(this, AddBookActivity.class);
                startActivityForResult(addIntent,ADD_REQUEST);
                return true;
            case R.id.checkout:
                Intent checkIntent = new Intent(this, CheckoutActivity.class);
                int count=lstvw.getCount();
                if(count == 0){
                    Toast.makeText(this, "Cart is Empty!!", Toast.LENGTH_LONG).show();
                }
                else {
                    checkIntent.putExtra(EXTRA, count);
                    startActivityForResult(checkIntent, CHECKOUT_REQUEST);
                }
                return true;
        }
		// CHECKOUT provide the UI for checking out
	return false;
    }


	@Override
	protected void onActivityResult(int requestCode, int resultCode,
			Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		// TODO Handle results from the Search and Checkout activities.
		
		// Use SEARCH_REQUEST and CHECKOUT_REQUEST codes to distinguish the cases.
		if(resultCode==ADD_REQUEST)
        {
            lm.restartLoader(BOOK_LOADER_ID, null, this);
        }
        else if(resultCode==CHECKOUT_REQUEST)
        {
            ref.clear();
            ContentResolver cr= getContentResolver();
            cr.delete(BookContract.CONTENT_URI,null,null);
            lm.restartLoader(BOOK_LOADER_ID, null, this);
        }

		// SEARCH: add the book that is returned to the shopping cart.
		
		// CHECKOUT: empty the shopping cart.

	}

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        switch (i) {
            case BOOK_LOADER_ID:
                return new CursorLoader(this, BookContract.CONTENT_URI, null
                            ,null, null, null);

            case AUTH_LOADER_ID:
                return new CursorLoader(this,BookContract.CONTENT_URI_A,null
                        ,null,null,null);
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        CartDbAdapter cdb = new CartDbAdapter(this, cursor);
        int i = cursor.getCount();
        Log.i(TAG, Integer.toString(i));
        cursor.moveToFirst();
        ref.clear();
        for(int j=0;j<i;j++)
        {
            int m=BookContract.getId(cursor);
            ref.add(m);
            cursor.moveToNext();
        }
        lstvw.setAdapter(cdb);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        CartDbAdapter cdb = new CartDbAdapter(this, null);
        cdb.swapCursor(null);
    }
}