package edu.stevens.cs522.chat.oneway;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import provider.ChatProvider;
import service.ChatReceiverService;
import service.IChatSendService;
import android.R.integer;
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;
import android.os.ResultReceiver;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
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
	private ServiceConnection connectionSender;
	private ChatReceiverService service;
	private IChatSendService senderservice;
	Messenger messenger;

	private DatagramSocket serverSocket;
	DbAdapter da;

	private boolean socketOK = true;
	private ArrayList<String> messageList;
	ArrayAdapter<String> adapter;
	public SimpleCursorAdapter sca;

	String[] from = new String[] { DbAdapter.SENDER, DbAdapter.TEXT };
	int[] to = new int[] { R.id.cart_row_sender, R.id.cart_row_message };
	private EditText destinationHost;

	private EditText destinationPort;

	private EditText messageText;


    public class SendReceiver extends ResultReceiver {
        public SendReceiver(Handler handler) {
            super(handler);
            // TODO Auto-generated constructor stub
        }
        protected void onReceiveResult(int resultCode, Bundle result) {
			Toast.makeText(getApplicationContext(),"Message sent",Toast.LENGTH_LONG).show();
            sca.getCursor().requery();
        }
    };
	
	public class AckReceiver extends ResultReceiver {
		public AckReceiver(Handler handler) {
			super(handler);
			// TODO Auto-generated constructor stub
		}
		protected void onReceiveResult(int resultCode, Bundle result) {
			Toast.makeText(getApplicationContext(),"Message Received",Toast.LENGTH_LONG).show();
			sca.getCursor().requery();
		}
	};
	
	
	public class ResultReceiverWrapper extends ResultReceiver {
		  public ResultReceiverWrapper(Handler handler) {
		    super(handler);
		}
		  private IReceiver receiver;
		  public void setReceiver(IReceiver receiver) {
			  this.receiver = receiver;
		  }
		  protected void onReceiveResult(int resultCode, Bundle data) {
			  if (receiver != null) {
			    receiver.onReceiveResult(resultCode, data);
				Toast.makeText(getApplicationContext(),"Message sent.",Toast.LENGTH_LONG).show();
			  } 
		  }
	}
	public interface IReceiver {
		public void onReceiveResult(int resultCode,Bundle resultData);
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		connection = new ServiceConnection() { 
			public void onServiceConnected(ComponentName className, IBinder binder) {
			// Called when the connection is made.
				service = ((ChatReceiverService.MyBinder)binder).getService();
			}
			public void onServiceDisconnected(ComponentName className) {
				service = null;
			}
		};
		connectionSender = new ServiceConnection() { 
			public void onServiceConnected(ComponentName className, IBinder binder) {
				senderservice = ((IChatSendService.MyBinder)binder).getService();
			}
			public void onServiceDisconnected(ComponentName className) {
				senderservice = null;
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

		} else {

			clientName = "User";
			clientPort = 6666;

		}
		
		try {
			 Cursor c = getContentResolver().query(Contract.CONTENT_URI, ChatProvider.projection, null,null, null);
			 
			sca= new SimpleCursorAdapter(this, R.layout.message, c, from, to);
			getListView().setAdapter(sca);
		} catch (Exception e) {
			Log.e(e.getClass().getName(), e.getMessage());
		}
        Intent sendintent = new Intent(this,IChatSendService.class);
        sendintent.putExtra("sender",new SendReceiver(new Handler()));
        sendintent.putExtra("port", clientPort);
        bindService(sendintent, connectionSender, Context.BIND_AUTO_CREATE);

        Intent bindIntent = new Intent(this, ChatReceiverService.class);
		bindIntent.putExtra("receiver",new AckReceiver(new Handler()));
		bindIntent.putExtra("port", clientPort);
		bindService(bindIntent, connection, Context.BIND_AUTO_CREATE);

		messageList = new ArrayList<String>();
		adapter = new ArrayAdapter<String>(this, R.layout.message, messageList);
		
	}
	public void closeSocket() {
		serverSocket.close();
	}
	boolean socketIsOK() {
		return socketOK;
	}

	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		// TODO
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
		} // An invalid id was passed in
	}

	public boolean addMessage(ChatMessage b) {
		ContentValues contentValues = new ContentValues();
		b.writeToProvider(contentValues);
		try{
		getContentResolver().insert(Contract.CONTENT_URI, contentValues);
		}catch(Exception e){
			return false;
		}
		return true;
	}
	public void send(View v) {
		Intent sender=new Intent(this,IChatSendService.class);
		sender.putExtra("port", Integer.parseInt(destinationPort.getText().toString()));
		sender.putExtra("host", destinationHost.getText().toString());
		sender.putExtra("text", messageText.getText().toString());
		sender.putExtra("name", clientName);
		messageText.setText("");
		try{
		if(senderservice!=null)
			senderservice.sendmessage(sender);
		}catch(Exception e){
			Log.e(TAG, e.getClass().getName());
		}
	}


	public boolean addPeer(Peer b) {
		ContentValues contentValues = new ContentValues();
		b.writeToProvider(contentValues);
		try{
		getContentResolver().delete(
				Contract.CONTENT_URI_PEER,
				"name ='" + b.name + "' and " + "address ='"
						+ b.address.getHostAddress() + "'", null);
		getContentResolver().insert(Contract.CONTENT_URI_PEER,
				contentValues);
	}catch(Exception e){
		return false;
	}
		return true;
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

