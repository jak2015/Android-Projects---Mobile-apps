package edu.stevens.cs522.chat.oneway.server;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.TextView;
import contracts.PeerContract;
import database.DbAdapter;

public class ViewText extends Activity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.text_view);
        Intent intent = getIntent();
        long id = intent.getLongExtra(ShowPeers.Detail,0);

        try {
            DbAdapter adapter = new DbAdapter(this);
            adapter.open();
            Cursor cursor = adapter.fetchText(id);
            startManagingCursor(cursor);
            cursor.moveToFirst();
            int count = cursor.getCount();
            String msg = "";
            for (int i = 1; i <= count; i++) {
                msg = msg + "\n" + PeerContract.getMessageText(cursor);
                cursor.moveToNext();
            }
            String str = "";
            str = str + "Peer: " + PeerContract.getName(cursor) + '\n' + '\n' +
                    "Address: " + PeerContract.getAddress(cursor) + '\n' + '\n' +
                    "Port: " + PeerContract.getPort(cursor) + '\n' + '\n' +
                    "Message: " + msg;
            TextView passedView = (TextView) findViewById(R.id.msgList);
            passedView.setText(str);

        }catch(Exception e)
        {
            e.printStackTrace();
        }
    }
}
