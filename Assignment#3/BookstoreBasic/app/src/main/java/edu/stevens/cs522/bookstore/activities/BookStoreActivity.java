package edu.stevens.cs522.bookstore.activities;

import java.util.ArrayList;
import java.util.Collections;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

import contracts.BookContract;
import edu.stevens.cs522.bookstore.R;
import edu.stevens.cs522.bookstore.entities.Book;
import databases.CartDbAdapter;

public class BookStoreActivity extends ListActivity {

    // Use this when logging errors and warnings.
    @SuppressWarnings("unused")
    private static final String TAG = BookStoreActivity.class.getCanonicalName();

    // These are request codes for subactivity request calls
    static final private int ADD_REQUEST = 1;

    @SuppressWarnings("unused")
    static final private int CHECKOUT_REQUEST = ADD_REQUEST + 1;

    // There is a reason this must be an ArrayList instead of a List.
    @SuppressWarnings("unused")
    private ArrayList<Book> shoppingCart = new ArrayList<Book>();
    private ArrayList<Integer> del = new ArrayList<Integer>();
    static final String SHOP_CART = "shopcart";
    private ListView lstview;
    public final static String Detail = "D";
    public final static String EXTRA = "E";
    int count;

    public void Display() {
        CartDbAdapter cdadapter = new CartDbAdapter(this);
        cdadapter.open();
        Cursor cursor = cdadapter.fetchAllBooks();
        if (cursor != null) {
            cursor.moveToFirst();
            int x = 0;
            while (x < cursor.getCount()) {
                String[] from = {BookContract.TITLE, BookContract.Authors};
                int[] to = new int[]{android.R.id.text1, android.R.id.text2};

                // Now create a list adaptor that encapsulates the result of a DB query
                SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_activated_2, cursor, from, to);
                adapter.notifyDataSetChanged();
                lstview.setAdapter(adapter);
                x++;
            }
        }
        String[] from = {BookContract.TITLE, BookContract.Authors};
        int[] to = new int[]{android.R.id.text1, android.R.id.text2};

        // Now create a list adaptor that encapsulates the result of a DB query
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_activated_2, cursor, from, to);
        adapter.notifyDataSetChanged();
        lstview.setAdapter(adapter);
        cdadapter.close();
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO check if there is saved UI state, and if so, restore it (i.e. the cart contents)
        // TODO Set the layout (use cart.xml layout)
        setContentView(R.layout.cart);
        lstview = (ListView) findViewById(android.R.id.list);

        Display();
        lstview.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        lstview.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode actionMode, int i, long l, boolean b) {
                if(lstview.isItemChecked(i)) {
                    count = count + 1;
                    actionMode.setTitle(count + " book(s) selected");
                    del.add(i + 1);
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
                        Display();
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
        lstview.setOnItemClickListener(  new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id)
            {
                Intent intent = new Intent(BookStoreActivity.this, ViewBookActivity.class);

                intent.putExtra("POS",position+1);
                intent.putExtra(Detail,id);
                startActivity(intent);
            }
        });
    }

    public void calldelete()
    {
        Collections.sort(del);
        for(int i = del.size()-1;i>=0;i--)
        {   CartDbAdapter cdadapter = new CartDbAdapter(this);
            cdadapter.open();
            cdadapter.deleteItem(del.get(i));
            cdadapter.close();
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can restore the view hierarchy
        super.onRestoreInstanceState(savedInstanceState);

        // Restore state members from saved instance
        shoppingCart = savedInstanceState.getParcelableArrayList(SHOP_CART);
    }
    @Override
    public void onSaveInstanceState(Bundle SIS) {
        // TODO save the shopping cart contents (which should be a list of parcelables).
        super.onCreate(SIS); // Always call the superclass first
        super.onSaveInstanceState(SIS);
        SIS.putParcelableArrayList(SHOP_CART,shoppingCart);
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
                startActivityForResult(addIntent, ADD_REQUEST);
                return true;
            case R.id.checkout:
                CartDbAdapter cdadapter = new CartDbAdapter(this);
                cdadapter.open();
                Cursor cursor = cdadapter.fetchAllBooks();
                    if(cursor.getCount() == 0){
                        Toast.makeText(this, "Cart is Empty!!", Toast.LENGTH_LONG).show();
                    }
                    else {
                    Intent checkIntent = new Intent(this, CheckoutActivity.class);
                    String item_count = "";
                    item_count = item_count + cursor.getCount();
                    checkIntent.putExtra(EXTRA, item_count);
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
        if(resultCode == ADD_REQUEST)
        {
            CartDbAdapter cdadapter = new CartDbAdapter(this);
            cdadapter.open();
            Cursor cursor = cdadapter.fetchAllBooks();
            if(cursor != null){
                while(cursor.moveToNext())
                {
                    String[] from = {BookContract.TITLE,BookContract.Authors};
                    int[] to = new int[] { android.R.id.text1, android.R.id.text2 };

                    // Now create a list adaptor that encapsulates the result of a DB query
                    SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_activated_2, cursor, from, to);
                    lstview.setAdapter(adapter);
                }
            }
            cdadapter.close();
        }
        else if(resultCode == CHECKOUT_REQUEST)
        {
            lstview = (ListView) findViewById(android.R.id.list);
            CartDbAdapter cdadapter = new CartDbAdapter(this);
            cdadapter.open();
            cdadapter.deleteAll();
            cdadapter.close();
            SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_activated_2,null,null,null);
            setListAdapter(adapter);
            lstview.setSelection(0);
        }
    }
}