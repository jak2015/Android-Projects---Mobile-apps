package edu.stevens.cs522.chat.oneway.server;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import database.DbAdapter;
import edu.stevens.cs522.chat.oneway.server.R;
import entities.Message;
import entities.Peer;

public class ChatServer extends Activity implements OnClickListener {
    ArrayList arrayList = new ArrayList<String>();
    final static public String TAG = ChatServer.class.getCanonicalName();
        /*
     * Socket used both for sending and receiving
     */
    private DatagramSocket serverSocket;

    /*
     * True as long as we don't get socket errors
     */
    private boolean socketOK = true;
    ListView listView;
    ArrayAdapter arrayAdapter;
    private String sender;
    private String text;
    Button next;

    /*
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        Button button = (Button)findViewById(R.id.next);
        button.setOnClickListener(this);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
			/*
			 * Get port information from the resources.
			 */
            int port = Integer.parseInt(this.getString(R.string.app_port));
            serverSocket = new DatagramSocket(port);
        } catch (Exception e) {
            Log.e(TAG, "Cannot open socket" + e.getMessage());
            return;
        }

		listView =(ListView)findViewById(R.id.msgList);
        arrayAdapter = new ArrayAdapter<String>(this,R.layout.message,arrayList);
        listView.setAdapter(arrayAdapter);
        registerForContextMenu(listView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater= getMenuInflater();
        inflater.inflate(R.menu.show_peers,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        super.onOptionsItemSelected(item);
        switch (item.getItemId())
        {
            case R.id.show_peers:
                Intent intent=new Intent(this,ShowPeers.class);
                startActivity(intent);
        }
        return true;
    }

    public void onClick(View v) {

        boolean flag = false;
        byte[] receiveData = new byte[1024];

        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

        try {
            serverSocket.receive(receivePacket);
            Log.i(TAG, "Received a packet");
            InetAddress sourceIPAddress = receivePacket.getAddress();
            Log.i(TAG, "Source IP Address: " + sourceIPAddress);
            int remotePort = receivePacket.getPort();

			/*
			 * TODO: Extract sender and receiver from message and display.
			 */
            String str = new String(receivePacket.getData());
            for(String retval: str.split(",",2))
            {
                if (flag == false)
                {
                    sender = retval;
                    flag = true;
                }
                else
                {
                    text = retval;
                    flag = false;
                }
            }
            DbAdapter adapter = new DbAdapter(this);
            adapter.open();
            boolean count = adapter.isExits(sender);
            if(count == false)
            {
                Peer peer = new Peer(3, sender, sourceIPAddress, remotePort);
                adapter.createPeer(peer);
                int id = adapter.fetchId(sender);
                Message msg = new Message(id,text,sender);
                adapter.createMessage(msg);
            }
            else
            {
                String port = sourceIPAddress.toString();
                adapter.updatePeer(sender,port,remotePort);
                int id = adapter.fetchId(sender);
                Message msg = new Message(id,text,sender);
                adapter.createMessage(msg);
            }
            adapter.close();
            String last = "From: "+sender+"\n"+"Text: "+text;
            arrayList.add(last);
            listView.setAdapter(arrayAdapter);
            arrayAdapter.notifyDataSetChanged();
            Log.i(TAG, "Received a packet");

        } catch (Exception e) {

            Log.e(TAG, "Problems receiving packet: " + e.getMessage());
            socketOK = false;
        }

    }

    /*
     * Close the socket before exiting application
     */
    public void closeSocket() {
        serverSocket.close();
    }

    /*
     * If the socket is OK, then it's running
     */
    boolean socketIsOK() {
        return socketOK;
    }

}