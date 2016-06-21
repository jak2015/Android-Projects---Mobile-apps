package edu.stevens.cs522.chat.oneway.server;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import contracts.PeerContract;
import database.DbAdapter;

public class ShowPeers extends Activity
{
    ListView listView;
    public void display()
    {
        setContentView(R.layout.show_peers);
        DbAdapter adapter = new DbAdapter(this);
        try {
            adapter.open();
            Cursor cursor = adapter.fetchAllPeers();
            startManagingCursor(cursor);
            String[] from = new String[]{PeerContract.NAME};
            int[] to = new int[]{android.R.id.text1};
            SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_2, cursor, from, to);
            listView = (ListView) findViewById(R.id.peerlist);
            listView.setAdapter(cursorAdapter);

        }catch(Exception e)
        {
            e.printStackTrace();
        }
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent intent=new Intent(ShowPeers.this,ViewText.class);
                intent.putExtra(Detail,l);
                startActivity(intent);
            }
        });
    }
    public final static String Detail="Detail";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        display();
    }
}
