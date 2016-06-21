package service;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import android.app.IntentService;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class ChatSendService extends IntentService implements IChatSendService{

    String TAG = "sender";
    public ChatSendService() {
        super("ChatSendService");
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try{
            DatagramSocket clientSocket;
            clientSocket = new DatagramSocket(null);
            clientSocket.setReuseAddress(true) ;
            clientSocket.bind(new InetSocketAddress(intent.getExtras().getInt("port")));

            InetAddress destAddr = null;

            int destPort = -1;

            byte[] sendData = null;  // Combine sender and message text; default encoding is UTF-8

            destPort=intent.getExtras().getInt("port");
            destAddr=InetAddress.getByName(intent.getExtras().getString("host"));
            sendData=(intent.getExtras().getString("name")+": "+intent.getExtras().getString("text")).getBytes();

            DatagramPacket sendPacket = new DatagramPacket(sendData,
                    sendData.length, destAddr, destPort);

            send(clientSocket,sendPacket);

            Log.i(TAG, "Sent packet: " + sendData.toString());
            clientSocket.close();
        } catch (UnknownHostException e) {
            Log.e(TAG, "Unknown host exception: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "IO exception: " + e.getMessage());
        }finally{

        }
    }

    public void send(DatagramSocket clientSocket,DatagramPacket p) {
        try {
            clientSocket.send(p);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
