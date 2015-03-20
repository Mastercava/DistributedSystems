import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.HashMap;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;



public class Server {
	
	private final int serverId = 1; 
	
	private Messaging multicast;
	private FlatTable flatTable;
	
	
	public Server() {
		
		multicast = new Messaging(serverId);
		multicast.initialize();
		
		flatTable = new FlatTable();
		
	}
	
	public static void main(String[] args) {
				
		Server server = new Server();
		FlatTable flatTable = new FlatTable();
		
		server.startReceiving();

	}
	
	public void startReceiving() {
		
		Packet incomingPacket;
		
		while(true) {
			
			incomingPacket = multicast.receiveMessage();
			if(incomingPacket.getType() == 0) {
				System.out.println("Client #" + incomingPacket.getSenderId() + ": " + incomingPacket.getMessage());
			}
			else {
				//TODO
			}
			
		}
		
	}

}
