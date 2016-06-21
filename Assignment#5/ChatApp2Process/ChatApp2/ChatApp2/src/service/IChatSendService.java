package service;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.ResultReceiver;
import android.util.Log;

public class IChatSendService extends Service {
	private final IBinder binder = new MyBinder();
	ResultReceiver resultReceiver;
	int clientPort;
	String TAG="sender";
	public IChatSendService() {
		super();
	}
	public class MyBinder extends Binder {
		public IChatSendService getService() {
	      return IChatSendService.this;
	    }
	};
	@SuppressWarnings("unused")
	private class MessageHandler extends Handler{

		  @SuppressLint("HandlerLeak")
		public MessageHandler(ChatReceiverService chatReceiverService,
				Looper messengerLooper) {
			  super(messengerLooper);
			
		}

		public void handleMessage(Message message) {
		    Bundle data = message.getData();
		    ResultReceiver resultReceiver = data.getParcelable(null);
		}
	};
	@Override
	public IBinder onBind(Intent intent) {
		resultReceiver=(ResultReceiver)intent.getExtras().getParcelable("receiver");
		return binder;
	}
	
	public void sendmessage(Intent intent) {
		SendBackgroud SendTask= new SendBackgroud();
		SendTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, intent.getExtras());
	}
	class SendBackgroud extends AsyncTask<Bundle, Void, Void> {

		@Override
		protected Void doInBackground(Bundle... bundles) {
			try{
				DatagramSocket clientSocket;
				clientSocket = new DatagramSocket(null);
				clientSocket.setReuseAddress(true) ;
				clientSocket.bind(new InetSocketAddress(bundles[0].getInt("port")));
				
				InetAddress destAddr = null;
				
				int destPort = -1;
				
				byte[] sendData = null;  
				
				// TODO get data from UI
				destPort=bundles[0].getInt("port");
				destAddr=InetAddress.getByName(bundles[0].getString("host"));
				sendData=(bundles[0].getString("name")+": "+bundles[0].getString("text")).getBytes();
				
				DatagramPacket sendPacket = new DatagramPacket(sendData,
						sendData.length, destAddr, destPort);
				
				clientSocket.send(sendPacket);
				
				Log.i(TAG, "Sent packet: " + sendData.toString());
				clientSocket.close();
			} catch (UnknownHostException e) {
				Log.e(TAG, "Unknown host exception: " + e.getMessage());
			} catch (IOException e) {
				Log.e(TAG, "IO exception: " + e.getMessage());
				//new Toast(this).makeText(this, "Port taken", Toast.LENGTH_SHORT).show();
			}finally{
				
			}
			return null;
		}
		
	}
}
