package service;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.os.Messenger;
import android.os.ResultReceiver;
import android.util.Log;
import edu.stevens.cs522.chat.oneway.ChatApp;
import edu.stevens.cs522.chat.oneway.ChatMessage;
import edu.stevens.cs522.chat.oneway.Contract;
import edu.stevens.cs522.chat.oneway.Peer;

public class ChatReceiverService extends Service {
	private final IBinder binder = new MyBinder();
	public Messenger messenger;
	ResultReceiver resultReceiver;
	final static public String TAG = ChatApp.class.getCanonicalName();
	int port=6666;
	public class MyBinder extends Binder {
		public ChatReceiverService getService() {
	      return ChatReceiverService.this;
	    }
	};

	private class ChatAsyncTask extends AsyncTask<String,Integer,Integer>{

		@Override
		protected Integer doInBackground(String... arg0) {
			 int myProgress = 0;
			    // Perform background processing task, update myProgress
			 byte[] receiveData = new byte[1024];
			 DatagramSocket serverSocket; 
			 DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
			 
				try {
					String []res;
					serverSocket= new DatagramSocket(port);
					serverSocket.receive(receivePacket);
					Log.i(TAG, "Received a packet");

					InetAddress sourceIPAddress = receivePacket.getAddress();
					Log.i(TAG, "Source IP Address: " + sourceIPAddress);
					
					receiveData=receivePacket.getData();
					res=(new String(receiveData, 0, receivePacket.getLength())).split(":");
					ChatMessage m=new ChatMessage(1, res[1], res[0]);
					Peer p=new Peer(res[0], sourceIPAddress,receivePacket.getPort());
					if(!addMessage(m)){
						Log.e("addMessge", "fail");
					};
					if(!addPeer(p)){
						Log.e("addPeer", "fail");
					}
					serverSocket.close();
					} catch (IOException e) {
					}finally{
						resultReceiver.send(1, null);
					}
				
			 publishProgress(myProgress);
			 receiveMessage();
			 return myProgress;
		}
		public boolean addMessage(ChatMessage b) {
			ContentValues contentValues = new ContentValues();
			b.writeToProvider(contentValues);
			getContentResolver().insert(Contract.CONTENT_URI, contentValues);
			return true;
		}

		public boolean addPeer(Peer b) {
			ContentValues contentValues = new ContentValues();
			b.writeToProvider(contentValues);
			getContentResolver().delete(
					Contract.CONTENT_URI_PEER,
					"name ='" + b.name + "' and " + "address ='"
							+ b.address.getHostAddress() + "'", null);
			getContentResolver().insert(Contract.CONTENT_URI_PEER,
					contentValues);
			return true;
		}
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		receiveMessage();
	}
	public void receiveMessage() {
		  ChatAsyncTask reTask= new ChatAsyncTask();
		  reTask.execute(new String[] { "null" });
	}
	public ChatReceiverService() {
	}

	@Override
	public IBinder onBind(Intent intent) {
		port=intent.getExtras().getInt("port");
		resultReceiver=(ResultReceiver)intent.getExtras().getParcelable("receiver");
		return binder;
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	

	}