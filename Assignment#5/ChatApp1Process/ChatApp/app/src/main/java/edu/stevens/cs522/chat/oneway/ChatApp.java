package edu.stevens.cs522.chat.oneway;

import java.util.ArrayList;
import provider.ChatProvider;
import service.ChatReceiverService;
import service.ChatSendService;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;

public class ChatApp extends ListActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {
    final static public String TAG = ChatApp.class.getCanonicalName();

    private static final int LOADER_ID1 = 0;
    private static final int LOADER_ID2 = 1;
    static final private int LIST_PEERS = 1;

    public String CLIENT_NAME_KEY = "un";
    public String CLIENT_PORT_KEY = "pt";

    private String clientName;
    private int clientPort;

    private ServiceConnection connection;
    private ChatReceiverService service;

    private	ArrayList<String> messageList;
    ArrayAdapter<String> adapter;
    public SimpleCursorAdapter sca;
    private EditText destinationHost;

    private EditText destinationPort;

    private EditText messageText;

    public class AckReceiver extends ResultReceiver {
        public AckReceiver(Handler handler) {
            super(handler);
        }
        protected void onReceiveResult(int resultCode, Bundle result) {
            Context context2 = getApplicationContext();
            int duration2 = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context2, "Message received.", duration2);
            toast.show();
            sca.getCursor().requery();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        connection = new ServiceConnection() {
            public void onServiceConnected(ComponentName className, IBinder binder) {
                service = ((ChatReceiverService.MyBinder)binder).getService();
            }
            public void onServiceDisconnected(ComponentName className) {
                service = null;
            }
        };

        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            clientName = intent.getExtras().getString(CLIENT_NAME_KEY);
            clientPort = intent.getExtras().getInt(CLIENT_PORT_KEY);
            Context context2 = getApplicationContext();
            int duration2 = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context2, "Welcome " + clientName
                    + " at " + clientPort, duration2);
            toast.show();
            destinationPort = (EditText) findViewById(R.id.destination_port);
            destinationHost = (EditText) findViewById(R.id.destination_host);
            messageText = (EditText) findViewById(R.id.message_text);
            destinationPort.setText(String.valueOf(clientPort));
        }
        else
        {
            clientName = "frank";
            clientPort = 6666;
        }
        String[] from = new String[] { DbAdapter.SENDER, DbAdapter.TEXT };
        int[] to = new int[] { R.id.cart_row_sender, R.id.cart_row_message };
        try
        {
            Cursor c = getContentResolver().query(Contract.CONTENT_URI, ChatProvider.projection, null,null, null);
            sca= new SimpleCursorAdapter(this, R.layout.message, c, from, to);
            getListView().setAdapter(sca);
        }
        catch (Exception e)
        {
            Log.e(e.getClass().getName(), e.getMessage());
        }
        Intent bindIntent = new Intent(this, ChatReceiverService.class);
        bindIntent.putExtra("receiver",new AckReceiver(new Handler()));
        bindIntent.putExtra("port", clientPort);
        bindService(bindIntent, connection, Context.BIND_AUTO_CREATE);

        messageList = new ArrayList<String>();
        adapter = new ArrayAdapter<String>(this, R.layout.message, messageList);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.show_peers:
                Intent addIntent = new Intent(this, PeerList.class);
                startActivityForResult(addIntent, LIST_PEERS);
                this.onPause();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
    }

    public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle) {
        switch (loaderID) {
            case LOADER_ID1:
                return new CursorLoader(getActivity(), Contract.CONTENT_URI,
                        ChatProvider.projection, null, null, null);
            case LOADER_ID2:
                return new CursorLoader(getActivity(),
                        Contract.CONTENT_URI_PEER, ChatProvider.projection2,
                        null, null, null);
            default:
                return null;
        }
    }

    public void send(View v) {
        Intent sender=new Intent(this,ChatSendService.class);
        sender.putExtra("port", Integer.parseInt(destinationPort.getText().toString()));
        sender.putExtra("host", destinationHost.getText().toString());
        sender.putExtra("text", messageText.getText().toString());
        sender.putExtra("name", clientName);
        messageText.setText("");
        startService(sender);
    }

    private Context getActivity() {
        return this;
    }

    public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
        sca.swapCursor(arg1);
    }

    public void onLoaderReset(Loader<Cursor> arg0) {
        sca.swapCursor(null);
    }


}
