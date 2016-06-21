package edu.stevens.cs522.bookstore.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import edu.stevens.cs522.bookstore.R;

public class CheckoutActivity extends Activity {
    int c;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        c = intent.getIntExtra(BookStoreActivity.EXTRA,0);
		setContentView(R.layout.checkout);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		// TODO display ORDER and CANCEL options.
        MenuInflater inflater= getMenuInflater();
        inflater.inflate(R.menu.checkout_menu,menu);
        return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		// TODO
        switch(item.getItemId())
        {
            case R.id.order:
                EditText Credit = (EditText) findViewById(R.id.credit);
                EditText Name = (EditText) findViewById(R.id.name);
                EditText Email = (EditText) findViewById(R.id.email);
                EditText Address = (EditText) findViewById(R.id.address);
                String Credit1 = Credit.getText().toString();
                String Name1 = Name.getText().toString();
                String Email1 = Email.getText().toString();
                String Address1 = Address.getText().toString();
                if(Credit1.isEmpty() || Name1.isEmpty() || Email1.isEmpty() || Address1.isEmpty()){
                    Toast.makeText(getApplicationContext(),"Please Enter all Details", Toast.LENGTH_SHORT).show();
                    return true;
                }
                else{
                    Toast.makeText(getApplicationContext(),"Order for "+c+" book(s) has been placed", Toast.LENGTH_SHORT).show();
                    setResult(2);
                    finish();
                    return true;
                }
            case R.id.cancel:
                Toast.makeText(this, "Order cancelled!", Toast.LENGTH_LONG).show();
                setResult(RESULT_CANCELED);
                finish();
                return true;
        }
		// ORDER: display a toast message of how many books have been ordered and return
		
		// CANCEL: just return with REQUEST_CANCELED as the result code

		return false;
	}
	
}