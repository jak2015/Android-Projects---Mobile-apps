package edu.stevens.cs522.chat.oneway.client;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class ParentActivity extends Activity
{
    public final static String SENDER_NAME = "MESSAGE";
    Button enter;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.parent_activity);
        enter = (Button)findViewById(R.id.button);
        enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
    }

    public void sendMessage()
    {
        Intent intent = new Intent(this,ChatClient.class);
        EditText editText = (EditText)findViewById(R.id.Sender_name);
        String sender_name = editText.getText().toString();
        if(sender_name.equals(""))
        {
            Toast.makeText(getApplicationContext(),"Please provide Input",Toast.LENGTH_LONG).show();
        }
        else
        {
            intent.putExtra(SENDER_NAME, sender_name);
            startActivity(intent);
        }
    }

}
