package service;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public interface IChatSendService {
	public void send (DatagramSocket clientSocket,DatagramPacket p);
}
