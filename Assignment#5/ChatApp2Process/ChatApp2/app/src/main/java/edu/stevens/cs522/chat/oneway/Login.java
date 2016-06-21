package edu.stevens.cs522.chat.oneway;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class Login extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}
	
	public void sendLogin(View view) {
	    Intent i=new Intent(this,ChatApp.class);
	    EditText username= (EditText)findViewById(R.id.editText1);
	    EditText port= (EditText)findViewById(R.id.editText2);
	    String un=username.getText().toString();
	    String pt=port.getText().toString();
	    
	    int ipt=0;
	    try{
	    ipt=Integer.parseInt(pt);
	    }catch(Exception e){}
	    if(ipt>65535|ipt<1){
	    	Context context = getApplicationContext();
			 int duration = Toast.LENGTH_SHORT;
			 Toast toast = Toast.makeText(context,"Port number should be 1-65535.", duration);
			 toast.show();
			 return;
	    }
	    if(!un.isEmpty()){
	    i.putExtra("un",un);
	    i.putExtra("pt",ipt);
	    startActivity(i);
	    }
	    return;
	}

}
